package org.example.execution;

import org.example.model.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author yeesheng on 15/08/2021
 * @project RateLimiterExample
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class TestExecutionManager implements DisposableBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestExecutionManager.class);
    private ThreadPoolExecutor taskExecutor;
    private Map<String, TestExecutionRunnable> testExecutionTaskMap;
    private volatile Double consumedPermits = 0.0;

    @Value("${throttle.emission.queue}")
    private String destinationQueue;

    @Value("${throttle.max.pps}")
    private Double maxPermits;

    @Value("${throttle.ramp.up.period}")
    private Long rampUpPeriod;

    @Autowired
    private JmsTemplate jmsTemplate;

    public TestExecutionManager() {
        this.testExecutionTaskMap = new ConcurrentHashMap<String, TestExecutionRunnable>();
        this.taskExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(100);
    }

    public void startTest(Test test) throws Exception {
        if (testExecutionTaskMap.containsKey(test.getId())) {
            LOGGER.error("Test execution ID={} exist! Ignore test execution request.", test.getId());
            throw new Exception(String.format("Test execution ID=%s exists!", test.getId()));
        }

        if (!isCapacityAvailable(test.getPermitPerSecond())) {
            LOGGER.error("No available capacity for requested {} permits. Remaining available permits is {}", test.getPermitPerSecond(), (maxPermits - consumedPermits));
            throw new Exception(String.format("No available capacity for requested %d permits", test.getPermitPerSecond()));
        }

        TestExecutionRunnable task = new TestExecutionRunnable(
                test.getName(), destinationQueue, test.getPermitPerSecond(), rampUpPeriod, jmsTemplate);

        taskExecutor.execute(task);
        testExecutionTaskMap.put(test.getId(), task);
        addPermitConsumption(task.getRate());
    }

    public void stopTest(String id) throws Exception {
        if (testExecutionTaskMap.get(id) != null) {
            TestExecutionRunnable task = testExecutionTaskMap.get(id);
            task.stop();
            testExecutionTaskMap.remove(id);
            removePermitConsumption(task.getRate());
        } else {
            LOGGER.error("Test execution with ID {} not found!", id);
            throw new Exception(String.format("Test execution with ID %s not found!", id));
        }
    }

    public double getAvailablePermits() {
        return (maxPermits - consumedPermits);
    }

    public void startAll(List<Test> testList) throws Exception {
        if (testExecutionTaskMap.isEmpty()) {
            for (Test t : testList) {
                startTest(t);
            }
        } else {
            LOGGER.warn("Test execution exists! Please stop existing test executions.");
        }
    }

    public void stopAll() {
        for (TestExecutionRunnable r : testExecutionTaskMap.values()) {
            r.stop();
            removePermitConsumption(r.getRate());
        }
        testExecutionTaskMap.clear();
    }

    private synchronized boolean isCapacityAvailable(int requestedPps) {
        return (!((consumedPermits + requestedPps) > maxPermits));
    }

    private synchronized void addPermitConsumption(double permits) {
        consumedPermits += permits;
    }

    private synchronized void removePermitConsumption(double permits) {
        consumedPermits -= permits;
    }

    public void shutDown() throws InterruptedException {
        stopAll();
        taskExecutor.shutdown();
        taskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    @Override
    public void destroy() throws Exception {
        shutDown();
    }
}
