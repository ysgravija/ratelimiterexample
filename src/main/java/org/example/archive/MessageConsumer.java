package org.example.archive;

import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

/**
 * @author yeesheng on 10/08/2021
 * @project RateLimiterExample
 */

//@Component
@EnableJms
public class MessageConsumer {

    @JmsListener(destination = "DEV.QUEUE.1", concurrency = "3-5")
    public void receiveMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                String stringMessage = textMessage.getText();
                System.out.println();
                System.out.println("========================================");
                System.out.println(Thread.currentThread().getName() + " - Received message is: " + stringMessage);
                System.out.println("========================================");
            }
        } catch (JMSException e) {
            //catch error
        }
    }
}
