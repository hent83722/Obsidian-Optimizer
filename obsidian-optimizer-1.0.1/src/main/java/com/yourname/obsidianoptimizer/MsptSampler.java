package com.yourname.obsidianoptimizer;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayDeque;
import java.util.Deque;

final class MsptSampler {

    private static final Deque<Double> last100Samples = new ArrayDeque<>();
    private static long lastTickTime = System.nanoTime();
    private static BukkitTask task;

    static void start(Plugin plugin) {
        if (task != null) return;
        task = Bukkit.getScheduler().runTaskTimer(plugin, MsptSampler::sample, 1, 1);
    }

    static void stop() {
        if (task != null) task.cancel();
        task = null;
        last100Samples.clear();
    }

    private static void sample() {
        long now = System.nanoTime();
        double mspt = (now - lastTickTime) / 1_000_000.0;
        lastTickTime = now;
        if (last100Samples.size() >= 100) last100Samples.removeFirst();
        last100Samples.addLast(mspt);
    }

    static double getAvgMsptLast5s() {
        if (last100Samples.isEmpty()) return 50.0;
        double sum = 0.0;
        for (double d : last100Samples) sum += d;
        return sum / last100Samples.size();
    }
}
