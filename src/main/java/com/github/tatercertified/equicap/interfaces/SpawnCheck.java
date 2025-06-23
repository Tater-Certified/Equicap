package com.github.tatercertified.equicap.interfaces;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.ChunkPos;

public interface SpawnCheck {
    ServerPlayerEntity getSpawnFrom(SpawnGroup spawnGroup, ChunkPos chunkPos);
}
