package com.github.tatercertified.equicap.mixin;

import com.github.tatercertified.equicap.interfaces.MobCapAccess;
import net.minecraft.entity.SpawnGroup;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SpawnGroup.class)
public class SpawnGroupMixin implements MobCapAccess {
    @Mutable
    @Shadow @Final private int capacity;

    @Override
    public void setMobCapSize(int size) {
        this.capacity = size;
    }
}
