package com.github.tatercertified.equicap.spawn_area;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;

public class DynamicSpawnManager {
    private int maxSpawnAttemptsPerTick = 3;
    private final DynamicSpawnArea area;
    public DynamicSpawnManager(RandomSource random) {
        this.area = new DynamicSpawnArea(random);
        // TODO Load values from config
    }

    public void spawnMobs(ServerLevel world, NaturalSpawner.SpawnState info, List<MobCategory> spawnableGroups) {
        for (Player player : world.players()) {
            BlockPos playerPos = player.blockPosition();

            LevelChunk spawnChunk = this.area.getRandomChunk(playerPos, world);
            for (int attempt = 0; attempt < maxSpawnAttemptsPerTick; attempt++) {
                NaturalSpawner.spawnForChunk(world, spawnChunk, info, spawnableGroups);
            }
        }
    }
}
