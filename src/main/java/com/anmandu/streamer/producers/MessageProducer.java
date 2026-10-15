package com.anmandu.streamer.producers;

import com.anmandu.streamer.dto.Input;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageProducer {
    private final KafkaTemplate<String, Input> kafkaTemplate;

    public MessageProducer(KafkaTemplate<String, Input> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String topic, Input message) {
        kafkaTemplate.send(topic, message);
    }
}
