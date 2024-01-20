package me.aikovdp.punishmenthook;

import me.leoko.advancedban.bungee.event.PunishmentEvent;
import me.leoko.advancedban.bungee.event.RevokePunishmentEvent;
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
import java.nio.file.Files;
import java.nio.file.Path;

public final class PunishmentHook extends Plugin implements Listener {
    private PunishmentRenderer punishmentRenderer;
    private String webhookUrl;
    private PunishmentHandler handler;

    @Override
    public void onEnable() {
        Logger log = getSLF4JLogger();
        getProxy().getPluginManager().registerListener(this, this);

        createConfig();

        // Load config
        try {
            Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
            webhookUrl = configuration.getString("webhookUrl");
        } catch (IOException e) {
            log.error("Unable to load config!");
        }
        if (webhookUrl.isEmpty()) {
            log.error("No webhook URL found! Disabling..");
            getProxy().getPluginManager().unregisterListeners(this);
        }

        try {
            punishmentRenderer = new PunishmentRenderer(log);
        } catch (IOException e) {
            log.error("Unable to load punishment webhook template!");
        }
        WebhookExecutor webhookExecutor = new WebhookExecutor(URI.create(webhookUrl), log);

        handler = new PunishmentHandler(punishmentRenderer, webhookExecutor);
    }

    @EventHandler
    public void onPunishmentEvent(PunishmentEvent event) {
        handler.handle(event.getPunishment(), false);
    }

    @EventHandler
    public void onPunishmentEvent(RevokePunishmentEvent event) {
        handler.handle(event.getPunishment(), true);
    }

    private void createConfig() {
        Path configPath = getDataFolder().toPath().resolve("config.yml");
        if (!Files.exists(configPath)) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.createDirectories(configPath);
                Files.copy(in, configPath);
            } catch (IOException e) {
                getSLF4JLogger().error("Unable to create config!");
            }
        }
    }
}
