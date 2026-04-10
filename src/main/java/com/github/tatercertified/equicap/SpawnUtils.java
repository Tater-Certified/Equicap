package com.github.tatercertified.equicap;

import com.github.tatercertified.equicap.interfaces.MobCapTracker;
import com.github.tatercertified.equicap.interfaces.SpawnedFrom;
import com.github.tatercertified.equicap.mixin.SpawnHelperInvoker;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;

public final class SpawnUtils {
    public static final ThreadLocal<ServerPlayer> PLAYER_SPAWN_FROM = new ThreadLocal<>();

    public static void modifiedSpawning(MobCategory group, ServerLevel world, LevelChunk chunk, NaturalSpawner.SpawnPredicate checker, NaturalSpawner.AfterSpawnCallback runner, ServerPlayer player) {
        BlockPos blockPos = SpawnHelperInvoker.getRandomPosInChunkSection(world, chunk);
        if (blockPos.getY() >= world.getMinY() + 1) {
            SpawnUtils.modifiedSpawnEntitiesInChunk(group, world, chunk, blockPos, checker, player, runner);
        }
    }

    // This is slightly modified from Mojang, but mostly Mojang code.
    // I added the ServerPlayerEntity reference
    private static void modifiedSpawnEntitiesInChunk(MobCategory group, ServerLevel world, ChunkAccess chunk, BlockPos pos, NaturalSpawner.SpawnPredicate checker, ServerPlayer spawnFrom, NaturalSpawner.AfterSpawnCallback runner) {
        StructureManager structureAccessor = world.structureManager();
        ChunkGenerator chunkGenerator = world.getChunkSource().getGenerator();
        int i = pos.getY();
        BlockState blockState = chunk.getBlockState(pos);
        if (!blockState.isRedstoneConductor(chunk, pos)) {
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            int j = 0;

            for(int k = 0; k < 3; ++k) {
                int l = pos.getX();
                int m = pos.getZ();
                MobSpawnSettings.SpawnerData spawnEntry = null;
                SpawnGroupData entityData = null;
                int o = Mth.ceil(world.getRandom().nextFloat() * 4.0F);
                int p = 0;

                for(int q = 0; q < o; ++q) {
                    l += world.getRandom().nextInt(6) - world.getRandom().nextInt(6);
                    m += world.getRandom().nextInt(6) - world.getRandom().nextInt(6);
                    mutable.set(l, i, m);
                    double d = (double)l + 0.5;
                    double e = (double)m + 0.5;

                    double f = spawnFrom.distanceToSqr(d, i, e);
                    if (SpawnHelperInvoker.isAcceptableSpawnPosition(world, chunk, mutable, f)) {
                        if (spawnEntry == null) {
                            Optional<MobSpawnSettings.SpawnerData> optional = SpawnHelperInvoker.pickRandomSpawnEntry(world, structureAccessor, chunkGenerator, group, world.getRandom(), mutable);
                            if (optional.isEmpty()) {
                                break;
                            }

                            spawnEntry = optional.get();
                            o = spawnEntry.minCount() + world.getRandom().nextInt(1 + spawnEntry.maxCount() - spawnEntry.minCount());
                        }

                        if (SpawnHelperInvoker.canSpawn(world, group, structureAccessor, chunkGenerator, spawnEntry, mutable, f) && checker.test(spawnEntry.type(), mutable, chunk)) {
                            Mob mobEntity = SpawnHelperInvoker.createMob(world, spawnEntry.type());
                            if (mobEntity == null) {
                                return;
                            }

                            mobEntity.snapTo(d, i, e, world.getRandom().nextFloat() * 360.0F, 0.0F);
                            if (SpawnHelperInvoker.isValidSpawn(world, mobEntity, f)) {
                                entityData = mobEntity.finalizeSpawn(world, world.getCurrentDifficultyAt(mobEntity.blockPosition()), EntitySpawnReason.NATURAL, entityData);
                                ++j;
                                ++p;
                                // Attach spawnFrom Player to Entity
                                ((SpawnedFrom)mobEntity).setSpawnedFrom(spawnFrom);

                                world.addFreshEntityWithPassengers(mobEntity);
                                // Attach Entity to spawnFrom Player
                                ((MobCapTracker)spawnFrom).addMob(group);

                                runner.run(mobEntity, chunk);
                                if (j >= mobEntity.getMaxSpawnClusterSize()) {
                                    return;
                                }

                                if (mobEntity.isMaxGroupSizeReached(p)) {
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
