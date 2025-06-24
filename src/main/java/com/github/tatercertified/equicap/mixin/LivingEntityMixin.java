package com.github.tatercertified.equicap.mixin;

import com.github.tatercertified.equicap.interfaces.MobCapTracker;
import com.github.tatercertified.equicap.interfaces.SpawnedFrom;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "remove", at = @At("HEAD"))
    private void equicap$removeFromTrackedPlayer(Entity.RemovalReason reason, CallbackInfo ci) {
        if (this instanceof SpawnedFrom spawnedFrom) {
            if (spawnedFrom.getSpawnedFrom() != null) {
                ((MobCapTracker) spawnedFrom.getSpawnedFrom()).removeMob(((LivingEntity)(Object)this).getType().getSpawnGroup());
            }
        }
    }
}
