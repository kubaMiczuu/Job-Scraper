package notifier;

import notifier.notifier.DiscordINotifier;
import notifier.notifier.EmailINotifier;
import notifier.notifier.INotifier;
import notifier.provider.FakeJobProvider;
import notifier.provider.IJobProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        logger.info("Starting Application...");

        IJobProvider provider = new FakeJobProvider();

        INotifier email = new EmailINotifier();
        INotifier discord = new DiscordINotifier();

        NotificationService service = new NotificationService(provider, List.of(email, discord));

        Scheduler scheduler = new Scheduler(service);
        scheduler.start();
    }
}