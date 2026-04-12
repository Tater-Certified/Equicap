package com.github.tatercertified.equicap.interfaces;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;

public interface SpawnCheck {
    ServerPlayer getSpawnFrom(MobCategory spawnGroup, ChunkPos chunkPos);
}
