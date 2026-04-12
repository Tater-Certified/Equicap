package com.github.tatercertified.equicap.interfaces;

import net.minecraft.server.level.ServerPlayer;

public interface SpawnedFrom {
    void setSpawnedFrom(ServerPlayer player);
    ServerPlayer getSpawnedFrom();
    boolean shouldBeInCap();
}
