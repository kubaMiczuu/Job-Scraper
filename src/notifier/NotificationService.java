package notifier;

import notifier.notifier.EmailNotifier;
import notifier.provider.FakeJobProvider;
import java.io.IOException;

public class NotificationService {
    private final FakeJobProvider provider;
    private final EmailNotifier emailNotifier;

    public NotificationService(FakeJobProvider provider, EmailNotifier emailNotifier) {
        this.provider = provider;
        this.emailNotifier = emailNotifier;
    }

    public void runNotificationCycle() throws IOException, InterruptedException {
        emailNotifier.send(provider.getAllJobs());
    }
}
