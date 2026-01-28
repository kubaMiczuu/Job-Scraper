package notifier;

import notifier.notifier.Notifier;
import notifier.provider.JobProvider;

import java.io.IOException;

public class NotificationService {
    private final JobProvider provider;
    private final Notifier emailNotifier;
    private final Notifier discordNotifier;

    public NotificationService(JobProvider provider, Notifier emailNotifier, Notifier discordNotifier) {
        this.provider = provider;
        this.emailNotifier = emailNotifier;
        this.discordNotifier = discordNotifier;
    }

    public void runNotificationCycle() throws IOException, InterruptedException {
        emailNotifier.send(provider.getNewJobs());
        discordNotifier.send(provider.getNewJobs());
    }
}