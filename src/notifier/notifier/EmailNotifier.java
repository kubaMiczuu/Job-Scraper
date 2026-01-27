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

public class EmailNotifier {
    String apiKey;
    String url = "https://api.resend.com/emails";
    String from = "onboarding@resend.dev";
    String to;
    String subject = "Job Offers";

    public EmailNotifier() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("resources/config.properties")) {
            if (input == null) { throw new RuntimeException("Nie znaleziono pliku config.properties"); }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Nie mogę wczytać config.properties", e);
        }
        this.apiKey = props.getProperty("EMAIL_API_KEY");
        this.to = props.getProperty("EMAIL_ADDRESS");
    }

    public void send(List<Job> jobs) throws IOException, InterruptedException {
        StringBuilder html = getString(jobs);

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

        client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Email Sent");
    }

    private StringBuilder getString(List<Job> jobs) {
        StringBuilder html = new StringBuilder(
                "<table><th>"
                +"<td>Id</td>"
                +"<td>Title</td>"
                +"<td>Company</td>"
                +"<td>Location</td>"
                +"<td>URL</td>"
                +"<td>Date</td></th>"
        );
        for(int i = 0; i < jobs.size(); i++) {
            Job job = jobs.get(i);
            html.append("<tr>");
            html.append("<td>").append(i+1).append("</td>");
            html.append("<td>").append(job.title).append("</td>");
            html.append("<td>").append(job.company).append("</td>");
            html.append("<td>").append(job.location).append("</td>");
            html.append("<td>").append(job.url).append("</td>");
            html.append("<td>").append(job.date).append("</td>");
            html.append("</tr></table>");
        }
        return html;
    }
}
