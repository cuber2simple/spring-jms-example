package org.cuber.example;

import lombok.extern.slf4j.Slf4j;
import org.cuber.jms.JmsApplication;
import org.cuber.jms.example.ExampleMq;
import org.cuber.jms.mq.MqSender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = JmsApplication.class)
@Slf4j
public class JmsTest {

    @Resource
    private MqSender mqSender;

    @Test
    public void test() throws Exception {
        ExampleMq exampleMq = new ExampleMq();
        exampleMq.setAge(12);
        exampleMq.setName("cuber");
        log.info("发送正常消息");
        mqSender.sendNormalQueue(exampleMq);
        log.info("发送队列名为配置的队列名");
        mqSender.sendDirectQueue(exampleMq);
        log.info("发送广播");
        mqSender.sendTopicQueue(exampleMq);
        log.info("发送延迟消息");
        mqSender.sendDelayedQueue(exampleMq, 3);
        TimeUnit.SECONDS.sleep(5);
        log.info("发送系列延迟消息");
        mqSender.sendDelay(exampleMq);
        TimeUnit.SECONDS.sleep(20);
    }
}
