package pl.jobscraper.core.infrastructure.notifier;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.jobscraper.core.infrastructure.persistence.entity.JobEntity;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Discord-based implementation of the {@link INotifier} interface.
 * Dispatches job notifications to a Discord channel using Webhooks.
 * The messages are formatted as Rich Embeds with emojis and structured fields.
 */
@Component
public class DiscordINotifier implements INotifier {
    private static final Logger logger = LoggerFactory.getLogger(DiscordINotifier.class);

    private static final int BATCH_SIZE = 10;
    private static final int MAX_REQUESTS_PER_MINUTE = 25;
    private static final long DELAY_BETWEEN_REQUESTS_MS = 60_000 / MAX_REQUESTS_PER_MINUTE;

    private final String discordWebhookUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper; //

    /**
     * Initializes the Discord notifier with a target Webhook URL.
     *
     * @param discordWebhookUrl The URL of the Discord webhook, injected from properties.
     */
    public DiscordINotifier(@Value("${discord.webhook.api}") String discordWebhookUrl) {
        this.discordWebhookUrl = discordWebhookUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();

        logger.info("DiscordNotifier initialized successfully");
    }

    /**
     * Sends the job listings to Discord.
     * Converts the list of jobs into a JSON payload structure required by Discord Webhooks.
     *
     * @param jobs A non-null list of {@link JobEntity} objects.
     * @throws InterruptedException If the HTTP communication is interrupted.
     */
    @Override
    public boolean send(@NonNull List<JobEntity> jobs) throws InterruptedException, JsonProcessingException {
        logger.info("Sending Discord notifications with {} jobs in batches of {}", jobs.size(), BATCH_SIZE);

        List<List<JobEntity>> batches = new ArrayList<>();
        for (int i = 0; i < jobs.size(); i += BATCH_SIZE) {
            batches.add(jobs.subList(i, Math.min(i + BATCH_SIZE, jobs.size())));
        }

        boolean allSuccess = true;

        for (int i = 0; i < batches.size(); i++) {
            List<JobEntity> batch = batches.get(i);

            boolean success = sendBatch(batch, i + 1, batches.size());
            if (!success) allSuccess = false;

            if (i < batches.size() - 1) {
                logger.debug("Rate limiting: waiting {}ms before next batch...", DELAY_BETWEEN_REQUESTS_MS);
                Thread.sleep(DELAY_BETWEEN_REQUESTS_MS);
            }
        }
        return allSuccess;
    }

    private boolean sendBatch(List<JobEntity> batch, int currentBatch, int totalBatches) throws JsonProcessingException {
        logger.info("Sending batch {}/{} ({} jobs)", currentBatch, totalBatches, batch.size());

        Map<String, Object> payload = new HashMap<>();
        payload.put("content", String.format("🚀 **New jobs found!** (Batch %d/%d)", currentBatch, totalBatches));

        Map<String, Object> embed = new HashMap<>();
        embed.put("title", "Job Offers");
        embed.put("color", 3066993);

        List<Map<String, Object>> fields = new ArrayList<>();
        for (JobEntity job : batch) {
            Map<String, Object> field = new HashMap<>();

            field.put("name", job.getTitle());

            String description = String.format("🏢 %s\n📍 %s\n%s🔗 [Link](%s)",
                    job.getCompany(),
                    job.getLocation(),
                    job.getSalary() != null ? "💵 " + job.getSalary() + "\n" : "",
                    job.getUrl());

            field.put("value", description);
            field.put("inline", false);
            fields.add(field);
        }
        embed.put("fields", fields);
        payload.put("embeds", List.of(embed));

        String jsonBody = objectMapper.writeValueAsString(payload);


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(discordWebhookUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                logger.info("Discord message sent!");
                return true;
            } else {
                logger.error("Discord API returned error: {} - {}", response.statusCode(), response.body());
                return false;
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to send batch to Discord", e);
            return false;
        }
    }
}