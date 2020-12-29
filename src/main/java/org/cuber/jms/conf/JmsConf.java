package org.cuber.jms.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.cuber.jms.activemq.JmsPlusTemplate;
import org.cuber.jms.activemq.ProxyMessageListener;
import org.cuber.jms.activemq.ResendHandler;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerConfigUtils;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.jms.listener.adapter.MessagingMessageListenerAdapter;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.util.ErrorHandler;

import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.cuber.jms.constants.MqConstants.TYPE_PROPERTY;

@Configuration(proxyBeanMethods = false)
@EnableJms
@EnableAsync
@Slf4j
public class JmsConf {

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private ResendHandler resendHandler;

    @Bean
    public ErrorHandler errorHandler() {
        return t -> log.error("打印错误信息", t);
    }

    /**
     * 消息系列化
     *
     * @param objectMapper
     * @return
     */
    @Bean
    public MessageConverter getJacksonMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        messageConverter.setTargetType(MessageType.TEXT);
        messageConverter.setTypeIdPropertyName(TYPE_PROPERTY);
        messageConverter.setObjectMapper(objectMapper);
        messageConverter.setEncoding(StandardCharsets.UTF_8.name());
        return messageConverter;
    }


    @Bean(name = "topicJmsFactory")
    public JmsListenerContainerFactory<?> defaultJmsListener(ConnectionFactory connectionFactory,
                                                             DefaultJmsListenerContainerFactoryConfigurer configurer,
                                                             ErrorHandler errorHandler) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setErrorHandler(errorHandler);
        factory.setPubSubDomain(true);
        return factory;
    }


    @Bean(name = "jmsTemplate")
    @Primary
    public JmsTemplate jmsTemplateQueue(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setPubSubDomain(false);
        jmsTemplate.setMessageConverter(messageConverter);
        return jmsTemplate;
    }


    @Bean(name = "jmsTemplateTopic")
    public JmsTemplate jmsTemplateTopic(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setPubSubDomain(true);
        jmsTemplate.setMessageConverter(messageConverter);
        return jmsTemplate;
    }


    @Bean
    public JmsPlusTemplate jmsPlusTemplate(JmsTemplate jmsTemplate) {
        return new JmsPlusTemplate(jmsTemplate);
    }

    @Bean
    public ResendHandler resendHandler(JmsPlusTemplate jmsPlusTemplate, JmsTemplate jmsTemplate, MessageConverter messageConverter) {
        resendHandler = new ResendHandler(jmsTemplate, jmsPlusTemplate, messageConverter);
        return resendHandler;
    }

    @EventListener
    public void applicationStart(ApplicationReadyEvent applicationReadyEvent) throws Exception {
        JmsListenerEndpointRegistry jmsListenerEndpointRegistry = applicationContext.getBean(JmsListenerConfigUtils.JMS_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME, JmsListenerEndpointRegistry.class);
//        if (jmsListenerEndpointRegistry instanceof TracingJmsListenerEndpointRegistry) {
//            Field delegate = FieldUtils.getField(TracingJmsListenerEndpointRegistry.class, "delegate", true);
//            jmsListenerEndpointRegistry = (JmsListenerEndpointRegistry) delegate.get(jmsListenerEndpointRegistry);
//        }
        Field listenerContainers = FieldUtils.getField(JmsListenerEndpointRegistry.class, "listenerContainers", true);
        Map<String, MessageListenerContainer> listenerContainerMap = (Map<String, MessageListenerContainer>) listenerContainers.get(jmsListenerEndpointRegistry);
        if (MapUtils.isNotEmpty(listenerContainerMap)) {
            listenerContainerMap.forEach((id, value) -> {
                if (value instanceof AbstractMessageListenerContainer) {
                    AbstractMessageListenerContainer abstractMessageListenerContainer = (AbstractMessageListenerContainer) value;
                    Object messageListener = abstractMessageListenerContainer.getMessageListener();
                    if (messageListener instanceof MessagingMessageListenerAdapter && !(
                            messageListener instanceof ProxyMessageListener)) {
                        abstractMessageListenerContainer.setMessageListener(new ProxyMessageListener(resendHandler, (MessagingMessageListenerAdapter) messageListener));
                    }
                }
            });
        }
    }
}
