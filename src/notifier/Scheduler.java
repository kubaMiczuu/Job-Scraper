package notifier;

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
        Runnable sender = () -> service.runNotificationCycle();
        scheduler.scheduleAtFixedRate(sender, 10, 10, SECONDS);
    }
}
