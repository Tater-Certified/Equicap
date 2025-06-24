package com.github.tatercertified.equicap.mixin;

import com.github.tatercertified.equicap.PacketUtils;
import com.github.tatercertified.equicap.interfaces.SpawnedFrom;
import com.github.tatercertified.equicap.interfaces.VisualDebug;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity implements VisualDebug, SpawnedFrom {
    @Shadow public abstract boolean isPersistent();

    @Shadow public abstract boolean canImmediatelyDespawn(double distanceSquared);

    private ServerPlayerEntity spawnedFrom;
    private final List<ServerPlayerEntity> mobCapWatchers = new ArrayList<>();

    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @WrapOperation(method = "checkDespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getClosestPlayer(Lnet/minecraft/entity/Entity;D)Lnet/minecraft/entity/player/PlayerEntity;"))
    private PlayerEntity equicap$getCachedPlayer(World instance, Entity entity, double v, Operation<PlayerEntity> original) {
        ServerPlayerEntity cached = ((SpawnedFrom)this).getSpawnedFrom();
        if (cached != null) {
            return cached;
        } else {
            return original.call(instance, entity, v);
        }
    }

    @Override
    public void toggleDebugMarker(ServerPlayerEntity input, ServerPlayerEntity watcher) {
        if (this.isDebugMarkerToggled(watcher)) {
            PacketUtils.removeGlowPacket(watcher, ((MobEntity)(Object)this));
            this.mobCapWatchers.remove(watcher);
        } else {
            if (this.getSpawnedFrom() == null || this.getSpawnedFrom().equals(input)) {
                PacketUtils.sendGlowPacket(watcher, ((MobEntity)(Object)this));
                this.mobCapWatchers.add(watcher);
            }
        }
    }

    @Override
    public boolean isDebugMarkerToggled(ServerPlayerEntity watcher) {
        return this.mobCapWatchers.contains(watcher);
    }

    @Override
    public DataTracker setFakeGlow(boolean bool) {
        DataTracker copy = DataTrackerInvoker.init(this, ((DataTrackerInvoker)this.dataTracker).getEntries());
        byte b = this.dataTracker.get(FLAGS);
        if (bool) {
            copy.set(FLAGS, (byte)(b | 1 << 6));
        } else {
            copy.set(FLAGS, (byte)(b & ~(1 << 6)));
        }
        return copy;
    }

    @Override
    public void setSpawnedFrom(ServerPlayerEntity player) {
        this.spawnedFrom = player;
    }

    @Override
    public ServerPlayerEntity getSpawnedFrom() {
        return this.spawnedFrom;
    }

    @Override
    public boolean shouldBeInCap() {
        return !this.isPersistent() && this.canImmediatelyDespawn(16641);
    }
}
