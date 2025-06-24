package com.github.tatercertified.equicap.mixin;

import net.minecraft.entity.data.DataTracked;
import net.minecraft.entity.data.DataTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DataTracker.class)
public interface DataTrackerInvoker {
    @Invoker("<init>")
    static DataTracker init(DataTracked trackedEntity, DataTracker.Entry<?>[] entries) {
        throw new AssertionError();
    }

    @Accessor("entries")
    DataTracker.Entry<?>[] getEntries();
}
