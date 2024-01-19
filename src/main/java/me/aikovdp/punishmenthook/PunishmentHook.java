package me.aikovdp.punishmenthook;

import com.google.common.io.Resources;
import me.leoko.advancedban.bungee.event.PunishmentEvent;
import me.leoko.advancedban.bungee.event.RevokePunishmentEvent;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.PunishmentType;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public final class PunishmentHook extends Plugin implements Listener {
    private static String punishTemplate;
    private static String revokePunishTemplate;
    private String webhookUrl;
    private Logger log;

    private final HttpClient client = HttpClient.newHttpClient();

    @Override
    public void onEnable() {
        log = getSLF4JLogger();
        getProxy().getPluginManager().registerListener(this, this);
        try {
            punishTemplate =
                    Resources.toString(Resources.getResource("punishtemplate.json"), StandardCharsets.UTF_8);
            revokePunishTemplate =
                    Resources.toString(Resources.getResource("revokepunishtemplate.json"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Unable to load punishment webhook template!");
        }


        Path configPath = getDataFolder().toPath().resolve("config.yml");
        if (!Files.exists(configPath)) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.createDirectories(configPath);
                Files.copy(in, configPath);
            } catch (IOException e) {
                log.error("Unable to create config!");
            }
        }

        // Load config
        Configuration configuration;
        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
            webhookUrl = configuration.getString("webhookUrl");
            if (webhookUrl.isEmpty()) {
                log.error("No webhook URL found! Disabling..");
                getProxy().getPluginManager().unregisterListeners(this);
            }
        } catch (IOException e) {
            log.error("Unable to load config!");
        }
    }

    @EventHandler
    public void onPunishmentEvent(PunishmentEvent event) {
        executeWebhook(
                formatWebhook(event.getPunishment(), false),
                webhookUrl
        );
    }

    @EventHandler
    public void onPunishmentEvent(RevokePunishmentEvent event) {
        executeWebhook(
                formatWebhook(event.getPunishment(), true),
                webhookUrl
        );
    }

    private void executeWebhook(String webhook, String webhookURL) {
        var req = HttpRequest.newBuilder()
                .uri(URI.create(webhookURL))
                .POST(HttpRequest.BodyPublishers.ofString(webhook))
                .build();
        client.sendAsync(req, HttpResponse.BodyHandlers.discarding())
        .exceptionally(ex -> {
                log.error("Unable to execute webhook", ex);
                return null;
        });
    }

    private String formatWebhook(Punishment punishment, boolean revoke) {
        String template = revoke ? revokePunishTemplate : punishTemplate;
        PunishmentType type = punishment.getType();
        String name = punishment.getName();
        String operator = punishment.getOperator();
        String duration = punishment.getDuration(false);
        String reason = punishment.getReason();
        String uuid = "c06f89064c8a49119c29ea1dbd1aab82";
        int color = 0xFFFF55;
        if (type != PunishmentType.IP_BAN && type != PunishmentType.TEMP_IP_BAN) {
            uuid = punishment.getUuid();
        }

        String action;
        switch (type) {
            case BAN -> {
                action = revoke ? "ban" : "banned";
                if (!revoke) {
                    color = 0xFF0000;
                }
            }
            case TEMP_BAN -> {
                action = revoke ? "temporary ban" : "temporarily banned";
                if (!revoke) {
                    color = 0xFF0000;
                }
            }
            case IP_BAN -> {
                action = revoke ? "IP ban" : "IP banned";
                if (!revoke) {
                    color = 0xFF0000;
                }
            }
            case TEMP_IP_BAN -> {
                action = revoke ? "temporary IP ban" : "temporarily IP banned";
                if (!revoke) {
                    color = 0xFF0000;
                }
            }
            case KICK -> {
                action = revoke ? "kick" : "kicked";
                if (!revoke) {
                    color = 0xFF7F00;
                }
            }
            case MUTE -> {
                action = revoke ? "mute" : "muted";
                if (!revoke) {
                    color = 0xFF7F00;
                }
            }
            case TEMP_MUTE -> {
                action = revoke ? "temporary mute" : "temporarily muted";
                if (!revoke) {
                    color = 0xFF7F00;
                }
            }
            case WARNING -> action = revoke ? "warning" : "warned";
            case TEMP_WARNING -> action = revoke ? "temporary warning" : "temporarily warned";
            default -> {
                action = revoke ? "punishment" : "punished";
                log.error("Unknown punishment!");
            }
        }

        if (revoke) {
            color = 0x00FF00;
        }

        return template
                .replaceAll("%name%", name)
                .replaceAll("%uuid%", uuid)
                .replaceAll("%operator%", operator)
                .replaceAll("%action%", action)
                .replaceAll("%reason%", reason)
                .replaceAll("%duration%", duration)
                .replaceAll("%color%", String.valueOf(color))
                .replaceAll("%time%", ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
    }
}
