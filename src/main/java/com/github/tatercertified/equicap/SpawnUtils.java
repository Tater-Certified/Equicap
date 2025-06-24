package com.github.tatercertified.equicap;

import com.github.tatercertified.equicap.interfaces.MobCapTracker;
import com.github.tatercertified.equicap.interfaces.SpawnedFrom;
import com.github.tatercertified.equicap.mixin.SpawnHelperInvoker;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

import java.util.Optional;

public final class SpawnUtils {
    public static final ThreadLocal<ServerPlayerEntity> PLAYER_SPAWN_FROM = new ThreadLocal<>();

    public static void modifiedSpawning(SpawnGroup group, ServerWorld world, WorldChunk chunk, SpawnHelper.Checker checker, SpawnHelper.Runner runner, ServerPlayerEntity player) {
        BlockPos blockPos = SpawnHelperInvoker.getRandomPosInChunkSection(world, chunk);
        if (blockPos.getY() >= world.getBottomY() + 1) {
            SpawnUtils.modifiedSpawnEntitiesInChunk(group, world, chunk, blockPos, checker, player, runner);
        }
    }

    // This is slightly modified from Mojang, but mostly Mojang code.
    // I added the ServerPlayerEntity reference
    private static void modifiedSpawnEntitiesInChunk(SpawnGroup group, ServerWorld world, Chunk chunk, BlockPos pos, SpawnHelper.Checker checker, ServerPlayerEntity spawnFrom, SpawnHelper.Runner runner) {
        StructureAccessor structureAccessor = world.getStructureAccessor();
        ChunkGenerator chunkGenerator = world.getChunkManager().getChunkGenerator();
        int i = pos.getY();
        BlockState blockState = chunk.getBlockState(pos);
        if (!blockState.isSolidBlock(chunk, pos)) {
            BlockPos.Mutable mutable = new BlockPos.Mutable();
            int j = 0;

            for(int k = 0; k < 3; ++k) {
                int l = pos.getX();
                int m = pos.getZ();
                SpawnSettings.SpawnEntry spawnEntry = null;
                EntityData entityData = null;
                int o = MathHelper.ceil(world.random.nextFloat() * 4.0F);
                int p = 0;

                for(int q = 0; q < o; ++q) {
                    l += world.random.nextInt(6) - world.random.nextInt(6);
                    m += world.random.nextInt(6) - world.random.nextInt(6);
                    mutable.set(l, i, m);
                    double d = (double)l + 0.5;
                    double e = (double)m + 0.5;

                    double f = spawnFrom.squaredDistanceTo(d, i, e);
                    if (SpawnHelperInvoker.isAcceptableSpawnPosition(world, chunk, mutable, f)) {
                        if (spawnEntry == null) {
                            Optional<SpawnSettings.SpawnEntry> optional = SpawnHelperInvoker.pickRandomSpawnEntry(world, structureAccessor, chunkGenerator, group, world.random, mutable);
                            if (optional.isEmpty()) {
                                break;
                            }

                            spawnEntry = optional.get();
                            o = spawnEntry.minGroupSize() + world.random.nextInt(1 + spawnEntry.maxGroupSize() - spawnEntry.minGroupSize());
                        }

                        if (SpawnHelperInvoker.canSpawn(world, group, structureAccessor, chunkGenerator, spawnEntry, mutable, f) && checker.test(spawnEntry.type(), mutable, chunk)) {
                            MobEntity mobEntity = SpawnHelperInvoker.createMob(world, spawnEntry.type());
                            if (mobEntity == null) {
                                return;
                            }

                            mobEntity.refreshPositionAndAngles(d, i, e, world.random.nextFloat() * 360.0F, 0.0F);
                            if (SpawnHelperInvoker.isValidSpawn(world, mobEntity, f)) {
                                entityData = mobEntity.initialize(world, world.getLocalDifficulty(mobEntity.getBlockPos()), SpawnReason.NATURAL, entityData);
                                ++j;
                                ++p;
                                world.spawnEntityAndPassengers(mobEntity);
                                // Attach Entity to spawnFrom Player
                                ((MobCapTracker)spawnFrom).addMob(group);

                                // Attach spawnFrom Player to Entity
                                ((SpawnedFrom)mobEntity).setSpawnedFrom(spawnFrom);

                                runner.run(mobEntity, chunk);
                                if (j >= mobEntity.getLimitPerChunk()) {
                                    return;
                                }

                                if (mobEntity.spawnsTooManyForEachTry(p)) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}
