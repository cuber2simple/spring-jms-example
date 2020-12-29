package org.cuber.jms.mq;

import lombok.extern.slf4j.Slf4j;
import org.cuber.jms.example.ExampleMq;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static org.cuber.jms.constants.MqConstants._DEAD_CALL;
import static org.cuber.jms.mq.MqBizConstants.*;

@Component
@Slf4j
public class MqListener {

    @JmsListener(destination = NORMAL_QUEUE)
    public void listener(@Payload ExampleMq obj) {
        log.info("正常监听得到消息:{}", obj);
    }

    /**
     * 配置concurrency 是配置并发数量
     *
     * @param obj
     */
    @JmsListener(destination = "${config.direct:queue}", concurrency = "2-4")
    public void listenerDirect(@Payload ExampleMq obj) {
        log.info("配置在配置中心的队列名得到消息:{}", obj);
    }

    @JmsListener(destination = NORMAL_TOPIC, containerFactory = "topicJmsFactory")
    public void listenerTopic(@Payload ExampleMq obj) {
        log.info("正常监听广播得到消息:{}", obj);
    }

    @JmsListener(destination = DELAY_QUEUE)
    public void delayNormal(@Payload ExampleMq obj) {
        log.info("监听延迟消息得到:{}", obj);
    }

    @JmsListener(destination = ERROR_QUEUE)
    public void errorListener(@Payload ExampleMq obj) {
        log.info("错误监听错误消息:{}", obj);
        throw new Error("错误信息");
    }

    @JmsListener(destination = ERROR_QUEUE + _DEAD_CALL)
    public void errorListener_deadCall(@Payload ExampleMq obj) {
        log.info("到了最终错误节点吧:{}", obj);
    }
}
