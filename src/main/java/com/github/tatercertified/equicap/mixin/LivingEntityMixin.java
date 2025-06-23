package com.github.tatercertified.equicap.mixin;

import com.github.tatercertified.equicap.interfaces.MobCapTracker;
import com.github.tatercertified.equicap.interfaces.SpawnedFrom;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements SpawnedFrom {
    private ServerPlayerEntity spawnedFrom;

    @Override
    public void setSpawnedFrom(ServerPlayerEntity player) {
        this.spawnedFrom = player;
    }

    @Override
    public ServerPlayerEntity getSpawnedFrom() {
        return this.spawnedFrom;
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void equicap$removeFromTrackedPlayer(Entity.RemovalReason reason, CallbackInfo ci) {
        if (this.spawnedFrom != null) {
            ((MobCapTracker) this.spawnedFrom).removeMob(((LivingEntity)(Object)this).getType().getSpawnGroup());
        }
    }
}
