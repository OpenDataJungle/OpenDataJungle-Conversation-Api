package com.laulem.vectopath.conversation.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class VectoPathConversationApiApplication {

    static void main(String[] args) {
        SpringApplication.run(VectoPathConversationApiApplication.class, args);
    }

}
