package com.pickmebackend.properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class AppPropertiesTest {

    @Autowired
    AppProperties appProperties;

    @Test
    void propertyTest() {
        assertEquals(appProperties.getTestEmail(), "user@email.com");
        assertEquals(appProperties.getTestNickname(), "DNDpickme");
        assertEquals(appProperties.getTestPassword(), "password");
        assertEquals(appProperties.getTestAnotherEmail(), "another@email.com");
        assertEquals(appProperties.getTestAnotherNickname(), "springmaster");
        assertEquals(appProperties.getTestRegistrationNumber(), "01-2345-6789");
        assertEquals(appProperties.getTestName(), "KSU");
        assertEquals(appProperties.getTestAddress(), "BUSAN");
        assertEquals(appProperties.getTestCeoName(), "sangyeop");
    }
}