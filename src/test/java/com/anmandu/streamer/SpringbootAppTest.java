package com.anmandu.streamer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import com.anmandu.streamer.config.StreamerProperties;
import com.anmandu.streamer.config.StreamerProperties.Sensor;

/**
 * Characterization tests of current behavior: the application context starts
 * and the properties in application.properties are bound.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class SpringbootAppTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private StreamerProperties properties;

    @Test
    void contextLoads() {
        assertNotNull(context);
    }

    @Test
    void bindsTimerPropertiesFromApplicationProperties() {
        assertEquals(4, properties.sensors().size());
        assertEquals(Duration.ofMillis(500), properties.sensors().get(0).period());
        assertEquals("SENSOR-A", properties.sensors().get(0).name());
        assertEquals(Duration.ofMillis(400), properties.sensors().get(1).period());
        assertEquals("SENSOR-B", properties.sensors().get(1).name());
        assertEquals(Duration.ofMillis(550), properties.sensors().get(2).period());
        assertEquals("SENSOR-C", properties.sensors().get(2).name());
        assertEquals(Duration.ofMillis(1000), properties.sensors().get(3).period());
        assertEquals("SENSOR-D", properties.sensors().get(3).name());
    }

    @Test
    void bindsTimerPropertiesFromApplicationPropertiesValidation() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Sensor("A", Duration.ZERO);
        });
    }
}
