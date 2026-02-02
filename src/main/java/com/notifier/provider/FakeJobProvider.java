package com.notifier.provider;

import com.notifier.model.Job;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link IJobProvider} that provides mock job data.
 * Used primarily for testing purposes or during development when the real
 * data source is unavailable.
 */
@Component
public class FakeJobProvider implements IJobProvider {

    /** List containing newly discovered mock jobs. */
    public List<Job> newJobs = new ArrayList<>();

    /** List containing all historical mock jobs. */
    public List<Job> allJobs = new ArrayList<>();

    /**
     * Constructs the provider and populates {@link #newJobs} with dummy data.
     * Generates 10 sample jobs and manually sets additional attributes (seniority,
     * salary, etc.) for specific items to simulate various data scenarios.
     */
    public FakeJobProvider() {
        for (int i = 0; i < 10; i++) {
            newJobs.add(new Job("Job" + i, "Company" + i, "Location" + i, "https://job.com", "2026-01-26"));
        }
        // Simulating optional fields for testing UI/JSON output
        newJobs.get(2).setSeniority("MID");
        newJobs.get(5).setSalary("6400");
        newJobs.get(8).setEmploymentType("Remote");
        newJobs.get(9).setSource("https://www.pracuj.pl");
    }

    /**
     * Returns a list of mock jobs generated during initialization.
     * @return A list of newly created {@link Job} objects.
     */
    @Override
    public List<Job> getNewJobs() {
        return newJobs;
    }

    /**
     * Returns a list of all mock jobs generated during initialization.
     * @return A list of all available mock {@link Job} objects.
     */
    @Override
    public List<Job> getAllJobs() {
        return allJobs;
    }

}