package org.example.archive;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author yeesheng on 10/08/2021
 * @project RateLimiterExample
 */
//@Component
public class ThrottleController {

    private final long THROTTLE_COUNT = 100L;
    private final String destinationQueue = "DEV.QUEUE.1";

    private RateLimiter rateLimiter;
    private MessageProducer messageProducer;

    public ThrottleController(ConfigurableApplicationContext context) {
        messageProducer = context.getBean("messageProducer", MessageProducer.class);
        rateLimiter = RateLimiter.create(THROTTLE_COUNT, 1, TimeUnit.SECONDS);
    }

    public double getLimiterRate() {
        return rateLimiter.getRate();
    }
    public void setLimiterRate(double permitsPerSecond) {
        rateLimiter.setRate(permitsPerSecond);
    }

    public void sendMessage(String message) throws Exception {
        rateLimiter.acquire(1);
        message = message + " Sent on " + new Date();
        messageProducer.sendMessage(destinationQueue, message);
    }
/*
 * Calculates the expected QPS given a stableRate, a warmupPeriod, and a saturatedPeriod.
 * <p>
 * Both specified periods must use the same time unit.
 *
 * @param stableRate      how many permits become available per second once stable
 * @param warmupPeriod    the duration of the period where the {@code RateLimiter} ramps up
 *                        its rate, before reaching its stable (maximum) rate
 * @param saturatedPeriod the duration of the period for which the {@code RateLimiter} has
 *                        been under saturated demand starting from a cold state
 * @return The expected QPS assuming saturated demand starting from a cold state
 */
    public static double qps(double stableRate, double warmupPeriod, double saturatedPeriod) {
        if (saturatedPeriod >= warmupPeriod) {
            return stableRate;
        }
        double coldRate = stableRate / 3.0;
        return (stableRate - coldRate) * saturatedPeriod / warmupPeriod + coldRate;
    }
}
