package ru.baby_benz.kontur.intern.chartographer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class ChartographerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChartographerApplication.class, args);
    }
}
