package io.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class AdminFrameworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminFrameworkApplication.class, args);
    }

}
