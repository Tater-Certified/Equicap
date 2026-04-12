package com.github.tatercertified.equicap.mixin.compat;

import com.bawnorton.mixinsquared.TargetHandler;
import com.github.tatercertified.equicap.SpawnUtils;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@IfModLoaded(value = "carpet")
@Mixin(value = NaturalSpawner.class, priority = 1001)
public class CarpetMixin {
    @TargetHandler(
            mixin = "carpet.mixins.NaturalSpawnerMixin",
            name = "spawnMultipleTimes"
    )
    @Redirect(
            method = {"@MixinSquared:Handler"},
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/NaturalSpawner;spawnCategoryForChunk(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V")
    )
    private static void equicap$useEquicapLogic(MobCategory group, ServerLevel world, LevelChunk chunk, NaturalSpawner.SpawnPredicate checker, NaturalSpawner.AfterSpawnCallback runner) {
        SpawnUtils.modifiedSpawning(group, world, chunk, checker, runner, SpawnUtils.PLAYER_SPAWN_FROM.get());
    }

    @TargetHandler(
            mixin = "carpet.mixins.NaturalSpawnerMixin",
            name = "spawnMultipleTimes"
    )
    @Inject(method = {"@MixinSquared:Handler"},
            at = @At("TAIL")
    )
    private static void equicap$removeThreadLocalRef(MobCategory category, ServerLevel world, LevelChunk chunk, NaturalSpawner.SpawnPredicate checker, NaturalSpawner.AfterSpawnCallback runner, CallbackInfo ci) {
        SpawnUtils.PLAYER_SPAWN_FROM.remove();
    }
}
