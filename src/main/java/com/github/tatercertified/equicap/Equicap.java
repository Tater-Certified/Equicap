package com.github.tatercertified.equicap;

import net.fabricmc.api.ModInitializer;

public class Equicap implements ModInitializer {

    @Override
    public void onInitialize() {
        MobCapCommand.registerCommand();
    }
}
