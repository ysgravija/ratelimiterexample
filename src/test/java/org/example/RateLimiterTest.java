package org.example;

import com.google.common.util.concurrent.RateLimiter;
import org.example.archive.ThrottleController;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.Assert.assertTrue;

/**
 * @author yeesheng on 10/08/2021
 * @project RateLimiterExample
 */
public class RateLimiterTest {

    @Test
    public void testStableRate() {
        System.out.println(ThrottleController.qps(100,10, 3));
    }
    @Test
    public void givenLimitedResource_whenREquestedOnce_thenShouldPermitWithoutBlocking() {
        // given
        RateLimiter rateLimiter = RateLimiter.create(100);

        // when
        long startTime = ZonedDateTime.now().getSecond();
        rateLimiter.acquire(100);
        doSomeOperation();
        long elapsedTimeSeconds = ZonedDateTime.now().getSecond() - startTime;

        // then
        assertTrue(elapsedTimeSeconds <= 1);
    }

    private void doSomeOperation() {
        System.out.println("Do some operation!!");
    }

    @Test
    public void givenLimitedResource_whenUseRateLimiter_thenShouldLimitPermits() {
        // given
        RateLimiter rateLimiter = RateLimiter.create(100);

        // when
        long startTime = ZonedDateTime.now().getSecond();
        IntStream.range(0, 120).forEach(i -> {
            rateLimiter.acquire();
            doSomeOperation();
        });
        long elapsedTimeSeconds = ZonedDateTime.now().getSecond() - startTime;
        System.out.println("Elapsed: " + elapsedTimeSeconds);
        // then
        assertTrue(elapsedTimeSeconds >= 3);
    }

    @Test
    public void testSmoothBursty() {
        RateLimiter r = RateLimiter.create(10);
        while (true) {
            System.out.println("get 1 tokens: " + r.acquire() + "s - " + ZonedDateTime.now().getSecond());
        }
        /**
         * output: Basically, the limiter is executed every 0.2s, which complies with the setting of releasing five tokens per second.
         * get 1 tokens: 0.0s
         * get 1 tokens: 0.182014s
         * get 1 tokens: 0.188464s
         * get 1 tokens: 0.198072s
         * get 1 tokens: 0.196048s
         * get 1 tokens: 0.197538s
         * get 1 tokens: 0.196049s
         */
    }

    @Test
    public void testSmoothwarmingUp() {
        RateLimiter r = RateLimiter.create(10, 15, TimeUnit.SECONDS);
        while (true)
        {
            System.out.println("get 1 tokens: " + r.acquire(1) + "s");
            System.out.println("get 1 tokens: " + r.acquire(1) + "s");
            System.out.println("get 1 tokens: " + r.acquire(1) + "s");
            System.out.println("get 1 tokens: " + r.acquire(1) + "s");
            System.out.println("get 1 tokens: " + r.acquire(1) + "s");
            System.out.println("get 1 tokens: " + r.acquire(1) + "s");
            System.out.println("get 1 tokens: " + r.acquire(1) + "s");
            System.out.println("get 1 tokens: " + r.acquire(1) + "s");
            System.out.println("get 1 tokens: " + r.acquire(1) + "s");
            System.out.println("get 1 tokens: " + r.acquire(1) + "s");
            System.out.println("end");
            /**
             * output:
             * get 1 tokens: 0.0s
             * get 1 tokens: 1.329289s
             * get 1 tokens: 0.994375s
             * get 1 tokens: 0.662888s  The total amount of time that has been taken for acquiring these three tokens is 3s.
             * end
             * get 1 tokens: 0.49764s  Tokens are acquired at the normal rate of two tokens/s.
             * get 1 tokens: 0.497828s
             * get 1 tokens: 0.49449s
             * get 1 tokens: 0.497522s
             */
        }
    }

    @Test
    public void testSomething() {

    }
}
