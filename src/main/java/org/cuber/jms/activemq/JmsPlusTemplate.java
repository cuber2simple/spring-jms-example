package org.cuber.jms.activemq;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ScheduledMessage;
import org.apache.commons.collections4.MapUtils;
import org.cuber.jms.utils.DelayUtils;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.JMSException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.cuber.jms.constants.MqConstants.*;


@Slf4j
public class JmsPlusTemplate {

    private JmsTemplate jmsTemplate;

    public JmsPlusTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void sendDelay(String destinationName, Duration duration, Map<String, Object> headers, Object obj) {
        jmsTemplate.convertAndSend(destinationName, obj, message -> {
            if (MapUtils.isNotEmpty(headers)) {
                headers.forEach((key, value) -> {
                    try {
                        message.setObjectProperty(key, value);
                    } catch (JMSException e) {
                        log.error("组装报文报错", e);
                    }
                });
            }
            message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, duration.toMillis());
            return message;
        });
    }

    /**
     * 发送延迟队列
     *
     * @param destinationName
     * @param duration
     * @param obj
     */
    public void sendDelay(String destinationName, Duration duration, Object obj) {
        sendDelay(destinationName, duration, null, obj);
    }

    public void sendDelay(String destinationName, Object obj, String delayQueue) {
        sendDelay(destinationName, obj, delayQueue, 0);
    }

    public boolean sendDelay(String destinationName, Object obj, String delayQueue, int current) {
        if (!DelayUtils.isRight(delayQueue)) {
            throw new RuntimeException("[" + delayQueue + "]非法");
        }
        boolean result = false;
        String[] durations = delayQueue.split(",");
        if (current < durations.length) {
            Duration duration = DelayUtils.duration(delayQueue, current);
            Map<String, Object> stringObjectMap = new HashMap<>();
            stringObjectMap.put(DELAY_SEQUENCE, delayQueue);
            stringObjectMap.put(DELAY_SEQUENCE_CURRENT, current);
            sendDelay(destinationName, duration, stringObjectMap, obj);
            result = true;
        }
        return result;
    }


}
