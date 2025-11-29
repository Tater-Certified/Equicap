package com.github.tatercertified.equicap;

import com.github.tatercertified.equicap.interfaces.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PacketUtils {
    private static final Map<SpawnGroup, Team> LOW_TEAMS = new EnumMap<>(SpawnGroup.class);
    private static final Map<SpawnGroup, Team> HIGH_TEAMS = new EnumMap<>(SpawnGroup.class);

    public static void init(MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();

        setupTeam(scoreboard, SpawnGroup.MONSTER, Formatting.RED, Formatting.DARK_RED);
        setupTeam(scoreboard, SpawnGroup.CREATURE, Formatting.GREEN, Formatting.DARK_GREEN);
        setupTeam(scoreboard, SpawnGroup.AMBIENT, Formatting.GRAY, Formatting.DARK_GRAY);
        setupTeam(scoreboard, SpawnGroup.AXOLOTLS, Formatting.LIGHT_PURPLE, Formatting.DARK_PURPLE);
        setupTeam(scoreboard, SpawnGroup.UNDERGROUND_WATER_CREATURE, Formatting.BLUE, Formatting.DARK_BLUE);
        setupTeam(scoreboard, SpawnGroup.WATER_CREATURE, Formatting.AQUA, Formatting.DARK_AQUA);
        setupTeam(scoreboard, SpawnGroup.WATER_AMBIENT, Formatting.AQUA, Formatting.DARK_AQUA); // Re-use Aqua/Dark Aqua
        setupTeam(scoreboard, SpawnGroup.MISC, Formatting.YELLOW, Formatting.GOLD);
    }

    private static void setupTeam(Scoreboard scoreboard, SpawnGroup group, Formatting low, Formatting high) {
        String lowName = "equicap_" + group.getName() + "_l";
        String highName = "equicap_" + group.getName() + "_h";

        Team lowTeam = scoreboard.getTeam(lowName);
        if (lowTeam == null) lowTeam = scoreboard.addTeam(lowName);
        lowTeam.setColor(low);
        LOW_TEAMS.put(group, lowTeam);

        Team highTeam = scoreboard.getTeam(highName);
        if (highTeam == null) highTeam = scoreboard.addTeam(highName);
        highTeam.setColor(high);
        HIGH_TEAMS.put(group, highTeam);
    }

    public static void sendGlowPacket(ServerPlayerEntity player, MobEntity entity) {
        String teamName = entity.getNameForScoreboard();
        if (player.getEntityWorld().getScoreboard().getScoreHolderTeam(teamName) == null) {
            Team team = LOW_TEAMS.getOrDefault(entity.getType().getSpawnGroup(), LOW_TEAMS.get(SpawnGroup.MISC));
            if (team != null) {
                player.getEntityWorld().getScoreboard().addScoreHolderToTeam(teamName, team);
            }
        }
        List<DataTracker.SerializedEntry<?>> entries = ((VisualDebug)entity).setFakeGlow(true);
        player.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(entity.getId(), entries));
    }

    public static void removeGlowPacket(ServerPlayerEntity player, MobEntity entity) {
        List<DataTracker.SerializedEntry<?>> entries = ((VisualDebug)entity).setFakeGlow(false);
        player.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(entity.getId(), entries));
    }

    public static void addNewEntitiesToDebugRenderer(ServerPlayerEntity watcher, ServerPlayerEntity input) {
        if (((VisualDebug)watcher).isDebugMarkerToggled(null)) {
            ServerWorld world = (ServerWorld) watcher.getEntityWorld();
            Scoreboard scoreboard = world.getScoreboard();
            
            MobCapTracker tracker = (MobCapTracker) input;
            Map<SpawnGroup, Integer> currentCounts = new HashMap<>();
            for (SpawnGroup group : SpawnGroup.values()) {
                currentCounts.put(group, tracker.getPlayerMobCap(group));
            }

            Map<SpawnGroup, Boolean> isHigh = new HashMap<>();
            for (SpawnGroup group : SpawnGroup.values()) {
                int cap = tracker.adjustedMobCapMaxSize(group);
                if (cap > 0) {
                    float percent = (float) currentCounts.get(group) / cap;
                    isHigh.put(group, percent > 0.75f);
                } else {
                    isHigh.put(group, true); 
                }
            }

            for (Entity entity : ((EntityTransfer)world).getEntities()) {
                if (entity instanceof MobEntity mob) {
                    ServerPlayerEntity spawnedFrom = ((SpawnedFrom)mob).getSpawnedFrom();
                    if (((SpawnedFrom)mob).shouldBeInCap() &&
                            (spawnedFrom == null || spawnedFrom.equals(input))) {
                        
                        boolean shouldGlow = !((VisualDebug)mob).isDebugMarkerToggled(watcher);
                        
                        if (shouldGlow) {
                            ((VisualDebug)mob).toggleDebugMarker(input, watcher);
                        }
                        
                        SpawnGroup group = mob.getType().getSpawnGroup();
                        boolean high = isHigh.getOrDefault(group, false);
                        Team team = high ? HIGH_TEAMS.get(group) : LOW_TEAMS.get(group);
                        if (team == null) team = high ? HIGH_TEAMS.get(SpawnGroup.MISC) : LOW_TEAMS.get(SpawnGroup.MISC);
                        
                        if (team != null) {
                            scoreboard.addScoreHolderToTeam(entity.getNameForScoreboard(), team);
                        }
                        
                        if (shouldGlow) {
                        }
                    }
                }
            }
        }
    }
    
    public static void applyGlow(ServerPlayerEntity watcher, Entity entity) {
        watcher.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(entity.getId(), new StatusEffectInstance(StatusEffects.GLOWING, -1, 0, true, false), false));
    }
}
