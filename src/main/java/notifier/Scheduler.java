package notifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import static java.util.concurrent.TimeUnit.*;

public class Scheduler {
    private static final Logger logger = LoggerFactory.getLogger(Scheduler.class);

    private final NotificationService service;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public Scheduler(NotificationService service) {
        this.service = service;
        logger.info("Scheduler initialized successfully");
    }

    public void start() {
        Runnable sender = () -> {
            try {
                logger.info("Starting notification service");
                service.runNotificationCycle();
                logger.info("Notification cycle completed");
            } catch (IOException | InterruptedException e) {
                logger.error("Notification cycle failed: ", e);
                throw new RuntimeException(e);
            }
        };
        scheduler.scheduleAtFixedRate(sender, 10, 10, SECONDS);
    }
}