package com.yourname.obsidianoptimizer;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class Tuner implements Listener {

    private final ObsidianOptimizerPlugin plugin;
    private BukkitTask loopTask;
    private final Map<UUID, Long> lastAdjust = new HashMap<>();
    private final Map<UUID, Long> postTeleportHold = new HashMap<>();

    Tuner(ObsidianOptimizerPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    void start() {
        MsptSampler.start(plugin); // begin sampling MSPT
        int interval = plugin.getConfig().getInt("tick-interval", 80);
        loopTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, interval, interval);
    }

    void stop() {
        if (loopTask != null) loopTask.cancel();
        MsptSampler.stop();
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        int delay = plugin.getConfig().getInt("post-teleport-resend-delay-ticks", 40);
        postTeleportHold.put(e.getPlayer().getUniqueId(), System.currentTimeMillis() + (delay * 50L));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        int delay = plugin.getConfig().getInt("post-teleport-resend-delay-ticks", 40);
        postTeleportHold.put(e.getPlayer().getUniqueId(), System.currentTimeMillis() + (delay * 50L));
    }

    private boolean cooldownPassed(UUID id) {
        long now = System.currentTimeMillis();
        long cd = plugin.getConfig().getInt("adjustment-cooldown-seconds", 15) * 1000L;
        return now - lastAdjust.getOrDefault(id, 0L) >= cd;
    }

    private boolean holdActive(UUID id) {
        Long until = postTeleportHold.get(id);
        return until != null && System.currentTimeMillis() < until;
    }

    private void tick() {
        if (plugin.isPaused()) return;

        FileConfiguration c = plugin.getConfig();
        double targetMspt = c.getDouble("target-mspt", 45.0);
        double panicMspt = c.getDouble("panic-mspt", 55.0);
        int minVD = c.getInt("min-view-distance", 4);
        int maxVD = c.getInt("max-view-distance", 12);
        int minNT = c.getInt("min-no-tick-view-distance", 6);
        int maxNT = c.getInt("max-no-tick-view-distance", 16);
        int step = c.getInt("tune-step", 1);
        int panicStep = c.getInt("panic-step", 2);
        int highPing = c.getInt("high-ping", 180);
        int veryHighPing = c.getInt("very-high-ping", 300);

        double mspt = MsptSampler.getAvgMsptLast5s();

        for (Player p : Bukkit.getOnlinePlayers()) {
            UUID id = p.getUniqueId();
            if (!cooldownPassed(id) || holdActive(id)) continue;

            int curVD = ViewApi.getViewDistanceSafe(p);
            int curNT = ViewApi.getNoTickViewDistanceSafe(p);
            int ping = p.getPing();

            int vd = curVD;
            int nt = curNT;

            // Panic first: server struggling
            if (mspt >= panicMspt) {
                vd = Math.max(minVD, curVD - panicStep);
                nt = Math.max(minNT, curNT - panicStep);
            } else {
                // Normal tuning: consider both server load and player's ping
                if (mspt > targetMspt || ping >= highPing) {
                    // degrade
                    vd = Math.max(minVD, curVD - step);
                    nt = Math.max(minNT, curNT - step);
                    if (ping >= veryHighPing) {
                        vd = Math.max(minVD, vd - 1);
                        nt = Math.max(minNT, nt - 1);
                    }
                } else {
                    // improve toward caps
                    vd = Math.min(maxVD, curVD + step);
                    nt = Math.min(maxNT, curNT + step);
                }
            }

            boolean changed = false;
            if (vd != curVD) {
                ViewApi.setViewDistanceSafe(p, vd);
                changed = true;
            }
            if (nt != curNT) {
                ViewApi.setNoTickViewDistanceSafe(p, nt);
                changed |= true;
            }
            if (changed) lastAdjust.put(id, System.currentTimeMillis());
        }
    }
}
