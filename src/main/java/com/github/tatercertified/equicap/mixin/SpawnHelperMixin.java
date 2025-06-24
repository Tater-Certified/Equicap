package com.github.tatercertified.equicap.mixin;

import com.github.tatercertified.equicap.SpawnUtils;
import com.github.tatercertified.equicap.interfaces.MobCapTracker;
import com.github.tatercertified.equicap.interfaces.SpawnCheck;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.moulberry.mixinconstraints.annotations.IfModAbsent;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.SpawnDensityCapper;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SpawnHelper.class)
public abstract class SpawnHelperMixin {
    @IfModAbsent("carpet")
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

    @IfModLoaded("carpet")
    @Redirect(method = "spawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/SpawnHelper$Info;canSpawn(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/util/math/ChunkPos;)Z"))
    private static boolean equicap$checkPlayerCapCarpet(SpawnHelper.Info instance, SpawnGroup group, ChunkPos chunkPos) {
        SpawnDensityCapper capper = ((SpawnHelperInfoAccessor)instance).getDensityCapper();
        ServerPlayerEntity spawnFrom = ((SpawnCheck)capper).getSpawnFrom(group, chunkPos);
        if (spawnFrom != null) {
            SpawnUtils.PLAYER_SPAWN_FROM.set(spawnFrom);
            return ((MobCapTracker)spawnFrom).canSpawn(group);
        } else {
            return false;
        }
    }

    @IfModAbsent("carpet")
    @Redirect(method = "spawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/SpawnHelper;spawnEntitiesInChunk(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/SpawnHelper$Checker;Lnet/minecraft/world/SpawnHelper$Runner;)V"))
    private static void equicap$useModifiedSpawning(SpawnGroup group, ServerWorld world, WorldChunk chunk, SpawnHelper.Checker checker, SpawnHelper.Runner runner, @Share("spawnFrom") LocalRef<ServerPlayerEntity> playerRef) {
        SpawnUtils.modifiedSpawning(group, world, chunk, checker, runner, playerRef.get());
    }
}
