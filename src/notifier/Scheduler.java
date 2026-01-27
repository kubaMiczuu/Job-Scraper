package notifier;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import static java.util.concurrent.TimeUnit.*;

public class Scheduler {

    private final NotificationService service;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public Scheduler(NotificationService service) {
        this.service = service;
    }

    public void start() {
        Runnable sender = () -> {
            try {
                service.runNotificationCycle();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
        scheduler.scheduleAtFixedRate(sender, 10, 10, SECONDS);
    }
}
