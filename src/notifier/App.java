package notifier;

import notifier.notifier.DiscordNotifier;
import notifier.notifier.EmailNotifier;
import notifier.notifier.Notifier;
import notifier.provider.FakeJobProvider;
import notifier.provider.JobProvider;

public class App {
    public static void main(String[] args) {
        JobProvider provider = new FakeJobProvider();

        Notifier email = new EmailNotifier();
        Notifier discord = new DiscordNotifier();

        NotificationService service = new NotificationService(provider, email, discord);

        Scheduler scheduler = new Scheduler(service);
        scheduler.start();
    }
}