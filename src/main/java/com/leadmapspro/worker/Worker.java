package com.leadmapspro.worker;

import com.leadmapspro.domain.Job;
import com.leadmapspro.repository.JobRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class Worker {

    private static final int STUCK_MINUTES = 5;
    private static final int MAX_TENTATIVAS = 3;

    private final JobRepository jobRepository;
    private final TransactionTemplate transactionTemplate;

    public Worker(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public void start() throws InterruptedException {
        while (true) {
            runCycle();
            Thread.sleep(2000);
        }
    }

    private void runCycle() {
        transactionTemplate.executeWithoutResult(status -> recoverStuckProcessingJobs());

        Job job =
                transactionTemplate.execute(
                        status -> {
                            Optional<Job> pending =
                                    jobRepository.findTopByStatusOrderByCreatedAtAsc("PENDING");
                            if (pending.isEmpty()) {
                                return null;
                            }
                            Job j = pending.get();
                            System.out.println("Job " + j.getId() + " - status: " + j.getStatus());
                            System.out.println("Job " + j.getId() + " - iniciando processamento");
                            j.setStatus("PROCESSING");
                            Job saved = jobRepository.save(j);
                            System.out.println("Job " + saved.getId() + " - status: " + saved.getStatus());
                            return saved;
                        });

        if (job == null) {
            return;
        }

        String jobId = job.getId();
        try {
            executarScraping(job);
            transactionTemplate.executeWithoutResult(
                    status ->
                            jobRepository
                                    .findById(jobId)
                                    .ifPresent(
                                            j -> {
                                                j.setStatus("DONE");
                                                jobRepository.save(j);
                                                System.out.println(
                                                        "Job " + j.getId() + " - status: " + j.getStatus());
                                                System.out.println(
                                                        "Job " + j.getId() + " - finalizado com sucesso");
                                            }));
        } catch (Exception e) {
            System.out.println("Job " + jobId + " - erro: " + e.getMessage());
            transactionTemplate.executeWithoutResult(
                    status ->
                            jobRepository
                                    .findById(jobId)
                                    .ifPresent(
                                            j -> {
                                                j.setTentativas(j.getTentativas() + 1);
                                                if (j.getTentativas() < MAX_TENTATIVAS) {
                                                    j.setStatus("PENDING");
                                                } else {
                                                    j.setStatus("ERROR");
                                                }
                                                jobRepository.save(j);
                                                System.out.println(
                                                        "Job " + j.getId() + " - status: " + j.getStatus());
                                            }));
        }
    }

    private void recoverStuckProcessingJobs() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(STUCK_MINUTES);
        List<Job> stuck = jobRepository.findByStatusAndUpdatedAtBefore("PROCESSING", cutoff);
        for (Job job : stuck) {
            System.out.println(
                    "Job "
                            + job.getId()
                            + " - recuperado (PROCESSING > "
                            + STUCK_MINUTES
                            + " min), voltando para PENDING");
            job.setStatus("PENDING");
            jobRepository.save(job);
        }
    }

    private void executarScraping(Job job) {
        // implementação futura
    }
}
