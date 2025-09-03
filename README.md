### 1.0.2 Is here!


## What's new?:

# 1️⃣ Adaptive View Distance
What it is

Minecraft’s view distance controls how many chunks a player can see around them.

Higher view distances increase CPU and network load.

This plugin dynamically adjusts view distance based on server conditions and player count.

How it works

AdaptiveDistanceTask (now embedded inside ObsidianOptimizerPlugin) periodically checks:

Number of online players.

Optional tick load and ping.

Rules applied:

If many players are online, reduce view distance to reduce server load.

If few players are online, increase view distance to give better visuals.

Adjusts per player dynamically (through Tuner) if needed.

Benefit

Prevents server lag spikes when lots of players are online.

Keeps the game looking nice when server load is low.

# 2️⃣ Tuner System (Dynamic Per-Player Optimization)

The Tuner.java module adds more fine-grained control:

Key Features

MSPT Sampling

MsptSampler tracks average milliseconds per tick (server tick speed).

A tick over 50ms → server is lagging.

Dynamic Adjustment

Each player’s:

view distance (chunks visible)

no-tick view distance (chunks that don’t receive entity updates)
…is tuned independently.

Adjustments depend on:

Server MSPT (load)

Player ping (network lag)

Cooldowns and teleport delays to avoid abrupt changes

Panic Mode

If MSPT is above a threshold (e.g., 55ms), the plugin reduces view distances aggressively to stabilize the server.

Cooldowns

Prevents view distance from changing too often.

Respecting teleport events to avoid visual glitches for players who just respawned or teleported.

# 3️⃣ Particle Limiter (Very early beta so will be added soon.)
What it is

Minecraft particles (like smoke, explosion, potion effects) can cause significant lag when lots of them spawn at once.

ParticleLimiter uses ProtocolLib to intercept particle packets and limit excessive particle spawning.

Behavior

Detects if ProtocolLib is installed.

Only enables particle control if available.

Helps high-performance servers maintain smooth FPS and TPS.

# 4️⃣ Multi-Version Support

With the new Maven setup:

Your plugin can compile for multiple Paper/Spigot versions (1.13 → 1.21).

Using Maven profiles, you can build:

One JAR per version.

Each JAR works with its target Minecraft version without errors.

Ensures you can support old and new servers without modifying code.

⚠️ Note: This does not produce a single universal JAR yet; it builds multiple JARs per version.

# 5️⃣ Key Configuration Options

Inside config.yml, the plugin allows tuning:

| Option                                                    | Purpose                                                  |
| --------------------------------------------------------- | -------------------------------------------------------- |
| `tick-interval`                                           | How often the tuner checks server conditions (in ticks)  |
| `target-mspt`                                             | Ideal server tick time (default \~45ms)                  |
| `panic-mspt`                                              | Tick time considered "dangerously high" (default \~55ms) |
| `min-view-distance` / `max-view-distance`                 | Caps for view distance                                   |
| `min-no-tick-view-distance` / `max-no-tick-view-distance` | Caps for chunks without entity updates                   |
| `tune-step`                                               | How much to increase/decrease view distance per tick     |
| `panic-step`                                              | How much to drop view distance during high load          |
| `post-teleport-resend-delay-ticks`                        | Prevents visual glitches right after teleport/respawn    |

# 6️⃣ Why This Update is Powerful for Competitive Servers

Reduces lag spikes from heavy entity loads or large numbers of players.

Improves responsiveness for competitive gameplay:

Lower MSPT → smoother player movement and block updates.

High ping players or lag spikes are handled gracefully.

Keeps graphics quality high when the server can handle it.

# 7️⃣ Optional Improvements Already Considered

Could implement reflection-based API calls to make one JAR compatible across all versions (1.13 → 1.21).

Particle limiter and view distance tuning could eventually support per-world or per-player preferences.

# ✅ Summary:

Adaptive view distance → reduces lag dynamically.

Tuner → adjusts each player’s distance based on server load and ping.

Particle limiter → limits heavy particle spam for smoother performance.

Multi-version build system → allows building for all major Paper versions.
