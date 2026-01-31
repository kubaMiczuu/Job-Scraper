package notifier;

import notifier.notifier.INotifier;
import notifier.provider.IJobProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final IJobProvider provider;
    private final List<INotifier> notifiers;

    public NotificationService(IJobProvider provider, List<INotifier> notifiers) {
        this.provider = provider;
        this.notifiers = notifiers;
        logger.info("NotificationService initialized successfully with {} notifiers", notifiers.size());
    }

    public void runNotificationCycle() throws IOException, InterruptedException {
        List<Job> jobs = provider.getNewJobs();

        if(jobs.isEmpty()) {
            logger.info("No new jobs found, skipping notifications");
            return;
        }

        logger.info("Found {} new jobs", jobs.size());

        for(INotifier notifier : notifiers) {
            notifier.send(jobs);
        }
    }
}