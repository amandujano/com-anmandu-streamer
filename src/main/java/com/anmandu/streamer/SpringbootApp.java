package com.anmandu.streamer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class SpringbootApp {
    public static void main(String[] args) {
        SpringApplication.run(SpringbootApp.class, args);
    }
}
KAFKA_ADMIN_PASSWORD='<contraseña de admin>' scripts/kafka-acls-remote.sh app test-topic test-group