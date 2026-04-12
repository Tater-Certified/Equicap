package com.github.tatercertified.equicap.mixin;

import com.github.tatercertified.equicap.interfaces.MobCapTracker;
import com.github.tatercertified.equicap.interfaces.SpawnCheck;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LocalMobCapCalculator;

@Mixin(LocalMobCapCalculator.class)
public abstract class SpawnDensityCapperMixin implements SpawnCheck {
    @Shadow protected abstract List<ServerPlayer> getPlayersNear(ChunkPos chunkPos);

    @Nullable
    @Override
    public ServerPlayer getSpawnFrom(MobCategory spawnGroup, ChunkPos chunkPos) {
        for (ServerPlayer player : this.getPlayersNear(chunkPos)) {
            if (((MobCapTracker)player).canSpawn(spawnGroup)) {
                return player;
            }
        }
        return null;
    }
}
