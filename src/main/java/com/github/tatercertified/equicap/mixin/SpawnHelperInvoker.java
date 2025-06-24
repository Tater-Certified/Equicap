package com.github.tatercertified.equicap.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(SpawnHelper.class)
public interface SpawnHelperInvoker {
    @Invoker("isAcceptableSpawnPosition")
    static boolean isAcceptableSpawnPosition(ServerWorld world, Chunk chunk, BlockPos.Mutable pos, double squaredDistance) {
        throw new AssertionError();
    }

    @Invoker("pickRandomSpawnEntry")
    static Optional<SpawnSettings.SpawnEntry> pickRandomSpawnEntry(ServerWorld world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, SpawnGroup spawnGroup, Random random, BlockPos pos) {
        throw new AssertionError();
    }

    @Invoker("canSpawn")
    static boolean canSpawn(ServerWorld world, SpawnGroup group, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, SpawnSettings.SpawnEntry spawnEntry, BlockPos.Mutable pos, double squaredDistance) {
        throw new AssertionError();
    }

    @Invoker("createMob")
    static MobEntity createMob(ServerWorld world, EntityType<?> type) {
        throw new AssertionError();
    }

    @Invoker("isValidSpawn")
    static boolean isValidSpawn(ServerWorld world, MobEntity entity, double squaredDistance) {
        throw new AssertionError();
    }

    @Invoker("getRandomPosInChunkSection")
    static BlockPos getRandomPosInChunkSection(World world, WorldChunk chunk) {
        throw new AssertionError();
    }
}
