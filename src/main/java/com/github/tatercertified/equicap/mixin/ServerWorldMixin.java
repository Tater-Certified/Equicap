package com.github.tatercertified.equicap.mixin;

import com.github.tatercertified.equicap.interfaces.EntityTransfer;
import com.github.tatercertified.equicap.interfaces.MobCapTracker;
import com.github.tatercertified.equicap.interfaces.SpawnedFrom;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.entity.EntityLookup;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin implements EntityTransfer {

    @Shadow protected abstract EntityLookup<Entity> getEntityLookup();

    @Shadow @NotNull public abstract MinecraftServer getServer();

    @Inject(method = "spawnEntity", at = @At("HEAD"))
    private void equicap$onSpawn(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof MobEntity mob && ((SpawnedFrom)mob).getSpawnedFrom() == null) {
            if (!shouldTrack(mob)) return;
            SpawnGroup group = mob.getType().getSpawnGroup();
            ServerPlayerEntity nearest = (ServerPlayerEntity) entity.getEntityWorld().getClosestPlayer(entity, group.getImmediateDespawnRange());
            if (nearest != null) {
                ((SpawnedFrom)mob).setSpawnedFrom(nearest);
                ((MobCapTracker)nearest).addMob(group);
                if (((MobCapTracker)nearest).isDebugLog()) {
                    nearest.sendMessage(net.minecraft.text.Text.literal("[Equicap] Hooked spawn of " + entity.getType().getName().getString() + ". Assigned to " + nearest.getName().getString()), false);
                }
            }
        }
    }

    @Override
    public void transferEntitiesOnLeave(ServerPlayerEntity player) {
        if (this.getServer().getCurrentPlayerCount() != 1) {
            for (Entity entity : this.getEntityLookup().iterate()) {
                if (needsTransferred(entity, player)) {
                    attemptTransfer((MobEntity) entity);
                }
            }
        }
    }

    @Override
    public void transferEntitiesOnJoin(ServerPlayerEntity joining) {
        if (this.getServer().getCurrentPlayerCount() == 0) {
            for (Entity entity : this.getEntityLookup().iterate()) {
                if (needsPlayer(entity)) {
                    transfer((MobEntity) entity, entity.getType().getSpawnGroup(), joining);
                }
            }
        }
    }

    @Override
    public int getNearbyPlayerCount(ServerPlayerEntity player) {
        List<ServerPlayerEntity> players = ((ServerWorld)(Object)this).getPlayers();
        int count = 0;
        for (ServerPlayerEntity other : players) {
            if (other.getBlockPos().isWithinDistance(player.getBlockPos(), 128)) {
                count++;
            }
        }
        return Math.max(1, count);
    }

    @Override
    public Iterable<Entity> getEntities() {
        return this.getEntityLookup().iterate();
    }

    private boolean needsTransferred(Entity entity, ServerPlayerEntity leaving) {
        return entity instanceof MobEntity mob &&
                shouldTrack(mob) &&
                ((SpawnedFrom)mob).getSpawnedFrom() != leaving;
    }

    private boolean needsPlayer(Entity entity) {
        return entity instanceof MobEntity mob &&
                shouldTrack(mob);
    }

    private void attemptTransfer(MobEntity entity) {
        SpawnGroup group = entity.getType().getSpawnGroup();
        ServerPlayerEntity nearest = (ServerPlayerEntity) entity.getEntityWorld().getClosestPlayer(entity, group.getImmediateDespawnRange());
        transfer(entity, group, nearest);
    }

    private void transfer(MobEntity entity, SpawnGroup group, ServerPlayerEntity nearest) {
        if (nearest != null && ((MobCapTracker)nearest).canSpawn(group)) {
            ((MobCapTracker)nearest).addMob(group);
        } else if (shouldDespawnOnUnload(entity)) {
            entity.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        } else {
            ((SpawnedFrom)entity).setSpawnedFrom(null);
        }
    }

    private boolean shouldDespawnOnUnload(MobEntity entity) {
        return !entity.isPersistent() && entity.canImmediatelyDespawn(16641);
    }

    private boolean shouldTrack(MobEntity entity) {
        return !entity.isPersistent() && !entity.cannotDespawn();
    }
}
