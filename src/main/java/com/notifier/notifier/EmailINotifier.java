package com.notifier.notifier;

import com.notifier.model.Job;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Component
public class EmailINotifier implements INotifier {
    private static final Logger logger =  LoggerFactory.getLogger(EmailINotifier.class);

    private final String apiKey;
    private final String to;

    public EmailINotifier(@Value("${email.api.key}") String apiKey, @Value("${email.address}") String to) {
        this.apiKey = apiKey;
        this.to = to;

        logger.info("EmailNotifier initialized successfully");

    }

    @Override
    public void send(@NonNull List<Job> jobs) throws InterruptedException {
        logger.info("Sending email notifications to: {} with {} jobs", to, jobs.size());

        String url = "https://api.resend.com/emails";
        String from = "onboarding@resend.dev";
        String subject = "Job Offers";

        String html = getHTML(jobs).toString().replace("\"", "\\\"");

        String json = "{"
                + "\"from\": \"" + from + "\","
                + "\"to\": \"" + to + "\","
                + "\"subject\": \"" + subject + "\","
                + "\"html\": \"" + html + "\""
                + "}";

        try (HttpClient client = HttpClient.newHttpClient()) {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    logger.info("Email sent! to: {}", to);
                } else {
                    logger.info("Failed to send an Email! to: {}. Status: {}", to, response.statusCode());
                }
            } catch (IOException e) {
                logger.error("Failed to send HTTP Request", e);
            }
        }
    }

    private @NonNull StringBuilder getHTML(@NonNull List<Job> jobs) {
        StringBuilder html = new StringBuilder(
                "<table style=\"border-collapse:collapse; width:100%; font-family:Arial, sans-serif;\">"
                        + "<tr>"
                        + "<th style=\"background-color:#4CAF50; color:white; padding:8px; border:1px solid #ddd;\">Id</th>"
                        + "<th style=\"background-color:#4CAF50; color:white; padding:8px; border:1px solid #ddd;\">Title</th>"
                        + "<th style=\"background-color:#4CAF50; color:white; padding:8px; border:1px solid #ddd;\">Company</th>"
                        + "<th style=\"background-color:#4CAF50; color:white; padding:8px; border:1px solid #ddd;\">Location</th>"
                        + "<th style=\"background-color:#4CAF50; color:white; padding:8px; border:1px solid #ddd;\">URL</th>"
                        + "<th style=\"background-color:#4CAF50; color:white; padding:8px; border:1px solid #ddd;\">Date</th>"
                        + "</tr>"
        );

        for (int i = 0; i < jobs.size(); i++) {
            Job job = jobs.get(i);

            String rowColor = (i % 2 == 0) ? "#f9f9f9" : "#ffffff";

            html.append("<tr style=\"background-color:").append(rowColor).append(";\">");
            html.append("<td style=\"padding:8px; border:1px solid #ddd;\">")
                    .append(i + 1)
                    .append("</td>");
            html.append("<td style=\"padding:8px; border:1px solid #ddd;\">")
                    .append(job.title)
                    .append("</td>");
            html.append("<td style=\"padding:8px; border:1px solid #ddd;\">")
                    .append(job.company)
                    .append("</td>");
            html.append("<td style=\"padding:8px; border:1px solid #ddd;\">")
                    .append(job.location)
                    .append("</td>");
            html.append("<td style=\"padding:8px; border:1px solid #ddd;\">")
                    .append("<a href=\"").append(job.url).append("\">Link</a>")
                    .append("</td>");
            html.append("<td style=\"padding:8px; border:1px solid #ddd;\">")
                    .append(job.date)
                    .append("</td>");
            html.append("</tr>");
        }
        html.append("</table>");

        return html;
    }
}