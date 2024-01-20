package me.aikovdp.punishmenthook;

import me.leoko.advancedban.utils.Punishment;

public class PunishmentHandler {
    private final PunishmentRenderer renderer;
    private final WebhookExecutor executor;

    public PunishmentHandler(
            PunishmentRenderer renderer,
            WebhookExecutor executor
    ) {

        this.renderer = renderer;
        this.executor = executor;
    }

    public void handle(Punishment punishment, Boolean revoked) {
        executor.execute(renderer.render(punishment, revoked));
    }
}
