package com.notifier.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ScheduleService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    private final NotificationService notificationService;

    public ScheduleService(NotificationService notificationService) {
        this.notificationService = notificationService;
        logger.info("Scheduler initialized successfully\n");
    }

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

    @Scheduled(cron = "${notification.test.cron}")
    public void testNotifications() {
        sendNotifications();
    }
}