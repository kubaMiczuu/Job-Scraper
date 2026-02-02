package com.notifier.notifier;

import com.notifier.model.Job;

import java.io.IOException;
import java.util.List;

public interface INotifier {
    void send(List<Job> jobs) throws IOException, InterruptedException;
}
