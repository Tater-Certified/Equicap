package com.github.tatercertified.equicap.interfaces;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.EnumMap;
import java.util.Map;

public interface MobCapTracker {
    int getPlayerMobCap(SpawnGroup group);
    void addMob(SpawnGroup group);
    void removeMob(SpawnGroup group);
    default boolean canSpawn(SpawnGroup group) {
        return this.getPlayerMobCap(group) < this.adjustedMobCapMaxSize(group);
    }
    int adjustedMobCapMaxSize(SpawnGroup group);
    void adjustMobCapBy(float percent);
    void setDebugLog(boolean enabled);
    boolean isDebugLog();
    default EnumMap<SpawnGroup, int[]> getPlayerMobCapData() {
        EnumMap<SpawnGroup, int[]> count = new EnumMap<>(SpawnGroup.class);
        for (SpawnGroup group : SpawnGroup.values()) {
            int cap = this.getPlayerMobCap(group);
            int total = this.adjustedMobCapMaxSize(group);
            int[] existing = count.get(group);
            if (existing == null) {
                existing = new int[] {cap, total};
            } else {
                existing[0] += cap;
                existing[1] += total;
            }
            count.put(group, existing);
        }
        return count;
    }
    static EnumMap<SpawnGroup, int[]> getDimensionMobCount(ServerWorld world) {
        EnumMap<SpawnGroup, int[]> count = new EnumMap<>(SpawnGroup.class);
        for (ServerPlayerEntity player : world.getPlayers()) {
            for (SpawnGroup group : SpawnGroup.values()) {
                int cap = ((MobCapTracker)player).getPlayerMobCap(group);
                int total = ((MobCapTracker)player).adjustedMobCapMaxSize(group);
                int[] existing = count.get(group);
                if (existing == null) {
                    existing = new int[] {cap, total};
                } else {
                    existing[0] += cap;
                    existing[1] += total;
                }
                count.put(group, existing);
            }
        }
        return count;
    }
    static EnumMap<SpawnGroup, int[]> getTotalMobCount(MinecraftServer server) {
        EnumMap<SpawnGroup, int[]> count = new EnumMap<>(SpawnGroup.class);
        for (ServerWorld world : server.getWorlds()) {
            EnumMap<SpawnGroup, int[]> worldCount = getDimensionMobCount(world);

            for (Map.Entry<SpawnGroup, int[]> entry : worldCount.entrySet()) {
                int[] existing = count.get(entry.getKey());
                if (existing == null) {
                    existing = entry.getValue();
                } else {
                    existing[0] += entry.getValue()[0];
                    existing[1] += entry.getValue()[1];
                }
                count.put(entry.getKey(), existing);
            }
        }
        return count;
    }
}
