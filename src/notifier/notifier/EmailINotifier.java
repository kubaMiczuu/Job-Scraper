package notifier.notifier;

import notifier.Job;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Properties;

public class EmailINotifier implements INotifier {
    private final String apiKey;
    private final String to;

    public EmailINotifier() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("resources/config.properties")) {
            if (input == null) {
                throw new RuntimeException("Couldn't find a config.properties");
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Loading config.properties failed", e);
        }

        this.apiKey = props.getProperty("EMAIL_API_KEY");
        this.to = props.getProperty("EMAIL_ADDRESS");
    }

    @Override
    public void send(List<Job> jobs) throws IOException, InterruptedException {
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

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer "+apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            System.out.print("Email sent! ");
        } else {
            System.out.print("Failed to send an Email! ");
        }
        System.out.print("Status: "+response.statusCode()+"\n");
    }

    private StringBuilder getHTML(List<Job> jobs) {
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