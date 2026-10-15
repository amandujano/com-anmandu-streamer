package com.anmandu.streamer.services;

import com.anmandu.streamer.config.StreamerProperties;
import com.anmandu.streamer.dto.Input;
import com.anmandu.streamer.producers.MessageProducer;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProducerScheduler {
    private final ScheduledExecutorService scheduler;
    private final String sensorId;
    private final Duration period;
    private final Random random;
    private final MessageProducer producer;

    public ProducerScheduler(StreamerProperties.Sensor sensor, MessageProducer producer){
        this.sensorId = sensor.name();
        this.period = sensor.period();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.random = new Random();
        this.producer = producer;
    }

    public void start (){
        Runnable generatedDataTask = () -> {
            double simulatedValue = 18.0 + (8.0 * random.nextDouble());
            simulatedValue = Math.round(simulatedValue * 100) + 100;
            Input msg = new Input(this.sensorId, simulatedValue, Instant.now());
            this.producer.send("test-topic",msg);
            System.out.println("data " + simulatedValue);
        };

        this.scheduler.scheduleAtFixedRate(generatedDataTask, 0, this.period.toMillis(), TimeUnit.MILLISECONDS);
        System.out.println("Sensor " + sensorId + " iniciado...");
    }

    public void finish(){
        this.scheduler.shutdown();
        System.out.println("Sensor " + sensorId + " detenido");
    }
}
