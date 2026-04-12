package com.github.tatercertified.equicap.interfaces;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobCategory;
import java.util.EnumMap;
import java.util.Map;

public interface MobCapTracker {
    int getPlayerMobCap(MobCategory group);
    void addMob(MobCategory group);
    void removeMob(MobCategory group);
    default boolean canSpawn(MobCategory group) {
        return this.getPlayerMobCap(group) < this.adjustedMobCapMaxSize(group);
    }
    int adjustedMobCapMaxSize(MobCategory group);
    void adjustMobCapBy(float percent);
    default EnumMap<MobCategory, int[]> getPlayerMobCapData() {
        EnumMap<MobCategory, int[]> count = new EnumMap<>(MobCategory.class);
        for (MobCategory group : MobCategory.values()) {
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
    static EnumMap<MobCategory, int[]> getDimensionMobCount(ServerLevel world) {
        EnumMap<MobCategory, int[]> count = new EnumMap<>(MobCategory.class);
        for (ServerPlayer player : world.players()) {
            for (MobCategory group : MobCategory.values()) {
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
    static EnumMap<MobCategory, int[]> getTotalMobCount(MinecraftServer server) {
        EnumMap<MobCategory, int[]> count = new EnumMap<>(MobCategory.class);
        for (ServerLevel world : server.getAllLevels()) {
            EnumMap<MobCategory, int[]> worldCount = getDimensionMobCount(world);

            for (Map.Entry<MobCategory, int[]> entry : worldCount.entrySet()) {
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
