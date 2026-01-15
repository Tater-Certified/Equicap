package com.github.tatercertified.equicap.spawn_area;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.chunk.WorldChunk;

import java.util.List;

public class DynamicSpawnManager {
    private int maxSpawnAttemptsPerTick = 3;
    private final DynamicSpawnArea area;
    public DynamicSpawnManager(Random random) {
        this.area = new DynamicSpawnArea(random);
        // TODO Load values from config
    }

    public void spawnMobs(ServerWorld world, SpawnHelper.Info info, List<SpawnGroup> spawnableGroups) {
        for (PlayerEntity player : world.getPlayers()) {
            BlockPos playerPos = player.getBlockPos();

            WorldChunk spawnChunk = this.area.getRandomChunk(playerPos, world);
            for (int attempt = 0; attempt < maxSpawnAttemptsPerTick; attempt++) {
                SpawnHelper.spawn(world, spawnChunk, info, spawnableGroups);
            }
        }
    }
}
