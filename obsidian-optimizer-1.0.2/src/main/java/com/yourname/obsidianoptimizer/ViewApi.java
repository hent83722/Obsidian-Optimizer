package com.yourname.obsidianoptimizer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.World;

import java.lang.reflect.Method;

final class ViewApi {

    private static Method setVD, getVD, setNT, getNT;

    static {
        try { setVD = Player.class.getMethod("setViewDistance", int.class); } catch (Throwable ignored) {}
        try { getVD = Player.class.getMethod("getViewDistance"); } catch (Throwable ignored) {}
        try { setNT = Player.class.getMethod("setNoTickViewDistance", int.class); } catch (Throwable ignored) {}
        try { getNT = Player.class.getMethod("getNoTickViewDistance"); } catch (Throwable ignored) {}
    }

    static int getViewDistanceSafe(Player p) {
        try {
            if (getVD != null) return (int) getVD.invoke(p);
        } catch (Throwable ignored) {}
        // Fallback: return world’s current VD
        return p.getWorld().getViewDistance();
    }

    static void setViewDistanceSafe(Player p, int value) {
        try {
            if (setVD != null) {
                setVD.invoke(p, value);
                return;
            }
        } catch (Throwable ignored) {}

        // Fallback: set global VD (affects whole world!)
        World w = p.getWorld();
        w.setViewDistance(value);
    }

    static int getNoTickViewDistanceSafe(Player p) {
        try {
            if (getNT != null) return (int) getNT.invoke(p);
        } catch (Throwable ignored) {}
        // Fallback: just reuse world VD if NT not supported
        return p.getWorld().getViewDistance();
    }

    static void setNoTickViewDistanceSafe(Player p, int value) {
        try {
            if (setNT != null) {
                setNT.invoke(p, value);
                return;
            }
        } catch (Throwable ignored) {}

        // No fallback: just ignore silently (older Paper doesn’t support no-tick VD)
    }
}
