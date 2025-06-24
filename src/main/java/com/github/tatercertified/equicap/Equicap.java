package com.github.tatercertified.equicap;

import com.github.tatercertified.equicap.interfaces.EntityTransfer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.util.Formatting;

public class Equicap implements ModInitializer {

    @Override
    public void onInitialize() {
        // Load Config
        Config.getInstance().loadConfig();
        MobCapCommand.registerCommand();

        ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> {
            PacketUtils.RED_TEAM = minecraftServer.getScoreboard().addTeam("equicap");
            PacketUtils.RED_TEAM.setColor(Formatting.RED);
        });

        ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender, minecraftServer) -> {
            ((EntityTransfer) serverPlayNetworkHandler.getPlayer().getWorld()).transferEntitiesOnJoin(serverPlayNetworkHandler.getPlayer());
        });

        ServerPlayConnectionEvents.DISCONNECT.register((serverPlayNetworkHandler, minecraftServer) -> {
            ((EntityTransfer) serverPlayNetworkHandler.getPlayer().getWorld()).transferEntitiesOnLeave(serverPlayNetworkHandler.getPlayer());
        });
    }
}
