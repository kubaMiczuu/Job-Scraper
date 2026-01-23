package notifier;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class FakeJobProvider {

    private final List<Job> availableJobs;

    public FakeJobProvider() {
        this.availableJobs = new ArrayList<>();
        loadFakeJobs();
    }

    private void loadFakeJobs() {
        for (int i = 0; i < 10; i++) {
            availableJobs.add(new Job(i, "Job" + i, "Company" + i, "Location" + i, "Url" + i, new Date(2026, Calendar.JANUARY, i+1)));
        }
    }

    public List<Job> getJobs() {
        return availableJobs;
    }
}
