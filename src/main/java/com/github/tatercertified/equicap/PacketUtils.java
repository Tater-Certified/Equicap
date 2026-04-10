package com.github.tatercertified.equicap;

import com.github.tatercertified.equicap.interfaces.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PacketUtils {
    private static final Map<MobCategory, PlayerTeam> LOW_TEAMS = new EnumMap<>(MobCategory.class);
    private static final Map<MobCategory, PlayerTeam> HIGH_TEAMS = new EnumMap<>(MobCategory.class);

    public static void init(MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();

        setupTeam(scoreboard, MobCategory.MONSTER, ChatFormatting.RED, ChatFormatting.DARK_RED);
        setupTeam(scoreboard, MobCategory.CREATURE, ChatFormatting.GREEN, ChatFormatting.DARK_GREEN);
        setupTeam(scoreboard, MobCategory.AMBIENT, ChatFormatting.GRAY, ChatFormatting.DARK_GRAY);
        setupTeam(scoreboard, MobCategory.AXOLOTLS, ChatFormatting.LIGHT_PURPLE, ChatFormatting.DARK_PURPLE);
        setupTeam(scoreboard, MobCategory.UNDERGROUND_WATER_CREATURE, ChatFormatting.BLUE, ChatFormatting.DARK_BLUE);
        setupTeam(scoreboard, MobCategory.WATER_CREATURE, ChatFormatting.AQUA, ChatFormatting.DARK_AQUA);
        setupTeam(scoreboard, MobCategory.WATER_AMBIENT, ChatFormatting.AQUA, ChatFormatting.DARK_AQUA); // Re-use Aqua/Dark Aqua
        setupTeam(scoreboard, MobCategory.MISC, ChatFormatting.YELLOW, ChatFormatting.GOLD);
    }

    private static void setupTeam(Scoreboard scoreboard, MobCategory group, ChatFormatting low, ChatFormatting high) {
        String lowName = "equicap_" + group.getName() + "_l";
        String highName = "equicap_" + group.getName() + "_h";

        PlayerTeam lowTeam = scoreboard.getPlayerTeam(lowName);
        if (lowTeam == null) lowTeam = scoreboard.addPlayerTeam(lowName);
        lowTeam.setColor(low);
        LOW_TEAMS.put(group, lowTeam);

        PlayerTeam highTeam = scoreboard.getPlayerTeam(highName);
        if (highTeam == null) highTeam = scoreboard.addPlayerTeam(highName);
        highTeam.setColor(high);
        HIGH_TEAMS.put(group, highTeam);
    }

    public static void sendGlowPacket(ServerPlayer player, Mob entity) {
        String teamName = entity.getScoreboardName();
        if (player.level().getScoreboard().getPlayersTeam(teamName) == null) {
            PlayerTeam team = LOW_TEAMS.getOrDefault(entity.getType().getCategory(), LOW_TEAMS.get(MobCategory.MISC));
            if (team != null) {
                player.level().getScoreboard().addPlayerToTeam(teamName, team);
            }
        }
        List<SynchedEntityData.DataValue<?>> entries = ((VisualDebug)entity).setFakeGlow(true);
        player.connection.send(new ClientboundSetEntityDataPacket(entity.getId(), entries));
    }

    public static void removeGlowPacket(ServerPlayer player, Mob entity) {
        List<SynchedEntityData.DataValue<?>> entries = ((VisualDebug)entity).setFakeGlow(false);
        player.connection.send(new ClientboundSetEntityDataPacket(entity.getId(), entries));
    }

    public static void addNewEntitiesToDebugRenderer(ServerPlayer watcher, ServerPlayer input) {
        if (((VisualDebug)watcher).isDebugMarkerToggled(null)) {
            ServerLevel world = watcher.level();
            Scoreboard scoreboard = world.getScoreboard();
            
            MobCapTracker tracker = (MobCapTracker) input;
            Map<MobCategory, Integer> currentCounts = new HashMap<>();
            for (MobCategory group : MobCategory.values()) {
                currentCounts.put(group, tracker.getPlayerMobCap(group));
            }

            Map<MobCategory, Boolean> isHigh = new HashMap<>();
            for (MobCategory group : MobCategory.values()) {
                int cap = tracker.adjustedMobCapMaxSize(group);
                if (cap > 0) {
                    float percent = (float) currentCounts.get(group) / cap;
                    isHigh.put(group, percent > 0.75f);
                } else {
                    isHigh.put(group, true); 
                }
            }

            for (Entity entity : ((EntityTransfer)world).equicap$getAllEntities()) {
                if (entity instanceof Mob mob) {
                    ServerPlayer spawnedFrom = ((SpawnedFrom)mob).getSpawnedFrom();
                    if (((SpawnedFrom)mob).shouldBeInCap() &&
                            (spawnedFrom == null || spawnedFrom.equals(input))) {
                        
                        if (!((VisualDebug)mob).isDebugMarkerToggled(watcher)) {
                            ((VisualDebug)mob).toggleDebugMarker(input, watcher);
                        }
                        
                        MobCategory group = mob.getType().getCategory();
                        boolean high = isHigh.getOrDefault(group, false);
                        PlayerTeam team = high ? HIGH_TEAMS.get(group) : LOW_TEAMS.get(group);

                        if (team == null) {
                            team = high ? HIGH_TEAMS.get(MobCategory.MISC) : LOW_TEAMS.get(MobCategory.MISC);
                        }

                        if (team != null) {
                            scoreboard.addPlayerToTeam(entity.getScoreboardName(), team);
                        }
                    }
                }
            }
        }
    }
}
