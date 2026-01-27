package notifier.provider;

import notifier.Job;

import java.util.ArrayList;
import java.util.List;

public class FakeJobProvider {

    public List<Job> allJobs = new ArrayList<>();
    public List<Job> newJobs = new ArrayList<>();

    public FakeJobProvider() {
        for (int i = 0; i < 10; i++) {
            allJobs.add(new Job("Job" + i, "Company" + i, "Location" + i, "Url" + i, "2026-01-26T11:00:03.152958200Z", "NEW"));
        }
        newJobs.add(allJobs.getLast());
    }

    public List<Job> getAllJobs() {
        return allJobs;
    }

    public List<Job> getNewJobs() {
        return newJobs;
    }


}