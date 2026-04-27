package net.rasanovum.endersender.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.rasanovum.endersender.EnderSender;

public class EnderSenderClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(EnderSender.ENDER_SENDER_SCREEN_HANDLER, EnderSenderScreen::new);
	}
}