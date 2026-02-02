package com.notifier.service;

import com.notifier.model.Job;
import com.notifier.notifier.INotifier;
import com.notifier.provider.IJobProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final IJobProvider provider;
    private final List<INotifier> notifiers;

    @Autowired
    public NotificationService(IJobProvider provider, List<INotifier> notifiers) {
        this.provider = provider;
        this.notifiers = notifiers;
        logger.info("NotificationService initialized successfully with {} notifiers", notifiers.size());
    }

    public void runNotificationCycle() throws IOException, InterruptedException {
        List<Job> jobs = provider.getNewJobs();

        if(jobs.isEmpty()) {
            logger.info("No new jobs found, skipping notifications");
            return;
        }

        logger.info("Found {} new jobs", jobs.size());

        for(INotifier notifier : notifiers) {
            notifier.send(jobs);
        }
    }
}