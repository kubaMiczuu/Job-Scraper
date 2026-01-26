package notifier;

import notifier.notifier.EmailNotifier;
import notifier.provider.FakeJobProvider;

public class App {
    public static void main(String[] args) {
        FakeJobProvider provider = new FakeJobProvider();

        EmailNotifier email = new EmailNotifier();

        NotificationService service = new NotificationService(provider, email);

        Scheduler scheduler = new Scheduler(service);
        scheduler.start();
    }
}
