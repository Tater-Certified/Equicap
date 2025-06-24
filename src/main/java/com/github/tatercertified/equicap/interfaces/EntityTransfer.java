package com.github.tatercertified.equicap.interfaces;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

public interface EntityTransfer {
    void transferEntitiesOnLeave(ServerPlayerEntity leaving);
    void transferEntitiesOnJoin(ServerPlayerEntity joining);
    int getNearbyPlayerCount(ServerPlayerEntity player);
    Iterable<Entity> getEntities();
}
