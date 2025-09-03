package com.yourname.obsidian.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ParticleLimiter extends PacketAdapter implements Listener {
    private final ProtocolManager protocolManager;
    private final Map<UUID, Integer> perTickBudget = new ConcurrentHashMap<>();
    private final Plugin plugin;

    public ParticleLimiter(Plugin plugin) {
        super(plugin, ListenerPriority.LOW, PacketType.Play.Server.WORLD_PARTICLES);
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(this);

        // reset counters each tick asynchronously to avoid blocking
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> perTickBudget.clear(), 0L, 1L);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        Player p = event.getPlayer();
        if (p == null || !p.isOnline()) return;

        // Simple combat-check placeholder: allow full particles if player's last damage < 10s (customize)
        boolean inCombat = p.hasMetadata("in_combat"); // replace with your combat logic
        if (inCombat) return;

        int used = perTickBudget.merge(p.getUniqueId(), 1, Integer::sum);
        if (used > 40) { // tweak threshold to taste
            event.setCancelled(true);
        }
    }

    public void shutdown() {
        if (protocolManager != null) protocolManager.removePacketListener(this);
    }
}
