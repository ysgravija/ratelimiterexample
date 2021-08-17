package org.example.limiter;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.TimerTask;

/**
 * @author yeesheng on 17/08/2021
 * @project RateLimiterExample
 */
@Slf4j
public class PasSoakTask extends TimerTask {

    private RateLimiter rateLimiter;
    private ZonedDateTime startPeriod;
    private ZonedDateTime endPeriod;
    private ZonedDateTime rampUpThreshold;
    private ZonedDateTime rampDownThreshold;
    private double maxPermitsPerSecond;
    private double currentPermitsPerSecond;
    private long timerIntervalInSecond;

    public PasSoakTask(RateLimiter rateLimiter, long runDurationInMinutes, double maxPermitsPerSecond, long rampUpDownDurationInMinutes, long timerInterval) {
        this.rateLimiter = rateLimiter;
        this.startPeriod = ZonedDateTime.now();
        this.endPeriod = startPeriod.plusMinutes(runDurationInMinutes);
        this.rampUpThreshold = startPeriod.plusMinutes(rampUpDownDurationInMinutes);
        this.rampDownThreshold = startPeriod.plusMinutes((runDurationInMinutes - rampUpDownDurationInMinutes));
        this.maxPermitsPerSecond = maxPermitsPerSecond;
        this.currentPermitsPerSecond = 1;
        this.timerIntervalInSecond = timerInterval;
    }

    @Override
    public void run() {
        ZonedDateTime currentTime = ZonedDateTime.now();

        if (startPeriod.toEpochSecond() <= currentTime.toEpochSecond() && currentTime.toEpochSecond() <= rampUpThreshold.toEpochSecond()) {
            currentPermitsPerSecond = computeRampUpLimit(
                    currentTime, rampUpThreshold, currentPermitsPerSecond, maxPermitsPerSecond, timerIntervalInSecond
            );

            PasSoakTask.setRateIfRequired(rateLimiter, currentPermitsPerSecond);
            log.debug("Ramping up speed - Compute and set rate to {}", currentPermitsPerSecond);
        } else if (rampDownThreshold.toEpochSecond() <= currentTime.toEpochSecond() && currentTime.toEpochSecond() <= endPeriod.toEpochSecond()) {
            currentPermitsPerSecond = computeRampDownLimit(
                    currentTime, endPeriod, currentPermitsPerSecond, 1, timerIntervalInSecond
            );

            PasSoakTask.setRateIfRequired(rateLimiter, currentPermitsPerSecond);
            log.debug("Ramping down - Compute and set rate to {}", currentPermitsPerSecond);
        } else {
            log.debug("PPS is at peak with constant pps {}", maxPermitsPerSecond);
        }

        if (currentTime.toEpochSecond() >= endPeriod.toEpochSecond()) {
            log.info("Timer has expired. Stop timer operation!");
            this.cancel();
        }
    }

    private static void setRateIfRequired(RateLimiter rateLimiter, double rate) {
        if (Math.round(rateLimiter.getRate()) != rate) {
            log.info("Set current rate {} to rate {}", Math.round(rateLimiter.getRate()), rate);
            rateLimiter.setRate(rate);
        }
    }
    private static double computeRampDownLimit(ZonedDateTime currentTime, ZonedDateTime endTime,
                                               double currentPermits, double minPermitsPerSecond,
                                               long timerInterval) {
        // Ramping down assuming PPS is already at peak, maybe some checks for negative scenario?
        double computedRate = (currentPermits - minPermitsPerSecond) /
                ((endTime.toEpochSecond() - currentTime.toEpochSecond()) / timerInterval);
        double decrementRate = Math.round(computedRate > 0 ? computedRate : 0);
        double setRate = currentPermits;
        if ( currentPermits > minPermitsPerSecond) {
            setRate -= decrementRate;
        } else {
            setRate = minPermitsPerSecond;
        }
        return setRate;
    }

    private static double computeRampUpLimit(ZonedDateTime currentTime, ZonedDateTime rampUpThreshold,
                                             double currentPermits, double maxPermitsPerSecond,
                                             long timerInterval) {
        double computedRate = (maxPermitsPerSecond - currentPermits) /
                        ((rampUpThreshold.plusSeconds(1).toEpochSecond() - currentTime.toEpochSecond()) / timerInterval);
        double incrementRate = Math.round((computedRate > 0 ? computedRate : 0));

//        log.info("Time difference: {}", (rampUpThreshold.plusSeconds(1).toEpochSecond() - currentTime.toEpochSecond()));
//        log.info("Computed rate: {}", Math.round(computedRate));
//        log.info("Ramp up time: {}", rampUpThreshold.plusSeconds(1).toEpochSecond());
//        log.info("Current up time: {}", currentTime.toEpochSecond());
//        log.info("Increase: {}", Math.round(incrementRate));
        double setRate = currentPermits;
        if (currentPermits < maxPermitsPerSecond) {
            setRate += incrementRate;
        } else {
            setRate = maxPermitsPerSecond;
        }
        return setRate;
    }
}
