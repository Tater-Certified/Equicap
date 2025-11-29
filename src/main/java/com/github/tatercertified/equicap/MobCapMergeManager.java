package com.github.tatercertified.equicap;

import com.github.tatercertified.equicap.interfaces.EntityTransfer;
import com.github.tatercertified.equicap.interfaces.MobCapTracker;
import net.minecraft.server.network.ServerPlayerEntity;

public final class MobCapMergeManager {

    public static void merge(ServerPlayerEntity player) {
        switch (Config.getInstance().mergeMode) {
            case Combine -> combine(player);
            case VanillaLike -> vanillalikeMerge(player);
            default -> {}
        }
    }

    private static void combine(ServerPlayerEntity player) {
        int nearby = ((EntityTransfer) player.getEntityWorld()).getNearbyPlayerCount(player);
        ((MobCapTracker)player).adjustMobCapBy(1.0F / nearby);
    }

    private static void vanillalikeMerge(ServerPlayerEntity player) {
        ServerPlayerEntity closest = (ServerPlayerEntity) player.getEntityWorld().getClosestPlayer(player, 128);
        if (closest != null) {
            int chunksAway = (int) Math.sqrt(player.getBlockPos().getSquaredDistance(closest.getBlockPos())) / 16;
            if (chunksAway < 17) {
                float percentage = (chunksAway + 17F) / 34F;
                ((MobCapTracker)player).adjustMobCapBy(percentage);
            }
        }
    }
}
