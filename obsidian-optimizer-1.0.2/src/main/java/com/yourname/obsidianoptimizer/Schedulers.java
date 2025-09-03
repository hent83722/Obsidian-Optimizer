package com.yourname.obsidian.util;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Schedulers {
    private final Plugin plugin;
    private final BukkitScheduler bukkitScheduler;

    public Schedulers(Plugin plugin) {
        this.plugin = plugin;
        this.bukkitScheduler = plugin.getServer().getScheduler();
    }

    public BukkitScheduler getBukkitScheduler() {
        return bukkitScheduler;
    }

    public void runAsync(Runnable r) {
        bukkitScheduler.runTaskAsynchronously(plugin, r);
    }

    public void runSync(Runnable r) {
        bukkitScheduler.runTask(plugin, r);
    }

    public void runLaterSync(Runnable r, long delayTicks) {
        bukkitScheduler.runTaskLater(plugin, r, delayTicks);
    }

    public void runRepeatingSync(Runnable r, long delayTicks, long periodTicks) {
        bukkitScheduler.runTaskTimer(plugin, r, delayTicks, periodTicks);
    }
}
