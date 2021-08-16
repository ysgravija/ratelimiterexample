package org.example.archive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;

/**
 * @author yeesheng on 10/08/2021
 * @project RateLimiterExample
 */
//@Component
@EnableJms
public class MessageProducer {
    @Autowired
    private JmsTemplate jmsTemplate;

    public void sendMessage(final String queue, final String message) {
        jmsTemplate.convertAndSend(queue, message);
    }
}
