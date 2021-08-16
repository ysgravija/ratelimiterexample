package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @author yeesheng on 10/08/2021
 * @project RateLimiterExample
 */
@SpringBootApplication
@EnableJms
public class Application {

    @Autowired
    private JmsTemplate jmsTemplate;

    private static final int MAX_RUN_TIME = 2 * 60;

    public static void main(String[] args) throws Exception {

        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
//        ThrottleController controller = new ThrottleController(context);

//        ZonedDateTime startDate = ZonedDateTime.now();
//        while(isTimeExpired(startDate)) {
//            controller.sendMessage("Hello from application! ");
//        }
//        System.out.println("Producer process done!");
    }

    private static boolean isTimeExpired(ZonedDateTime startDate) {
        ZonedDateTime currentDate = ZonedDateTime.now();
        if ((Duration.between(startDate, currentDate).getSeconds()) > MAX_RUN_TIME ) {
            return false;
        } else {
            return true;
        }
    }
}
