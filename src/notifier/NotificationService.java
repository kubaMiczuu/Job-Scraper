package notifier;

import notifier.notifier.INotifier;
import notifier.provider.IJobProvider;

import java.io.IOException;

public class NotificationService {
    private final IJobProvider provider;
    private final INotifier emailINotifier;
    private final INotifier discordINotifier;

    public NotificationService(IJobProvider provider, INotifier emailINotifier, INotifier discordINotifier) {
        this.provider = provider;
        this.emailINotifier = emailINotifier;
        this.discordINotifier = discordINotifier;
    }

    public void runNotificationCycle() throws IOException, InterruptedException {
        emailINotifier.send(provider.getNewJobs());
        discordINotifier.send(provider.getNewJobs());
    }
}