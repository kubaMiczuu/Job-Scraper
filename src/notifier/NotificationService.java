package notifier;

import notifier.notifier.EmailNotifier;
import notifier.provider.FakeJobProvider;

import java.util.List;

public class NotificationService {
    private final FakeJobProvider provider;
    private final EmailNotifier emailNotifier;

    public NotificationService(FakeJobProvider provider, EmailNotifier emailNotifier) {
        this.provider = provider;
        this.emailNotifier = emailNotifier;
    }

    public void runNotificationCycle() {
        List<Job> allJobs = provider.getAllJobs();
        List<Job> newJobs = provider.getNewJobs();

        System.out.println("All available jobs:");
        for(int i=0;i<10;i++){
            Job currentJob = allJobs.get(i);
            System.out.print(currentJob.title+", ");
            System.out.print(currentJob.company+", ");
            System.out.print(currentJob.location+", ");
            System.out.print(currentJob.date+", ");
            System.out.print(currentJob.jobState+"\n");
        }
        System.out.println("\nNew jobs:");
        System.out.print(newJobs.getFirst().title+", ");
        System.out.print(newJobs.getFirst().company+", ");
        System.out.print(newJobs.getFirst().location+", ");
        System.out.print(newJobs.getFirst().date+", ");
        System.out.print(newJobs.getFirst().jobState+"\n");
    }
}
