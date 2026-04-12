package com.github.tatercertified.equicap.interfaces;

import java.util.List;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

public interface VisualDebug {
    void toggleDebugMarker(ServerPlayer input, ServerPlayer watcher);
    boolean isDebugMarkerToggled(ServerPlayer watcher);
    static void removeWatcher(ServerPlayer watcher) {
        if (((VisualDebug)watcher).isDebugMarkerToggled(null)) {
            ServerLevel world = watcher.level();
        for (Entity entity : ((EntityTransfer)world).equicap$getAllEntities()) {
                if (entity instanceof Mob mob && ((VisualDebug)mob).isDebugMarkerToggled(watcher)) {
                    ((VisualDebug)mob).toggleDebugMarker(null, watcher);
                }
            }
            ((VisualDebug)watcher).toggleDebugMarker(null, null);
        }
    }
    List<SynchedEntityData.DataValue<?>> setFakeGlow(boolean bool);

}
