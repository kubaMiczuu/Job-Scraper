package pl.jobscraper.core.infrastructure.notifier;

import pl.jobscraper.core.domain.model.Job;
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

/**
 * An email-based implementation of the {@link INotifier} interface.
 * This class formats job listings into an HTML table and dispatches them
 * using the Resend API via HTTP POST requests.
 */
@Component
public class EmailINotifier implements INotifier {
    private static final Logger logger =  LoggerFactory.getLogger(EmailINotifier.class);

    private final String apiKey;
    private final String to;

    /**
     * Constructs the Email notifier with required API credentials and recipient.
     * @param apiKey The API key for the Resend service, injected from properties.
     * @param to     The recipient email address.
     */
    public EmailINotifier(@Value("${email.api.key}") String apiKey, @Value("${email.address}") String to) {
        this.apiKey = apiKey;
        this.to = to;

        logger.info("EmailNotifier initialized successfully");
    }

    /**
     * Sends the list of jobs as an HTML-formatted email.
     * Wraps the job data in a JSON payload compatible with the Resend API.
     *
     * @param jobs A non-null list of {@link Job} objects to be sent.
     * @throws InterruptedException If the HTTP request is interrupted.
     */
    @Override
    public void send(@NonNull List<Job> jobs) throws InterruptedException {
        logger.info("Sending email notifications to: {} with {} jobs", to, jobs.size());

        String url = "https://api.resend.com/emails";
        String from = "onboarding@resend.dev";
        String subject = "Job Offers";

        // Preparing the HTML content by escaping quotes for JSON compatibility
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

    /**
     * Generates a styled HTML table containing job details.
     * Features alternating row colors and a professional green header.
     *
     * @param jobs The list of jobs to render.
     * @return A {@link StringBuilder} containing the complete HTML markup.
     */
    private @NonNull StringBuilder getHTML(@NonNull List<Job> jobs) {
        StringBuilder html = new StringBuilder(
                "<table style=\"border-collapse:collapse; width:100%; font-family:Arial, sans-serif;\">"
                        + "<tr>"
                        + "<th style=\"background-color:#4CAF50; color:white; padding:8px; border:1px solid #ddd;\">Id</th>"
                        + "<th style=\"background-color:#4CAF50; color:white; padding:8px; border:1px solid #ddd;\">Title</th>"
                        + "<th style=\"background-color:#4CAF50; color:white; padding:8px; border:1px solid #ddd;\">Company</th>"
                        + "<th style=\"background-color:#4CAF50; color:white; padding:8px; border:1px solid #ddd;\">Location</th>"
                        + "<th style=\"background-color:#4CAF50; color:white; padding:8px; border:1px solid #ddd;\">Employment type</th>"
                        + "<th style=\"background-color:#4CAF50; color:white; padding:8px; border:1px solid #ddd;\">Seniority</th>"
                        + "<th style=\"background-color:#4CAF50; color:white; padding:8px; border:1px solid #ddd;\">Salary</th>"
                        + "<th style=\"background-color:#4CAF50; color:white; padding:8px; border:1px solid #ddd;\">URL</th>"
                        + "<th style=\"background-color:#4CAF50; color:white; padding:8px; border:1px solid #ddd;\">Source</th>"
                        + "<th style=\"background-color:#4CAF50; color:white; padding:8px; border:1px solid #ddd;\">Published date</th>"
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
                    .append(job.getTitle())
                    .append("</td>");

            html.append("<td style=\"padding:8px; border:1px solid #ddd;\">")
                    .append(job.getCompany())
                    .append("</td>");

            html.append("<td style=\"padding:8px; border:1px solid #ddd;\">")
                    .append(job.getLocation())
                    .append("</td>");

            html.append("<td style=\"padding:8px; border:1px solid #ddd;\">");
            if(job.getEmploymentType() != null) html.append(job.getEmploymentType());
            else html.append("N/A");
            html.append("</td>");

            html.append("<td style=\"padding:8px; border:1px solid #ddd;\">");
            if(job.getSeniority() != null) html.append(job.getSeniority());
            else html.append("N/A");
            html.append("</td>");

            html.append("<td style=\"padding:8px; border:1px solid #ddd;\">");
            if(job.getSalary() != null) html.append(job.getSalary());
            else html.append("N/A");
            html.append("</td>");

            html.append("<td style=\"padding:8px; border:1px solid #ddd;\">")
                    .append("<a href=\"").append(job.getUrl()).append("\">Link</a>")
                    .append("</td>");

            html.append("<td style=\"padding:8px; border:1px solid #ddd;\">");
            if(job.getSource() != null) html.append(job.getSource());
            else html.append("N/A");
            html.append("</td>");

            html.append("<td style=\"padding:8px; border:1px solid #ddd;\">")
                    .append(job.getPublishedDate())
                    .append("</td>");

            html.append("</tr>");
        }
        html.append("</table>");

        return html;
    }
}