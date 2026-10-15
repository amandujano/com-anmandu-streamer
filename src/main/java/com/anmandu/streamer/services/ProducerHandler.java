package com.anmandu.streamer.services;

import com.anmandu.streamer.config.StreamerProperties;
import com.anmandu.streamer.producers.MessageProducer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProducerHandler {

    private final StreamerProperties props;
    private final MessageProducer prod;
    private List<ProducerScheduler> producers;

    public ProducerHandler(StreamerProperties props, MessageProducer prod){
        this.props = props;
        this.prod = prod;
        this.producers = new ArrayList<>();

    }

    @PostConstruct
    public void init(){
        List<StreamerProperties.Sensor> sensors = props.sensors();
        for(StreamerProperties.Sensor item : sensors){
            producers.add(new ProducerScheduler(item, prod));
        }
        producers.forEach(ProducerScheduler::start);
    }

    @PreDestroy
    public void destroy(){
        producers.forEach(ProducerScheduler::finish);
    }
}
