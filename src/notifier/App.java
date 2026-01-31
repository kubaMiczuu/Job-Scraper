package notifier;

import notifier.notifier.DiscordINotifier;
import notifier.notifier.EmailINotifier;
import notifier.notifier.INotifier;
import notifier.provider.FakeIJobProvider;
import notifier.provider.IJobProvider;

public class App {
    public static void main(String[] args) {
        IJobProvider provider = new FakeIJobProvider();

        INotifier email = new EmailINotifier();
        INotifier discord = new DiscordINotifier();

        NotificationService service = new NotificationService(provider, email, discord);

        Scheduler scheduler = new Scheduler(service);
        scheduler.start();
    }
}