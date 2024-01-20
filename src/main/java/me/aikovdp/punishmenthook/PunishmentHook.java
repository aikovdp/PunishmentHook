package me.aikovdp.punishmenthook;

import me.leoko.advancedban.bukkit.event.PunishmentEvent;
import me.leoko.advancedban.bukkit.event.RevokePunishmentEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.net.URI;
import java.util.stream.Collectors;

public class PunishmentHook extends JavaPlugin implements Listener {

    private PunishmentHandler handler;
    @Override
    public void onEnable() {
        saveDefaultConfig();

        String webhookUrl = getConfig().getString("webhookUrl");
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            getSLF4JLogger().error("Webhook URL not set. Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        PunishmentRenderer renderer = new PunishmentRenderer(
                getTemplate("punishtemplate.json"),
                getTemplate("revokepunishtemplate.json"),
                getSLF4JLogger()
        );


        this.handler = new PunishmentHandler(
                renderer,
                new WebhookExecutor(URI.create(webhookUrl), getSLF4JLogger())
        );
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPunishmentEvent(PunishmentEvent event) {
        handler.handle(event.getPunishment(), false);
    }

    @EventHandler
    public void onPunishmentEvent(RevokePunishmentEvent event) {
        handler.handle(event.getPunishment(), true);
    }

    public String getTemplate(String name) {
        var reader = getTextResource(name);
        if (reader == null) {
            return null;
        }
        return new BufferedReader(reader).lines().collect(Collectors.joining());
    }
}
