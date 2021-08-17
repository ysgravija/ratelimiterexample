package org.example.execution;

import org.example.limiter.PasRateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

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
    private JmsTemplate jmsTemplate;
    private PasRateLimiter rateLimiter;
    private int permitPerSecond;
    private long rampInterval;
    private long rampPeriod;
    private long soakDuration;
    private boolean enableSoakTest;

    public TestExecutionRunnable(
            String name, String queueName, JmsTemplate jmsTemplate,
            int permitPerSecond, boolean enableSoakTest, long rampInterval,
            long rampPeriod, long soakDuration
            ) {
        this.name = name;
        this.queueName = queueName;
        this.jmsTemplate = jmsTemplate;
        this.permitPerSecond = permitPerSecond;
        this.rampInterval = rampInterval;
        this.rampPeriod = rampPeriod;
        this.soakDuration = soakDuration;
        this.enableSoakTest = enableSoakTest;
    }

    public double getRate() {
        return rateLimiter.getRateLimiter().getRate();
    }

    public void stop() {
        rateLimiter.stopSoakTimer();
        running.set(false);
    }

    public boolean isRunning() {
        return running.get();
    }

    public void setRate(double rate) {
        LOGGER.info(Thread.currentThread().getName() + " - {} - Update pps to {}", name, rateLimiter.getRateLimiter().getRate());
        rateLimiter.getRateLimiter().setRate(rate);
    }

    @Override
    public void run() {
        rateLimiter = new PasRateLimiter(
                permitPerSecond, enableSoakTest, soakDuration, rampPeriod, rampInterval);

        running.set(true);
        try {
            LOGGER.info(Thread.currentThread().getName() + " - {} - Starting test execution with {} pps.", name, rateLimiter.getRateLimiter().getRate());
            while(running.get()) {
                rateLimiter.getRateLimiter().acquire(1);
                jmsTemplate.convertAndSend(queueName, name + " says hello!");
            }
            LOGGER.info(Thread.currentThread().getName() + " - {} - Stopping test execution!", name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
