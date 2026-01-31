package notifier.notifier;

import notifier.Job;

import java.io.IOException;
import java.util.List;

public interface INotifier {
    public void send(List<Job> jobs) throws IOException, InterruptedException;
}
