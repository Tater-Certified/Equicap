package com.github.tatercertified.equicap.mixin;

import com.github.tatercertified.equicap.MobCapMergeManager;
import com.github.tatercertified.equicap.PacketUtils;
import com.github.tatercertified.equicap.interfaces.MobCapTracker;
import com.github.tatercertified.equicap.interfaces.VisualDebug;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobCategory;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumMap;
import java.util.List;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin implements MobCapTracker, VisualDebug {
    private final EnumMap<MobCategory, Integer> caps = new EnumMap<>(MobCategory.class);
    private float playerMobCapAdjustment = 1.0F;
    private int mergeTick;

    private ServerPlayer mobCapVisualTarget;
    private int mobCapVisualTick;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void equicap$fillMap(MinecraftServer server, ServerLevel world, GameProfile profile, ClientInformation clientOptions, CallbackInfo ci) {
        for (MobCategory group : MobCategory.values()) {
            caps.put(group, 0);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void equicap$adjustMobCap(CallbackInfo ci) {
        this.mergeTick++;
        if ((this.mergeTick %= 10) == 0) {
            MobCapMergeManager.merge(((ServerPlayer)(Object)this));
        }

        this.mobCapVisualTick++;
        if ((this.mobCapVisualTick %= 40) == 0) {
            PacketUtils.addNewEntitiesToDebugRenderer(((ServerPlayer)(Object)this), this.mobCapVisualTarget);
        }
    }

    @Override
    public int getPlayerMobCap(MobCategory group) {
        return this.caps.get(group);
    }

    @Override
    public void addMob(MobCategory group) {
        this.caps.merge(group, 1, Integer::sum);
    }

    @Override
    public void removeMob(MobCategory group) {
        this.caps.merge(group, -1, (oldValue, value) -> Math.max(0, oldValue + value));
    }

    @Override
    public void adjustMobCapBy(float percent) {
        this.playerMobCapAdjustment = percent;
    }

    @Override
    public int adjustedMobCapMaxSize(MobCategory group) {
        return (int) (group.getMaxInstancesPerChunk() * this.playerMobCapAdjustment);
    }

    @Override
    public void toggleDebugMarker(ServerPlayer input, ServerPlayer watcher) {
        this.mobCapVisualTarget = input;
    }

    @Override
    public boolean isDebugMarkerToggled(@Nullable ServerPlayer player) {
        return this.mobCapVisualTarget != null;
    }

    @Override
    public List<SynchedEntityData.DataValue<?>> setFakeGlow(boolean bool) {
        return null;
    }
}
