package org.cuber.jms.activemq;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.scheduling.annotation.Async;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import static org.cuber.jms.constants.MqConstants.*;

@Slf4j
public class ResendHandler {

    private JmsTemplate jmsTemplate;

    private JmsPlusTemplate jmsPlusTemplate;

    private MessageConverter messageConverter;

    public ResendHandler(JmsTemplate jmsTemplate, JmsPlusTemplate jmsPlusTemplate, MessageConverter messageConverter) {
        this.jmsTemplate = jmsTemplate;
        this.jmsPlusTemplate = jmsPlusTemplate;
        this.messageConverter = messageConverter;
    }

    @Async
    public void doResend(Message jmsMessage) throws JMSException {
        Destination jmsDestination = jmsMessage.getJMSDestination();
        if (jmsDestination instanceof ActiveMQDestination) {
            ActiveMQDestination activeMQDestination = (ActiveMQDestination) jmsDestination;
            String physicalName = activeMQDestination.getPhysicalName();
            if (activeMQDestination.isQueue() && !StringUtils.endsWith(physicalName, _DEAD_CALL)) {
                boolean isDelayQueue = isDelayQueue(jmsMessage);
                Object data = messageConverter.fromMessage(jmsMessage);
                if (isDelayQueue) {
                    String delayQueue = jmsMessage.getStringProperty(DELAY_SEQUENCE);
                    int index = jmsMessage.getIntProperty(DELAY_SEQUENCE_CURRENT) + 1;
                    if (!jmsPlusTemplate.sendDelay(physicalName, data, delayQueue, index)) {
                        jmsTemplate.convertAndSend(physicalName + _DEAD_CALL, data);
                    }
                } else {
                    jmsTemplate.convertAndSend(physicalName + _DEAD_CALL, data);

                }
            }
        }
    }

    private boolean isDelayQueue(Message jmsMessage) throws JMSException {
        return StringUtils.isNotBlank(jmsMessage.getStringProperty(DELAY_SEQUENCE));
    }
}
