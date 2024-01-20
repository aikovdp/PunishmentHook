package me.aikovdp.punishmenthook;

import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.PunishmentType;
import org.slf4j.Logger;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class PunishmentRenderer {

    private final String punishTemplate;
    private final String revokeTemplate;
    private final Logger logger;

    public PunishmentRenderer(
            String punishTemplate,
            String revokeTemplate,
            Logger logger
    ) {
        this.logger = logger;
        this.punishTemplate = punishTemplate;
        this.revokeTemplate = revokeTemplate;
    }

    String render(Punishment punishment, boolean revoke) {
        String template = revoke ? revokeTemplate : punishTemplate;
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
                logger.error("Unknown punishment!");
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
