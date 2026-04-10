package com.github.tatercertified.equicap.mixin;

import com.github.tatercertified.equicap.PacketUtils;
import com.github.tatercertified.equicap.interfaces.SpawnedFrom;
import com.github.tatercertified.equicap.interfaces.VisualDebug;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@Mixin(Mob.class)
public abstract class MobEntityMixin extends LivingEntity implements VisualDebug, SpawnedFrom {
    @Shadow public abstract boolean isPersistenceRequired();

    @Shadow public abstract boolean requiresCustomPersistence();

    private ServerPlayer spawnedFrom;
    private final List<ServerPlayer> mobCapWatchers = new ArrayList<>();

    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @WrapOperation(method = "checkDespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getNearestPlayer(Lnet/minecraft/world/entity/Entity;D)Lnet/minecraft/world/entity/player/Player;"))
    private Player equicap$getCachedPlayer(Level instance, Entity entity, double v, Operation<Player> original) {
        ServerPlayer cached = this.getSpawnedFrom();
        if (cached != null) {
            return cached;
        } else {
            return original.call(instance, entity, v);
        }
    }

    @Override
    public void toggleDebugMarker(ServerPlayer input, ServerPlayer watcher) {
        if (this.isDebugMarkerToggled(watcher)) {
            PacketUtils.removeGlowPacket(watcher, ((Mob)(Object)this));
            this.mobCapWatchers.remove(watcher);
        } else {
            if (this.getSpawnedFrom() == null || this.getSpawnedFrom().equals(input)) {
                PacketUtils.sendGlowPacket(watcher, ((Mob)(Object)this));
                this.mobCapWatchers.add(watcher);
            }
        }
    }

    @Override
    public boolean isDebugMarkerToggled(ServerPlayer watcher) {
        return this.mobCapWatchers.contains(watcher);
    }

    @Override
    public List<SynchedEntityData.DataValue<?>> setFakeGlow(boolean bool) {
        byte b = this.entityData.get(DATA_SHARED_FLAGS_ID);
        byte newValue = bool ? (byte)(b | 1 << 6) : (byte)(b & ~(1 << 6));
        return List.of(SynchedEntityData.DataValue.create(DATA_SHARED_FLAGS_ID, newValue));
    }

    @Override
    public void setSpawnedFrom(ServerPlayer player) {
        this.spawnedFrom = player;
    }

    @Override
    public ServerPlayer getSpawnedFrom() {
        return this.spawnedFrom;
    }

    @Override
    public boolean shouldBeInCap() {
        return !this.isPersistenceRequired() && !this.requiresCustomPersistence();
    }
}
