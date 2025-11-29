package com.github.tatercertified.equicap.mixin;

import com.github.tatercertified.equicap.MobCapMergeManager;
import com.github.tatercertified.equicap.PacketUtils;
import com.github.tatercertified.equicap.interfaces.MobCapTracker;
import com.github.tatercertified.equicap.interfaces.VisualDebug;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumMap;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements MobCapTracker, VisualDebug {
    private final EnumMap<SpawnGroup, Integer> caps = new EnumMap<>(SpawnGroup.class);
    private float playerMobCapAdjustment = 1.0F;
    private int mergeTick;

    private ServerPlayerEntity mobCapVisualTarget;
    private int mobCapVisualTick;
    private boolean debugLog;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void equicap$fillMap(MinecraftServer server, ServerWorld world, GameProfile profile, SyncedClientOptions clientOptions, CallbackInfo ci) {
        for (SpawnGroup group : SpawnGroup.values()) {
            caps.put(group, 0);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void equicap$adjustMobCap(CallbackInfo ci) {
        this.mergeTick++;
        if ((this.mergeTick %= 10) == 0) {
            MobCapMergeManager.merge(((ServerPlayerEntity)(Object)this));
        }

        this.mobCapVisualTick++;
        if ((this.mobCapVisualTick %= 40) == 0) {
            PacketUtils.addNewEntitiesToDebugRenderer(((ServerPlayerEntity)(Object)this), this.mobCapVisualTarget);
            // Team update logic is now inside PacketUtils
        }
    }

    @Override
    public int getPlayerMobCap(SpawnGroup group) {
        return this.caps.get(group);
    }

    @Override
    public void addMob(SpawnGroup group) {
        this.caps.merge(group, 1, Integer::sum);
        if (this.isDebugLog()) {
            ((ServerPlayerEntity)(Object)this).sendMessage(net.minecraft.text.Text.literal("[Equicap] Added mob to " + group.getName() + " cap. New count: " + this.caps.get(group)), false);
        }
    }

    @Override
    public void removeMob(SpawnGroup group) {
        this.caps.merge(group, -1, (oldValue, value) -> Math.max(0, oldValue + value));
        if (this.isDebugLog()) {
            ((ServerPlayerEntity)(Object)this).sendMessage(net.minecraft.text.Text.literal("[Equicap] Removed mob from " + group.getName() + " cap. New count: " + this.caps.get(group)), false);
        }
    }

    @Override
    public void setDebugLog(boolean enabled) {
        this.debugLog = enabled;
    }

    @Override
    public boolean isDebugLog() {
        return this.debugLog;
    }

    @Override
    public void adjustMobCapBy(float percent) {
        this.playerMobCapAdjustment = percent;
    }

    @Override
    public int adjustedMobCapMaxSize(SpawnGroup group) {
        return (int) (group.getCapacity() * this.playerMobCapAdjustment);
    }

    @Override
    public void toggleDebugMarker(ServerPlayerEntity input, ServerPlayerEntity watcher) {
        this.mobCapVisualTarget = input;
    }

    @Override
    public boolean isDebugMarkerToggled(@Nullable ServerPlayerEntity player) {
        return this.mobCapVisualTarget != null;
    }

    @Override
    public java.util.List<DataTracker.SerializedEntry<?>> setFakeGlow(boolean bool) {
        return null;
    }
}
