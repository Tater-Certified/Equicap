package com.github.tatercertified.equicap;

import com.github.tatercertified.equicap.interfaces.EntityTransfer;
import com.github.tatercertified.equicap.interfaces.MobCapTracker;
import net.minecraft.server.level.ServerPlayer;

public final class MobCapMergeManager {

    public static void merge(ServerPlayer player) {
        switch (Config.getInstance().mergeMode) {
            case Combine -> combine(player);
            case VanillaLike -> vanillalikeMerge(player);
            default -> {}
        }
    }

    private static void combine(ServerPlayer player) {
        int nearby = ((EntityTransfer) player.level()).getNearbyPlayerCount(player);
        ((MobCapTracker)player).adjustMobCapBy(1.0F / nearby);
    }

    private static void vanillalikeMerge(ServerPlayer player) {
        ServerPlayer closest = (ServerPlayer) player.level().getNearestPlayer(player, 128);
        if (closest != null) {
            int chunksAway = (int) Math.sqrt(player.blockPosition().distSqr(closest.blockPosition())) / 16;
            if (chunksAway < 17) {
                float percentage = (chunksAway + 17F) / 34F;
                ((MobCapTracker)player).adjustMobCapBy(percentage);
            }
        }
    }
}
