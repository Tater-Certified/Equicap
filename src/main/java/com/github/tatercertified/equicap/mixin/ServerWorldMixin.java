package com.github.tatercertified.equicap.mixin;

import com.github.tatercertified.equicap.interfaces.EntityTransfer;
import com.github.tatercertified.equicap.interfaces.MobCapTracker;
import com.github.tatercertified.equicap.interfaces.SpawnedFrom;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.entity.LevelEntityGetter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ServerLevel.class)
public abstract class ServerWorldMixin implements EntityTransfer {

    @Shadow protected abstract LevelEntityGetter<Entity> getEntities();

    @Shadow @NotNull public abstract MinecraftServer getServer();

    @Inject(method = "addFreshEntity", at = @At("HEAD"))
    private void equicap$onSpawn(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Mob mob && ((SpawnedFrom)mob).getSpawnedFrom() == null) {
            if (!shouldTrack(mob)) return;
            MobCategory group = mob.getType().getCategory();
            ServerPlayer nearest = (ServerPlayer) entity.level().getNearestPlayer(entity, group.getDespawnDistance());
            if (nearest != null) {
                ((SpawnedFrom)mob).setSpawnedFrom(nearest);
                ((MobCapTracker)nearest).addMob(group);
            }
        }
    }

    @Override
    public void transferEntitiesOnLeave(ServerPlayer player) {
        if (this.getServer().getPlayerCount() != 1) {
            for (Entity entity : this.getEntities().getAll()) {
                if (needsTransferred(entity, player)) {
                    attemptTransfer((Mob) entity);
                }
            }
        }
    }

    @Override
    public void transferEntitiesOnJoin(ServerPlayer joining) {
        if (this.getServer().getPlayerCount() == 0) {
            for (Entity entity : this.getEntities().getAll()) {
                if (needsPlayer(entity)) {
                    transfer((Mob) entity, entity.getType().getCategory(), joining);
                }
            }
        }
    }

    @Override
    public int getNearbyPlayerCount(ServerPlayer player) {
        List<ServerPlayer> players = ((ServerLevel)(Object)this).players();
        int count = 0;
        for (ServerPlayer other : players) {
            if (other.blockPosition().closerThan(player.blockPosition(), 128)) {
                count++;
            }
        }
        return Math.max(1, count);
    }

    @Override
    public Iterable<Entity> equicap$getAllEntities() {
        return this.getEntities().getAll();
    }

    private boolean needsTransferred(Entity entity, ServerPlayer leaving) {
        return entity instanceof Mob mob &&
                shouldTrack(mob) &&
                ((SpawnedFrom)mob).getSpawnedFrom() != leaving;
    }

    private boolean needsPlayer(Entity entity) {
        return entity instanceof Mob mob &&
                shouldTrack(mob);
    }

    private void attemptTransfer(Mob entity) {
        MobCategory group = entity.getType().getCategory();
        ServerPlayer nearest = (ServerPlayer) entity.level().getNearestPlayer(entity, group.getDespawnDistance());
        transfer(entity, group, nearest);
    }

    private void transfer(Mob entity, MobCategory group, ServerPlayer nearest) {
        if (nearest != null && ((MobCapTracker)nearest).canSpawn(group)) {
            ((MobCapTracker)nearest).addMob(group);
        } else if (shouldDespawnOnUnload(entity)) {
            entity.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        } else {
            ((SpawnedFrom)entity).setSpawnedFrom(null);
        }
    }

    private boolean shouldDespawnOnUnload(Mob entity) {
        return !entity.isPersistenceRequired() && entity.removeWhenFarAway(16641);
    }

    private boolean shouldTrack(Mob entity) {
        return !entity.isPersistenceRequired() && !entity.requiresCustomPersistence();
    }
}
