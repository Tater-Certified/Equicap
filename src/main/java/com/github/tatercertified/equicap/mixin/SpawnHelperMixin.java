package com.github.tatercertified.equicap.mixin;

import com.github.tatercertified.equicap.interfaces.MobCapTracker;
import com.github.tatercertified.equicap.interfaces.SpawnCheck;
import com.github.tatercertified.equicap.interfaces.SpawnedFrom;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.SpawnDensityCapper;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(SpawnHelper.class)
public abstract class SpawnHelperMixin {

    @Shadow
    private static boolean isAcceptableSpawnPosition(ServerWorld world, Chunk chunk, BlockPos.Mutable pos, double squaredDistance) {
        return false;
    }

    @Shadow
    private static Optional<SpawnSettings.SpawnEntry> pickRandomSpawnEntry(ServerWorld world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, SpawnGroup spawnGroup, Random random, BlockPos pos) {
        return Optional.empty();
    }

    @Shadow
    private static boolean canSpawn(ServerWorld world, SpawnGroup group, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, SpawnSettings.SpawnEntry spawnEntry, BlockPos.Mutable pos, double squaredDistance) {
        return false;
    }

    @Shadow
    @Nullable
    private static MobEntity createMob(ServerWorld world, EntityType<?> type) {
        return null;
    }

    @Shadow
    private static boolean isValidSpawn(ServerWorld world, MobEntity entity, double squaredDistance) {
        return false;
    }

    @Shadow
    private static BlockPos getRandomPosInChunkSection(World world, WorldChunk chunk) {
        return null;
    }

    @Redirect(method = "spawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/SpawnHelper$Info;canSpawn(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/util/math/ChunkPos;)Z"))
    private static boolean equicap$checkPlayerCap(SpawnHelper.Info instance, SpawnGroup group, ChunkPos chunkPos, @Share("spawnFrom") LocalRef<ServerPlayerEntity> playerRef) {
        SpawnDensityCapper capper = ((SpawnHelperInfoAccessor)instance).getDensityCapper();
        ServerPlayerEntity spawnFrom = ((SpawnCheck)capper).getSpawnFrom(group, chunkPos);
        if (spawnFrom != null) {
            playerRef.set(spawnFrom);
            return ((MobCapTracker)spawnFrom).canSpawn(group);
        } else {
            return false;
        }
    }

    @Redirect(method = "spawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/SpawnHelper;spawnEntitiesInChunk(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/SpawnHelper$Checker;Lnet/minecraft/world/SpawnHelper$Runner;)V"))
    private static void equicap$useModifiedSpawning(SpawnGroup group, ServerWorld world, WorldChunk chunk, SpawnHelper.Checker checker, SpawnHelper.Runner runner, @Share("spawnFrom") LocalRef<ServerPlayerEntity> playerRef) {
        BlockPos blockPos = getRandomPosInChunkSection(world, chunk);
        if (blockPos.getY() >= world.getBottomY() + 1) {
            modifiedSpawnEntitiesInChunk(group, world, chunk, blockPos, checker, playerRef.get(), runner);
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
                    if (isAcceptableSpawnPosition(world, chunk, mutable, f)) {
                        if (spawnEntry == null) {
                            Optional<SpawnSettings.SpawnEntry> optional = pickRandomSpawnEntry(world, structureAccessor, chunkGenerator, group, world.random, mutable);
                            if (optional.isEmpty()) {
                                break;
                            }

                            spawnEntry = optional.get();
                            o = spawnEntry.minGroupSize() + world.random.nextInt(1 + spawnEntry.maxGroupSize() - spawnEntry.minGroupSize());
                        }

                        if (canSpawn(world, group, structureAccessor, chunkGenerator, spawnEntry, mutable, f) && checker.test(spawnEntry.type(), mutable, chunk)) {
                            MobEntity mobEntity = createMob(world, spawnEntry.type());
                            if (mobEntity == null) {
                                return;
                            }

                            mobEntity.refreshPositionAndAngles(d, i, e, world.random.nextFloat() * 360.0F, 0.0F);
                            if (isValidSpawn(world, mobEntity, f)) {
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
