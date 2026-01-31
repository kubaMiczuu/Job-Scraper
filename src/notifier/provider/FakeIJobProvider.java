package notifier.provider;

import notifier.Job;

import java.util.ArrayList;
import java.util.List;

public class FakeIJobProvider implements IJobProvider {

    public List<Job> newJobs = new ArrayList<>();
    public List<Job> allJobs = new ArrayList<>();

    public FakeIJobProvider() {
        for (int i = 0; i < 10; i++) {
            newJobs.add(new Job("Job" + i, "Company" + i, "Location" + i, "https://job.com", "2026-01-26", "NEW"));
        }
    }

    @Override
    public List<Job> getNewJobs() {
        return newJobs;
    }

    @Override
    public List<Job> getAllJobs() {
        return newJobs;
    }

}