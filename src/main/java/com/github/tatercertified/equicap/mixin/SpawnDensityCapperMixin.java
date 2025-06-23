package com.github.tatercertified.equicap.mixin;

import com.github.tatercertified.equicap.interfaces.MobCapTracker;
import com.github.tatercertified.equicap.interfaces.SpawnCheck;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.SpawnDensityCapper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(SpawnDensityCapper.class)
public abstract class SpawnDensityCapperMixin implements SpawnCheck {
    @Shadow protected abstract List<ServerPlayerEntity> getMobSpawnablePlayers(ChunkPos chunkPos);

    @Nullable
    @Override
    public ServerPlayerEntity getSpawnFrom(SpawnGroup spawnGroup, ChunkPos chunkPos) {
        for (ServerPlayerEntity player : this.getMobSpawnablePlayers(chunkPos)) {
            if (((MobCapTracker)player).canSpawn(spawnGroup)) {
                return player;
            }
        }
        return null;
    }
}
