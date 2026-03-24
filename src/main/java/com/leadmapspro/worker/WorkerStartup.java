package com.leadmapspro.worker;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "leadmaps.worker", name = "enabled", havingValue = "true")
public class WorkerStartup implements CommandLineRunner {

    private final Worker worker;

    public WorkerStartup(Worker worker) {
        this.worker = worker;
    }

    @Override
    public void run(String... args) throws Exception {
        worker.start();
    }
}
