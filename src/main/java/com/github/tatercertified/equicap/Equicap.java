package com.github.tatercertified.equicap;

import com.github.tatercertified.equicap.interfaces.EntityTransfer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class Equicap implements ModInitializer {

    @Override
    public void onInitialize() {
        // Load Config
        Config.getInstance().loadConfig();

        MobCapCommand.registerCommand();
        ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender, minecraftServer) -> {
            ((EntityTransfer) serverPlayNetworkHandler.getPlayer().getWorld()).transferEntitiesOnJoin(serverPlayNetworkHandler.getPlayer());
        });

        ServerPlayConnectionEvents.DISCONNECT.register((serverPlayNetworkHandler, minecraftServer) -> {
            ((EntityTransfer) serverPlayNetworkHandler.getPlayer().getWorld()).transferEntitiesOnLeave(serverPlayNetworkHandler.getPlayer());
        });
    }
}
