package com.github.tatercertified.equicap.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;

@Mixin(NaturalSpawner.class)
public interface SpawnHelperInvoker {
    @Invoker("isRightDistanceToPlayerAndSpawnPoint")
    static boolean isAcceptableSpawnPosition(ServerLevel world, ChunkAccess chunk, BlockPos.MutableBlockPos pos, double squaredDistance) {
        throw new AssertionError();
    }

    @Invoker("getRandomSpawnMobAt")
    static Optional<MobSpawnSettings.SpawnerData> pickRandomSpawnEntry(ServerLevel world, StructureManager structureAccessor, ChunkGenerator chunkGenerator, MobCategory spawnGroup, RandomSource random, BlockPos pos) {
        throw new AssertionError();
    }

    @Invoker("isValidSpawnPostitionForType")
    static boolean canSpawn(ServerLevel world, MobCategory group, StructureManager structureAccessor, ChunkGenerator chunkGenerator, MobSpawnSettings.SpawnerData spawnEntry, BlockPos.MutableBlockPos pos, double squaredDistance) {
        throw new AssertionError();
    }

    @Invoker("getMobForSpawn")
    static Mob createMob(ServerLevel world, EntityType<?> type) {
        throw new AssertionError();
    }

    @Invoker("isValidPositionForMob")
    static boolean isValidSpawn(ServerLevel world, Mob entity, double squaredDistance) {
        throw new AssertionError();
    }

    @Invoker("getRandomPosWithin")
    static BlockPos getRandomPosInChunkSection(Level world, LevelChunk chunk) {
        throw new AssertionError();
    }
}
