package com.github.tatercertified.equicap.mixin;

import com.github.tatercertified.equicap.interfaces.MobCapTracker;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumMap;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements MobCapTracker {
    private final EnumMap<SpawnGroup, Integer> caps = new EnumMap<>(SpawnGroup.class);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void equicap$fillMap(MinecraftServer server, ServerWorld world, GameProfile profile, SyncedClientOptions clientOptions, CallbackInfo ci) {
        for (SpawnGroup group : SpawnGroup.values()) {
            caps.put(group, 0);
        }
    }


    @Override
    public int getPlayerMobCap(SpawnGroup group) {
        return this.caps.get(group);
    }

    @Override
    public void addMob(SpawnGroup group) {
        this.caps.merge(group, 1, Integer::sum);
    }

    @Override
    public void removeMob(SpawnGroup group) {
        this.caps.merge(group, -1, (oldValue, value) -> Math.max(0, oldValue + value));
    }
}
