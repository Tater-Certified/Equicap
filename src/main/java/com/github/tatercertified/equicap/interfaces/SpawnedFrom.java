package com.github.tatercertified.equicap.interfaces;

import net.minecraft.server.network.ServerPlayerEntity;

public interface SpawnedFrom {
    void setSpawnedFrom(ServerPlayerEntity player);
    ServerPlayerEntity getSpawnedFrom();
    boolean shouldBeInCap();
}
