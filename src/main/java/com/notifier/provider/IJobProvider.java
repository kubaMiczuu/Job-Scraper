package com.notifier.provider;

import com.notifier.model.Job;

import java.util.List;

public interface IJobProvider {
    List<Job> getNewJobs();
    List<Job> getAllJobs();
}
