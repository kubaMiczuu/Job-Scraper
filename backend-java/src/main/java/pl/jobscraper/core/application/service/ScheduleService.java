package pl.jobscraper.core.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Orchestrates the timing of notification cycles.
 * This service uses Spring's scheduling capabilities to trigger job notification
 * tasks based on configurable cron expressions.
 */
@Service
public class ScheduleService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    private final NotificationService notificationService;

    /**
     * Initializes the scheduler with the required notification service.
     * @param notificationService The service responsible for the actual notification logic.
     */
    public ScheduleService(NotificationService notificationService) {
        this.notificationService = notificationService;
        logger.info("Scheduler initialized successfully\n");
    }

    /**
     * Main production entry point for the notification cycle.
     * Triggered automatically based on the {@code notification.cron} property.
     * @throws RuntimeException if the notification cycle encounters an IO or interruption error.
     */
    @Scheduled(cron = "${notification.cron}")
    public void sendNotifications() {
        try {
            logger.info("Starting notification service");
            notificationService.runNotificationCycle();
            logger.info("Notification cycle completed\n");
        } catch (IOException | InterruptedException e) {
            logger.error("Notification cycle failed: ", e);
            throw new RuntimeException(e);
        }
    }

//    /**
//     * A secondary entry point used for testing purposes.
//     * Triggered based on the {@code notification.test.cron} property,
//     * allowing for more frequent or manual-like test runs.
//     */
//    @Scheduled(cron = "${notification.test.cron}")
//    public void testNotifications() {
//        sendNotifications();
//    }
}