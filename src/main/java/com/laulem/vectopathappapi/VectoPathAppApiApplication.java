package com.laulem.vectopathappapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class VectoPathAppApiApplication {

    static void main(String[] args) {
        SpringApplication.run(VectoPathAppApiApplication.class, args);
    }

}
