package com.example.crud.config;

import com.example.crud.entity.Task;
import com.example.crud.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final TaskRepository taskRepository;

    @Override
    public void run(String... args) {
        taskRepository.count()
                .filter(count -> count == 0)
                .flatMapMany(count -> {
                    log.info("Seeding 100 tasks into the database...");
                    return Flux.range(1, 100)
                            .map(i -> new Task(null, "Task " + i, "Description for task " + i, i % 2 == 0))
                            .flatMap(taskRepository::save);
                })
                .subscribe(
                        task -> {},
                        error -> log.error("Error seeding data: {}", error.getMessage()),
                        () -> log.info("Database seeding completed.")
                );
    }
}
