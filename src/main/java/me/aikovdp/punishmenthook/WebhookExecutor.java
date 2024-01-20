package me.aikovdp.punishmenthook;

import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WebhookExecutor {
    private final Logger logger;
    private final HttpClient client = HttpClient.newHttpClient();
    private final URI webhookUri;

    public WebhookExecutor(URI webhookUri, Logger logger) {
        this.webhookUri = webhookUri;
        this.logger = logger;
    }

    public void execute(String webhook) {
        var req = HttpRequest.newBuilder()
                .uri(webhookUri)
                .POST(HttpRequest.BodyPublishers.ofString(webhook))
                .build();

        client.sendAsync(req, HttpResponse.BodyHandlers.discarding()).exceptionally(ex -> {
            logger.error("Unable to execute webhook", ex);
            return null;
        });
    }
}
