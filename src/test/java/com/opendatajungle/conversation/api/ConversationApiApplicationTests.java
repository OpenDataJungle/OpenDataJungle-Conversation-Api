package com.opendatajungle.conversation.api;

import com.opendatajungle.conversation.api.testconfig.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class ConversationApiApplicationTests {

    @Test
    void contextLoads() {
    }

}
