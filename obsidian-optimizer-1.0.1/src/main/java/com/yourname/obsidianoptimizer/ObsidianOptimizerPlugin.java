package com.yourname.obsidianoptimizer;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ObsidianOptimizerPlugin extends JavaPlugin {

    private Tuner tuner;
    private boolean paused = false;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        tuner = new Tuner(this);
        tuner.start();
        getLogger().info("Obsidian Optimizer enabled.");
    }

    @Override
    public void onDisable() {
        if (tuner != null) tuner.stop();
        getLogger().info("Obsidian Optimizer disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("oo")) return false;
        if (!sender.hasPermission("obsidianoptimizer.admin")) {
            sender.sendMessage("§cYou don't have permission.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
            FileConfiguration c = getConfig();
            double mspt = MsptSampler.getAvgMsptLast5s();
            sender.sendMessage("§6[ObsidianOptimizer] Status:");
            sender.sendMessage("§7  paused: §f" + paused);
            sender.sendMessage("§7  avg MSPT (5s): §f" + String.format("%.2f", mspt));
            sender.sendMessage("§7  VD range: §f" + c.getInt("min-view-distance") + " - " + c.getInt("max-view-distance"));
            sender.sendMessage("§7  nVD range: §f" + c.getInt("min-no-tick-view-distance") + " - " + c.getInt("max-no-tick-view-distance"));
            for (Player p : Bukkit.getOnlinePlayers()) {
                sender.sendMessage("§8  - " + p.getName() + " ping=" + p.getPing()
                        + " VD=" + ViewApi.getViewDistanceSafe(p)
                        + " nVD=" + ViewApi.getNoTickViewDistanceSafe(p));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            sender.sendMessage("§aObsidian Optimizer config reloaded.");
            return true;
        }

        if (args[0].equalsIgnoreCase("pause")) {
            paused = true;
            sender.sendMessage("§eObsidian Optimizer paused.");
            return true;
        }

        if (args[0].equalsIgnoreCase("resume")) {
            paused = false;
            sender.sendMessage("§aObsidian Optimizer resumed.");
            return true;
        }

        if (args[0].equalsIgnoreCase("set") && args.length == 3) {
            try {
                int val = Integer.parseInt(args[2]);
                switch (args[1].toLowerCase()) {
                    case "min":
                        getConfig().set("min-view-distance", val);
                        break;
                    case "max":
                        getConfig().set("max-view-distance", val);
                        break;
                    case "nt-min":
                        getConfig().set("min-no-tick-view-distance", val);
                        break;
                    case "nt-max":
                        getConfig().set("max-no-tick-view-distance", val);
                        break;
                    default:
                        sender.sendMessage("§cUnknown key. Use min|max|nt-min|nt-max");
                        return true;
                }
                saveConfig();
                sender.sendMessage("§aUpdated " + args[1] + " to " + val + ".");
            } catch (NumberFormatException e) {
                sender.sendMessage("§cValue must be an integer.");
            }
            return true;
        }

        sender.sendMessage("§eUsage: /oo <status|reload|pause|resume|set [min|max|nt-min|nt-max] <value>>");
        return true;
    }

    public boolean isPaused() { return paused; }
}
