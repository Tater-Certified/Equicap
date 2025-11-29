package com.github.tatercertified.equicap;

import com.github.tatercertified.equicap.interfaces.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public final class PacketUtils {
    public static Team RED_TEAM;

    public static void sendGlowPacket(ServerPlayerEntity player, MobEntity entity) {
        if (((SpawnedFrom)entity).getSpawnedFrom() == null) {
            player.getEntityWorld().getScoreboard().addScoreHolderToTeam(entity.getNameForScoreboard(), RED_TEAM);
        }
        DataTracker tracker = ((VisualDebug)entity).setFakeGlow(true);
        player.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(entity.getId(), tracker.getChangedEntries()));
    }

    public static void removeGlowPacket(ServerPlayerEntity player, MobEntity entity) {
        DataTracker tracker = ((VisualDebug)entity).setFakeGlow(false);
        player.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(entity.getId(), tracker.getChangedEntries()));
    }

    public static void addNewEntitiesToDebugRenderer(ServerPlayerEntity watcher, ServerPlayerEntity input) {
        if (((VisualDebug)watcher).isDebugMarkerToggled(null)) {
            ServerWorld world = (ServerWorld) watcher.getEntityWorld();
            for (Entity entity : ((EntityTransfer)world).getEntities()) {
                if (entity instanceof MobEntity mob) {
                    ServerPlayerEntity spawnedFrom = ((SpawnedFrom)mob).getSpawnedFrom();
                    if (((SpawnedFrom)mob).shouldBeInCap() &&
                            (spawnedFrom == null || spawnedFrom.equals(input)) &&
                            !((VisualDebug)mob).isDebugMarkerToggled(watcher)) {
                        ((VisualDebug)mob).toggleDebugMarker(input, watcher);
                    }
                }
            }
        }
    }
}
