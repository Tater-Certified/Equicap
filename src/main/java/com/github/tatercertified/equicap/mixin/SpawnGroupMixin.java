package com.github.tatercertified.equicap.mixin;

import com.github.tatercertified.equicap.interfaces.MobCapAccess;
import net.minecraft.world.entity.MobCategory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MobCategory.class)
public class SpawnGroupMixin implements MobCapAccess {
    @Mutable
    @Shadow @Final private int max;

    @Override
    public void setMobCapSize(int size) {
        this.max = size;
    }
}
