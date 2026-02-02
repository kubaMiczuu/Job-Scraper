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
 * A Discord-based implementation of the {@link INotifier} interface.
 * Dispatches job notifications to a Discord channel using Webhooks.
 * The messages are formatted as Rich Embeds with emojis and structured fields.
 */
@Component
public class DiscordINotifier implements INotifier {
    private static final Logger logger =  LoggerFactory.getLogger(DiscordINotifier.class);

    private final String discordWebhookUrl;

    /**
     * Initializes the Discord notifier with a target Webhook URL.
     * @param discordWebhookUrl The URL of the Discord webhook, injected from properties.
     */
    public DiscordINotifier(@Value("${discord.webhook.api}") String discordWebhookUrl) {
        this.discordWebhookUrl = discordWebhookUrl;

        logger.info("DiscordNotifier initialized successfully");
    }

    /**
     * Sends the job listings to Discord.
     * Converts the list of jobs into a JSON payload structure required by Discord Webhooks.
     *
     * @param jobs A non-null list of {@link Job} objects.
     * @throws InterruptedException If the HTTP communication is interrupted.
     */
    @Override
    public void send(@NonNull List<Job> jobs) throws InterruptedException {
        logger.info("Sending Discord notifications with {} jobs", jobs.size());

        String jobsAsJson = formatJobsAsEmbed(jobs);

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(discordWebhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jobsAsJson))
                    .build();

            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    logger.info("Discord message sent!");
                } else {
                    logger.info("Failed to send a Discord message! Status: {}", response.statusCode());
                }
            } catch (IOException e) {
                logger.error("Failed to send HTTP Request", e);
            }
        }
    }

    /**
     * Transforms a list of {@link Job} objects into a Discord Embed JSON string.
     * Uses emojis for better visualization and escapes special characters to ensure valid JSON.
     *
     * @param jobs The list of jobs to be formatted.
     * @return A string representing the JSON payload for Discord.
     */
    public String formatJobsAsEmbed(@NonNull List<Job> jobs) {
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
            json.append(escapeJson(job.getTitle()));
            json.append("\",");

            json.append("\"value\":\"");
            json.append("\uD83C\uDFE2 ").append(escapeJson(job.getCompany())).append("\\n");
            json.append("\uD83D\uDCCD ").append(escapeJson(job.getLocation())).append("\\n");
            if(job.getSeniority() != null) json.append("\uD83C\uDFC5 ").append(escapeJson(job.getSeniority())).append("\\n");
            if(job.getEmploymentType() != null) json.append("\uD83D\uDCBC ").append(escapeJson(job.getEmploymentType())).append("\\n");
            if(job.getSalary() != null) json.append("\uD83D\uDCB5 ").append(escapeJson(job.getSalary())).append("\\n");
            json.append("\uD83D\uDCC5 ").append(escapeJson(job.getPublishedDate())).append("\\n");
            if(job.getSource() != null) json.append("\uD83D\uDD0E ").append(escapeJson(job.getSource())).append("\\n");
            json.append("\uD83D\uDD17 ").append("[Visit!](").append(escapeJson(job.getUrl())).append(")");
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

    /**
     * Escapes special characters in strings to prevent JSON syntax errors.
     * Matches standard JSON escaping rules for backslashes, quotes, and newlines.
     *
     * @param text The raw string to be escaped.
     * @return An escaped version of the input string, or an empty string if input is null.
     */
    private @NonNull String escapeJson(String text) {
        if (text == null) return "";

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
