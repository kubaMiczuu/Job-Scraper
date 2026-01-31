package notifier.notifier;

import notifier.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Properties;

public class DiscordINotifier implements INotifier {
    private static final Logger logger =  LoggerFactory.getLogger(DiscordINotifier.class);

    private final String discordWebhookUrl;

    public DiscordINotifier() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                logger.error("Couldn't find a config.properties");
                throw new RuntimeException("Couldn't find a config.properties");
            }
            props.load(input);
        } catch (IOException e) {
            logger.error("Loading config.properties failed", e);
            throw new RuntimeException("Loading config.properties failed", e);
        }
        this.discordWebhookUrl = props.getProperty("DISCORD_WEBHOOK_URL");

        logger.info("DiscordNotifier initialized successfully");
    }

    @Override
    public void send(List<Job> jobs) throws IOException, InterruptedException {
        logger.info("Sending Discord notifications with {} jobs", jobs.size());

        String jobsAsJson = formatJobsAsEmbed(jobs);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(discordWebhookUrl))
                .header("Content-Type","application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jobsAsJson))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                logger.info("Discord message sent!");
            } else {
                logger.info("Failed to send a Discord message! Status: {}",response.statusCode());
            }
        } catch (IOException e) {
            logger.error("Failed to send HTTP Request", e);
        }
    }

    public String formatJobsAsEmbed(List<Job> jobs) {
        StringBuilder json = new StringBuilder();
        json.append("{\"content\":\"New jobs here!!\",");
        json.append("\"embeds\":[{");
        json.append("\"title\":\"New Job Offers\",");
        json.append("\"description\":\"Found ");
        json.append(jobs.size());
        json.append(" new offers!\",");
        json.append("\"color\":3066993,");

        json.append("\"fields\":[");
        for(int i = 0; i < jobs.size(); i++) {
            Job job = jobs.get(i);

            json.append("{");

            json.append("\"name\":\"");
            json.append(escapeJson(job.title));
            json.append("\",");

            json.append("\"value\":\"");
            json.append("\uD83C\uDFE2 ").append(escapeJson(job.company)).append("\\n");
            json.append("\uD83D\uDCCD ").append(escapeJson(job.location)).append("\\n");
            json.append("⏰ ").append(escapeJson(job.date)).append("\\n");
            json.append("\uD83D\uDD17 ").append("[Visit!](").append(escapeJson(job.url)).append(")");
            json.append("\",");

            json.append("\"inline\":false");

            json.append("}");

            if(i < jobs.size() - 1) json.append(", ");
        }

        json.append("]");
        json.append("}]");
        json.append("}");

        return  json.toString();
    }

    private String escapeJson(String text) {
        if (text == null) return "";

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
