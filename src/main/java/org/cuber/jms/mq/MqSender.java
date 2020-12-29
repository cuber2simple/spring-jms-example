package org.cuber.jms.mq;

import org.cuber.jms.activemq.JmsPlusTemplate;
import org.cuber.jms.example.ExampleMq;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;

@Component
public class MqSender {

    @Resource
    private JmsTemplate jmsTemplate;

    @Resource(name = "jmsTemplateTopic")
    private JmsTemplate topicTemplate;

    @Resource
    private JmsPlusTemplate jmsPlusTemplate;

    @Value("${config.direct:queue}")
    private String directQueue;

    public void sendNormalQueue(ExampleMq exampleMq) {
        jmsTemplate.convertAndSend(MqBizConstants.NORMAL_QUEUE, exampleMq);
    }

    public void sendDirectQueue(ExampleMq exampleMq) {
        jmsTemplate.convertAndSend(directQueue, exampleMq);
    }

    public void sendTopicQueue(ExampleMq exampleMq) {
        topicTemplate.convertAndSend(MqBizConstants.NORMAL_TOPIC, exampleMq);
    }

    public void sendDelayedQueue(ExampleMq exampleMq, int seconds) {
        jmsPlusTemplate.sendDelay(MqBizConstants.DELAY_QUEUE, Duration.ofSeconds(seconds), exampleMq);
    }

    public void sendDelay(ExampleMq exampleMq) {
        //发送延迟系列
        jmsPlusTemplate.sendDelay(MqBizConstants.ERROR_QUEUE, exampleMq, "pt2s,pt4s,pt6s");
    }

}
