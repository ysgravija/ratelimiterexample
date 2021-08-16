package org.example.execution;

import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author yeesheng on 15/08/2021
 * @project RateLimiterExample
 */
public class TestExecutionRunnable implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestExecutionRunnable.class);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private String name;
    private String queueName;
    private RateLimiter rateLimiter;
    private JmsTemplate jmsTemplate;

    public TestExecutionRunnable(String name, String queueName, int permitPerSecond, long warmUpPeriod, JmsTemplate jmsTemplate) {
        this.name = name;
        this.queueName = queueName;
        this.rateLimiter = RateLimiter.create(permitPerSecond, warmUpPeriod, TimeUnit.SECONDS);
        this.jmsTemplate = jmsTemplate;
    }

    public double getRate() {
        return rateLimiter.getRate();
    }

    public void stop() {
        running.set(false);
    }

    public boolean isRunning() {
        return running.get();
    }

    @Override
    public void run() {
        running.set(true);
        try {
            LOGGER.info(Thread.currentThread().getName() + " - {} - Starting test execution with {} pps.", name, rateLimiter.getRate());
            while(running.get()) {
                rateLimiter.acquire(1);
                jmsTemplate.convertAndSend(queueName, name + " says hello!");
            }
            LOGGER.info(Thread.currentThread().getName() + " - {} - Stopping test execution!", name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
