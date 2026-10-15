package com.anmandu.streamer.consumers;

import com.anmandu.streamer.dto.Input;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MessageConsumer {
    private Input payload;

    @KafkaListener(topics = "test-topic", groupId = "test-group")
    public void receive(Input message) {
        this.payload = message;
        System.out.println("received");
        System.out.println(this.payload);
        // aggregate
    }

    public Input getPayload() {
        return payload;
    }
}
