package org.example.service;

import org.example.model.Test;
import org.example.execution.TestExecutionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author yeesheng on 15/08/2021
 * @project RateLimiterExample
 */
@Service
public class TestExecutionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestExecutionService.class);

    // Dummy value to represent database
    private List<Test> testList = new ArrayList<>(Arrays.asList(
            new Test("1", "Bank A", 15, false),
            new Test("2", "Bank B", 25, false),
            new Test("3", "Bank C", 10, false),
            new Test("4", "Bank D", 28, false),
            new Test("5", "Bank E", 22, false)
    ));

    @Autowired
    private TestExecutionManager testExecutionManager;

    @Async("restExecutor")
    public CompletableFuture<List<Test>> getTestExecutionList() throws Exception {
        LOGGER.info("Get test execution list.");
        return CompletableFuture.completedFuture(testList);
    }

    public Test getTestExecution(String id) {
        return testList.stream().filter(topic -> topic.getId().equals(id)).findFirst().get();
    }

    public void addTest(Test test) {
        testList.add(test);
    }

    public void updateTest(Test test, String id) {
        int counter = 0;
        for (Test t : testList) {
            if (t.getId().equals(id)) {
                testList.set(counter, test);
            }
            counter++;
        }
    }

    public void deleteTest(String id) {
        testList.removeIf(test -> test.getId().equals(id));
    }

    public double getAvailablePps() {
        return testExecutionManager.getAvailablePermits();
    }

    public void startTest(String id) throws Exception {
        Test test = testList.stream().filter(topic -> topic.getId().equals(id)).findFirst().get();
        testExecutionManager.startTest(test);
    }

    public void stopTest(String id) throws Exception {
        synchronized (this) {
            testExecutionManager.stopTest(id);
        }
    }

    public void resetTest() throws Exception {
        testExecutionManager.stopAll();
    }

    public void startAll() throws Exception {
        testExecutionManager.startAll(testList);
    }

}
