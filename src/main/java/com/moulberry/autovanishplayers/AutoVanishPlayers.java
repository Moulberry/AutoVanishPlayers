package com.moulberry.autovanishplayers;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoVanishPlayers implements ModInitializer {
	public static final String MOD_ID = "autovanishplayers";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static boolean isAutoVanishPlayersEnabled = false;

	@Override
	public void onInitialize() {
		LOGGER.info("Initialized AutoVanishPlayers");

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            var command = ClientCommandManager.literal("autovanishplayers");
            command.then(ClientCommandManager.literal("on").executes(cmd -> {
                isAutoVanishPlayersEnabled = true;
                cmd.getSource().sendFeedback(Component.literal("AutoVanishPlayers is now ON"));
                return 0;
            }));
            command.then(ClientCommandManager.literal("off").executes(cmd -> {
                isAutoVanishPlayersEnabled = false;
                cmd.getSource().sendFeedback(Component.literal("AutoVanishPlayers is now OFF"));
                return 0;
            }));
            command.executes(cmd -> {
                String onOff = isAutoVanishPlayersEnabled ? "ON" : "OFF";
                cmd.getSource().sendFeedback(Component.literal("AutoVanishPlayers is " + onOff));
                return 0;
            });
            dispatcher.register(command);
        });
	}
}
