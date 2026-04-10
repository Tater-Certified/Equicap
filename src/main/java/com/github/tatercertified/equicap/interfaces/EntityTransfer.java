package com.github.tatercertified.equicap.interfaces;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public interface EntityTransfer {
    void transferEntitiesOnLeave(ServerPlayer leaving);
    void transferEntitiesOnJoin(ServerPlayer joining);
    int getNearbyPlayerCount(ServerPlayer player);
    Iterable<Entity> equicap$getAllEntities();
}
