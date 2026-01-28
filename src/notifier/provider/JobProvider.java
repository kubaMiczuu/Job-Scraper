package notifier.provider;

import notifier.Job;
import java.util.List;

public interface JobProvider {
    public List<Job> getNewJobs();
    public List<Job> getAllJobs();

}
