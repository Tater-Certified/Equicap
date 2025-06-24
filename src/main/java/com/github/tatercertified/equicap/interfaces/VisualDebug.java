package com.github.tatercertified.equicap.interfaces;

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public interface VisualDebug {
    void toggleDebugMarker(ServerPlayerEntity input, ServerPlayerEntity watcher);
    boolean isDebugMarkerToggled(ServerPlayerEntity watcher);
    static void removeWatcher(ServerPlayerEntity watcher) {
        if (((VisualDebug)watcher).isDebugMarkerToggled(null)) {
            ServerWorld world = (ServerWorld) watcher.getWorld();
            for (Entity entity : ((EntityTransfer)world).getEntities()) {
                if (entity instanceof MobEntity mob && ((VisualDebug)mob).isDebugMarkerToggled(watcher)) {
                    ((VisualDebug)mob).toggleDebugMarker(null, watcher);
                }
            }
            ((VisualDebug)watcher).toggleDebugMarker(null, null);
        }
    }
    DataTracker setFakeGlow(boolean bool);

}
