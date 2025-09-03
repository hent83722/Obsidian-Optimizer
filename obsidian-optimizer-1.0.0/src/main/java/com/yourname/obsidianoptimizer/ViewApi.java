package com.yourname.obsidianoptimizer;

import org.bukkit.entity.Player;

import java.lang.reflect.Method;

/**
 * Paper exposes per-player view distance and no-tick view distance.
 * To stay compatible across versions, we probe via reflection and
 * fall back gracefully if a method isn't available.
 */
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
        // If not supported, approximate with client-reported or default server VD
        return p.getClientViewDistance(); // usually available
    }

    static void setViewDistanceSafe(Player p, int value) {
        try {
            if (setVD != null) setVD.invoke(p, value);
        } catch (Throwable ignored) {}
    }

    static int getNoTickViewDistanceSafe(Player p) {
        try {
            if (getNT != null) return (int) getNT.invoke(p);
        } catch (Throwable ignored) {}
        // Fallback: treat as same as VD if NT unsupported
        return getViewDistanceSafe(p);
    }

    static void setNoTickViewDistanceSafe(Player p, int value) {
        try {
            if (setNT != null) setNT.invoke(p, value);
        } catch (Throwable ignored) {}
    }
}
