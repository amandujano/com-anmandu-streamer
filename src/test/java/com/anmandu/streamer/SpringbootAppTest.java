package com.anmandu.streamer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/**
 * Characterization tests of current behavior: the application context starts
 * and the properties in application.properties are bound.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class SpringbootAppTest {

    @Autowired
    private ApplicationContext context;

    @Value("${timer.sensorA}")
    private long timerSensorA;

    @Value("${timer.sensorB}")
    private long timerSensorB;

    @Value("${timer.sensorC}")
    private long timerSensorC;

    @Value("${timer.sensorD}")
    private long timerSensorD;

    @Value("${multiple.publishers}")
    private boolean multiplePublishers;

    @Test
    void contextLoads() {
        assertNotNull(context);
    }

    @Test
    void bindsTimerPropertiesFromApplicationProperties() {
        assertEquals(500L, timerSensorA);
        assertEquals(400L, timerSensorB);
        assertEquals(550L, timerSensorC);
        assertEquals(1000L, timerSensorD);
    }

    @Test
    void bindsMultiplePublishersProperty() {
        assertTrue(multiplePublishers);
    }
}
