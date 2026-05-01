package net.rasanovum.endersender;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.rasanovum.endersender.block.EnderSenderBlock;
import net.rasanovum.endersender.block.entity.EnderSenderBlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnderSender implements ModInitializer {
	public static final String MOD_ID = "ender_sender";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final GameRules.Key<GameRules.BooleanValue> DO_ENDER_SENDER_EFFECTS =
			GameRuleRegistry.register("doEnderSenderEffects", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));

	public static final GameRules.Key<GameRules.IntegerValue> ENDER_SENDER_RADIUS =
			GameRuleRegistry.register("enderSenderRange", GameRules.Category.MISC, GameRuleFactory.createIntRule(32));

	public static final Block ENDER_SENDER_BLOCK = Registry.register(
			BuiltInRegistries.BLOCK,
			new ResourceLocation(MOD_ID, "ender_sender"),
			new EnderSenderBlock(BlockBehaviour.Properties.of()
					.strength(3.0f)
					.mapColor(MapColor.COLOR_PURPLE)
					.noOcclusion())
	);

	public static final BlockItem ENDER_SENDER_ITEM = Registry.register(
			BuiltInRegistries.ITEM,
			new ResourceLocation(MOD_ID, "ender_sender"),
			new BlockItem(ENDER_SENDER_BLOCK, new Item.Properties())
	);

	public static final BlockEntityType<EnderSenderBlockEntity> ENDER_SENDER_BE = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			new ResourceLocation(MOD_ID, "ender_sender"),
			FabricBlockEntityTypeBuilder.create(EnderSenderBlockEntity::new, ENDER_SENDER_BLOCK).build()
	);

	public static final MenuType<EnderSenderScreenHandler> ENDER_SENDER_SCREEN_HANDLER =
			Registry.register(
					BuiltInRegistries.MENU,
					new ResourceLocation(MOD_ID, "ender_sender"),
					new MenuType<>(EnderSenderScreenHandler::new, FeatureFlags.VANILLA_SET)
			);

	@Override
	public void onInitialize() {
		BlockPlaceHandler.register();
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(content -> {
			content.accept(ENDER_SENDER_ITEM);
		});
		LOGGER.info("Hello Fabric world!");
	}
}