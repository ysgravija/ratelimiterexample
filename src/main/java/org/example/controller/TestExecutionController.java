package org.example.controller;

import org.example.model.Test;
import org.example.service.TestExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

/**
 * @author yeesheng on 15/08/2021
 * @project RateLimiterExample
 */
@RestController
public class TestExecutionController {
    @Autowired
    private TestExecutionService testsService;

    @RequestMapping(method = RequestMethod.GET, value = "/tests")
    public CompletableFuture<List<Test>> getTestList() {
        try {
            return testsService.getTestExecutionList();
        } catch (Exception e) {
            return null;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/tests/pps")
    public double getAvailablePps() {
        return testsService.getAvailablePps();
    }

    @RequestMapping(method = RequestMethod.GET, value = "tests/{id}")
    public Test getTest(@PathVariable("id") String id) {
        return testsService.getTestExecution(id);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/tests")
    public void addTest(@RequestBody Test test) {
        testsService.addTest(test);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/tests/start")
    public ResponseEntity<Object> startTest() {
        try {
            testsService.startAll();
        } catch (Exception e) {

            return new ResponseEntity<Object>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<Object>(HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/tests/start/{id}")
    public ResponseEntity<Object> startTest(@PathVariable("id") String id) {
        try {
            testsService.startTest(id);
        } catch (Exception e) {
            if (e instanceof NoSuchElementException) {
                return new ResponseEntity<Object>(HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<Object>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<Object>(HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/tests/stop/{id}")
    public ResponseEntity<Object> stopTest(@PathVariable("id") String id) {
        try {
            testsService.stopTest(id);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/tests/{id}")
    public void updateTest(@RequestBody Test test, @PathVariable("id") String id) {
        testsService.updateTest(test, id);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/tests/{id}")
    public void deleteTest(@PathVariable("id") String id) {
        testsService.deleteTest(id);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/tests/reset")
    public ResponseEntity<Object> resetTest() {
        try {
            testsService.resetTest();
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
