package org.cuber.jms.activemq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.listener.adapter.MessagingMessageListenerAdapter;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

@Slf4j
public class ProxyMessageListener extends MessagingMessageListenerAdapter {

    private ResendHandler resendHandler;

    private MessagingMessageListenerAdapter delegate;

    public ProxyMessageListener(ResendHandler resendHandler, MessagingMessageListenerAdapter messagingMessageListenerAdapter) {
        this.resendHandler = resendHandler;
        this.delegate = messagingMessageListenerAdapter;
    }

    @Override
    public void onMessage(Message jmsMessage, Session session) throws JMSException {
        try {
            delegate.onMessage(jmsMessage, session);
        } catch (Throwable e) {
            session.commit();
            resendHandler.doResend(jmsMessage);
            throw e;
        }
    }

}
