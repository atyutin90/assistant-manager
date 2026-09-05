package ru.otus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "ru.otus.service", excludeName = {
    "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
    "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration"
})
public class IntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegrationApplication.class, args);
    }
}
