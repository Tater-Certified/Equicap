package com.github.tatercertified.equicap.mixin;

import com.github.tatercertified.equicap.interfaces.EntityTransfer;
import com.github.tatercertified.equicap.interfaces.MobCapTracker;
import com.github.tatercertified.equicap.interfaces.SpawnedFrom;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.entity.EntityLookup;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin implements EntityTransfer {

    @Shadow protected abstract EntityLookup<Entity> getEntityLookup();

    @Shadow @NotNull public abstract MinecraftServer getServer();

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
        int count = 1; // The current player
        if (this.getServer().getCurrentPlayerCount() != 1) {
            for (Entity entity : this.getEntityLookup().iterate()) {
                if (entity instanceof PlayerEntity playerEntity) {
                    BlockPos otherPos = playerEntity.getBlockPos();
                    if (otherPos.isWithinDistance(player.getBlockPos(), 128)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    @Override
    public Iterable<Entity> getEntities() {
        return this.getEntityLookup().iterate();
    }

    private boolean needsTransferred(Entity entity, ServerPlayerEntity leaving) {
        return entity instanceof MobEntity mob &&
                canDespawn(mob) &&
                ((SpawnedFrom)mob).getSpawnedFrom() != leaving;
    }

    private boolean needsPlayer(Entity entity) {
        return entity instanceof MobEntity mob &&
                canDespawn(mob);
    }

    private void attemptTransfer(MobEntity entity) {
        SpawnGroup group = entity.getType().getSpawnGroup();
        ServerPlayerEntity nearest = (ServerPlayerEntity) entity.getEntityWorld().getClosestPlayer(entity, group.getImmediateDespawnRange());
        transfer(entity, group, nearest);
    }

    private void transfer(MobEntity entity, SpawnGroup group, ServerPlayerEntity nearest) {
        if (nearest != null && ((MobCapTracker)nearest).canSpawn(group)) {
            ((MobCapTracker)nearest).addMob(group);
        } else {
            entity.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        }
    }

    private boolean canDespawn(MobEntity entity) {
        return !entity.isPersistent() && entity.canImmediatelyDespawn(16641);
    }
}
