package com.github.tatercertified.equicap.mixin.compat;

import com.bawnorton.mixinsquared.TargetHandler;
import com.github.tatercertified.equicap.SpawnUtils;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@IfModLoaded(value = "carpet")
@Mixin(value = SpawnHelper.class, priority = 1001)
public class CarpetMixin {
    @TargetHandler(
            mixin = "carpet.mixins.NaturalSpawnerMixin",
            name = "spawnMultipleTimes"
    )
    @Redirect(
            method = "@MixinSquared:Handler",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/SpawnHelper;spawnEntitiesInChunk(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/SpawnHelper$Checker;Lnet/minecraft/world/SpawnHelper$Runner;)V")
    )
    private static void equicap$useEquicapLogic(SpawnGroup group, ServerWorld world, WorldChunk chunk, SpawnHelper.Checker checker, SpawnHelper.Runner runner) {
        SpawnUtils.modifiedSpawning(group, world, chunk, checker, runner, SpawnUtils.PLAYER_SPAWN_FROM.get());
    }

    @TargetHandler(
            mixin = "carpet.mixins.NaturalSpawnerMixin",
            name = "spawnMultipleTimes"
    )
    @Inject(method = "@MixinSquared:Handler",
            at = @At("TAIL")
    )
    private static void equicap$removeThreadLocalRef(SpawnGroup category, ServerWorld world, WorldChunk chunk, SpawnHelper.Checker checker, SpawnHelper.Runner runner, CallbackInfo ci) {
        SpawnUtils.PLAYER_SPAWN_FROM.remove();
    }
}
