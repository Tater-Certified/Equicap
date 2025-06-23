package com.github.tatercertified.equicap.mixin;

import com.github.tatercertified.equicap.interfaces.SpawnedFrom;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MobEntity.class)
public class MobEntityMixin {
    @WrapOperation(method = "checkDespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getClosestPlayer(Lnet/minecraft/entity/Entity;D)Lnet/minecraft/entity/player/PlayerEntity;"))
    private PlayerEntity equicap$getCachedPlayer(World instance, Entity entity, double v, Operation<PlayerEntity> original) {
        ServerPlayerEntity cached = ((SpawnedFrom)this).getSpawnedFrom();
        if (cached != null) {
            return cached;
        } else {
            return original.call(instance, entity, v);
        }
    }
}
