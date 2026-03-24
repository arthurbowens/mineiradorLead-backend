package com.leadmapspro.api;

import com.leadmapspro.domain.Job;
import com.leadmapspro.repository.JobRepository;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "${leadmaps.cors.allowed-origins:http://localhost:4200}")
public class JobController {

    private final JobRepository jobRepository;

    public JobController(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @PostMapping
    public Job create(@RequestBody(required = false) Map<String, String> body) {
        Job job = new Job();
        job.setId(UUID.randomUUID().toString());
        if (body != null && body.get("tipo") != null) {
            job.setTipo(body.get("tipo"));
        } else {
            job.setTipo("");
        }
        if (body != null && body.get("payload") != null) {
            job.setPayload(body.get("payload"));
        } else {
            job.setPayload("");
        }
        job.setStatus("PENDING");
        job.setCreatedAt(LocalDateTime.now());
        return jobRepository.save(job);
    }
}
