package com.opendatajungle.conversation.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ConversationApiApplication {

    static void main(String[] args) {
        SpringApplication.run(ConversationApiApplication.class, args);
    }

}
