package com.yourname.obsidianoptimizer;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ObsidianOptimizerPlugin extends JavaPlugin {

    private boolean paused = false;

    @Override
    public void onEnable() {
        getLogger().info("ObsidianOptimizer loaded!");

        // Start adaptive distance task
        int minDistance = 6;   // Minimum view distance
        int maxDistance = 12;  // Maximum view distance
        AdaptiveDistanceTask task = new AdaptiveDistanceTask(minDistance, maxDistance);
        task.runTaskTimer(this, 0L, 100L); // Runs every 5 seconds (100 ticks)

        // Optional ParticleLimiter - only if ProtocolLib is installed
        try {
            Class<?> clazz = Class.forName("com.yourname.obsidianoptimizer.packets.ParticleLimiter");
            clazz.getConstructor(JavaPlugin.class).newInstance(this);
            getLogger().info("ParticleLimiter enabled (ProtocolLib detected).");
        } catch (ClassNotFoundException e) {
            getLogger().info("ProtocolLib not found. ParticleLimiter skipped.");
        } catch (Exception e) {
            getLogger().warning("Failed to initialize ParticleLimiter: " + e.getMessage());
        }

        // Start Tuner
        new Tuner(this).start();
    }

    @Override
    public void onDisable() {
        getLogger().info("ObsidianOptimizer unloaded!");
    }

    // Pause API for Tuner
    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    /**
     * AdaptiveDistanceTask adjusts server view distance based on online players.
     */
    public class AdaptiveDistanceTask extends BukkitRunnable {
        private final int minDistance;
        private final int maxDistance;

        public AdaptiveDistanceTask(int minDistance, int maxDistance) {
            this.minDistance = minDistance;
            this.maxDistance = maxDistance;
        }

        @Override
        public void run() {
            int online = Bukkit.getOnlinePlayers().size();
            int newDistance = minDistance;

            if (online >= 20) {
                newDistance = minDistance; // many players, lower distance
            } else if (online >= 10) {
                newDistance = (minDistance + maxDistance) / 2;
            } else {
                newDistance = maxDistance; // few players, higher distance
            }

            // Apply view distance to all online players
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setViewDistance(newDistance);
            }

            getLogger().info("AdaptiveDistanceTask set view distance to " + newDistance + " for " + online + " players.");
        }
    }
}
