package com.github.tatercertified.equicap.interfaces;

import net.minecraft.entity.SpawnGroup;

public interface MobCapTracker {
    int getPlayerMobCap(SpawnGroup group);
    void addMob(SpawnGroup group);
    void removeMob(SpawnGroup group);
    default boolean canSpawn(SpawnGroup group) {
        return this.getPlayerMobCap(group) < this.adjustedMobCapMaxSize(group);
    }
    int adjustedMobCapMaxSize(SpawnGroup group);
    void adjustMobCapBy(float percent);
}
