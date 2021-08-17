package org.example.limiter;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

/**
 * @author yeesheng on 17/08/2021
 * @project RateLimiterExample
 */
@Slf4j
public class PasRateLimiter {

    private Timer timer;
    private RateLimiter rateLimiter;
    private double maxPermitsPerSecond;
    private boolean enableRampUpAndDown;
    private long runDurationInMinutes;
    private long rampUpDownDurationInMinutes;
    private double currentPermitsPerSecond;
    private long rampInterval;

    public PasRateLimiter(double permitPerSecond, boolean enableRampUpAndDown, long runDurationInMinutes, long rampUpDownDurationInMinutes, long rampInterval) {
        this.maxPermitsPerSecond = permitPerSecond;
        this.enableRampUpAndDown = enableRampUpAndDown;
        this.runDurationInMinutes = runDurationInMinutes;
        this.rampUpDownDurationInMinutes = rampUpDownDurationInMinutes;
        this.currentPermitsPerSecond = 1L;
        this.rampInterval = rampInterval;

        initializeSoakTest();
    }

    private void initializeSoakTest() {
        log.info("Initialize Soak Test with enable soak test {}", enableRampUpAndDown);
        if (enableRampUpAndDown) {
            this.rateLimiter = RateLimiter.create(currentPermitsPerSecond, 1L, TimeUnit.SECONDS);
            startSoakTimer();
        } else {
            this.rateLimiter = RateLimiter.create(maxPermitsPerSecond, 1L, TimeUnit.SECONDS);
        }
    }

    private void startSoakTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(
                new PasSoakTask(rateLimiter, runDurationInMinutes, maxPermitsPerSecond, rampUpDownDurationInMinutes, rampInterval),
                0,
                rampInterval * 1000L);
    }

    public void stopSoakTimer() {
        log.info("Stop Soak Test Timer!");
        timer.cancel();
    }

    public RateLimiter getRateLimiter() {
        return rateLimiter;
    }
}
