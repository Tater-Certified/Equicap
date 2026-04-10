package com.github.tatercertified.equicap.mixin;

import com.github.tatercertified.equicap.SpawnUtils;
import com.github.tatercertified.equicap.interfaces.MobCapTracker;
import com.github.tatercertified.equicap.interfaces.SpawnCheck;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.moulberry.mixinconstraints.annotations.IfModAbsent;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LocalMobCapCalculator;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NaturalSpawner.class)
public abstract class SpawnHelperMixin {
    @IfModAbsent("carpet")
    @Redirect(method = "spawnForChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/NaturalSpawner$SpawnState;canSpawnForCategoryLocal(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/world/level/ChunkPos;)Z"))
    private static boolean equicap$checkPlayerCap(NaturalSpawner.SpawnState instance, MobCategory group, ChunkPos chunkPos, @Share("spawnFrom") LocalRef<ServerPlayer> playerRef) {
        LocalMobCapCalculator capper = ((SpawnHelperInfoAccessor)instance).getDensityCapper();
        ServerPlayer spawnFrom = ((SpawnCheck)capper).getSpawnFrom(group, chunkPos);
        if (spawnFrom != null) {
            playerRef.set(spawnFrom);
            return ((MobCapTracker)spawnFrom).canSpawn(group);
        } else {
            return false;
        }
    }

    @IfModLoaded("carpet")
    @Redirect(method = "spawnForChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/NaturalSpawner$SpawnState;canSpawnForCategoryLocal(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/world/level/ChunkPos;)Z"))
    private static boolean equicap$checkPlayerCapCarpet(NaturalSpawner.SpawnState instance, MobCategory group, ChunkPos chunkPos) {
        LocalMobCapCalculator capper = ((SpawnHelperInfoAccessor)instance).getDensityCapper();
        ServerPlayer spawnFrom = ((SpawnCheck)capper).getSpawnFrom(group, chunkPos);
        if (spawnFrom != null) {
            SpawnUtils.PLAYER_SPAWN_FROM.set(spawnFrom);
            return ((MobCapTracker)spawnFrom).canSpawn(group);
        } else {
            return false;
        }
    }

    @IfModAbsent("carpet")
    @Redirect(method = "spawnForChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/NaturalSpawner;spawnCategoryForChunk(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V"))
    private static void equicap$useModifiedSpawning(MobCategory group, ServerLevel world, LevelChunk chunk, NaturalSpawner.SpawnPredicate checker, NaturalSpawner.AfterSpawnCallback runner, @Share("spawnFrom") LocalRef<ServerPlayer> playerRef) {
        SpawnUtils.modifiedSpawning(group, world, chunk, checker, runner, playerRef.get());
    }
}
