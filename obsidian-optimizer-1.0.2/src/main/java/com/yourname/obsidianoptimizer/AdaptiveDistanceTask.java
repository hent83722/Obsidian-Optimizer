package com.yourname.obsidian.tasks;

import org.bukkit.Server;
import org.bukkit.entity.Player;

public class AdaptiveDistanceTask implements Runnable {
    private final Server server;
    private final int minView;
    private final int maxView;

    public AdaptiveDistanceTask(Server server, int minViewDistance, int maxViewDistance) {
        this.server = server;
        this.minView = Math.max(3, minViewDistance);
        this.maxView = Math.max(this.minView, maxViewDistance);
    }

    @Override
    public void run() {
        // getTPS may be Paper-only, but many Paper forks provide it. Use defensively.
        double tps = 20.0;
        try {
            // Paper exposes getTPS() on Server
            double[] tpsArr = (double[]) server.getClass().getMethod("getTPS").invoke(server);
            if (tpsArr != null && tpsArr.length > 0) tps = tpsArr[0];
        } catch (Exception ignored) {
            // fallback to 20.0 (best-effort if not Paper)
        }

        // Map tps 16..20 => scale 0..1
        double scale = Math.max(0.0, Math.min(1.0, (tps - 16.0) / (20.0 - 16.0)));

        for (Player p : server.getOnlinePlayers()) {
            int ping = 0;
            try {
                ping = (int) p.getClass().getMethod("getPing").invoke(p);
            } catch (Exception ignored) { }

            int target = (int) Math.round(minView + (maxView - minView) * scale);

            if (ping > 150) target = Math.max(minView, target - 2); // reduce view for high ping
            if (target < minView) target = minView;
            if (target > maxView) target = maxView;

            try {
                // Player#setViewDistance is Paper API; try reflectively to avoid hard dependency
                java.lang.reflect.Method m = p.getClass().getMethod("setViewDistance", int.class);
                int current = (int) p.getClass().getMethod("getViewDistance").invoke(p);
                if (current != target) m.invoke(p, target);
            } catch (NoSuchMethodException nsme) {
                // Not available — ignore
            } catch (Exception ignored) { }
        }
    }
}
