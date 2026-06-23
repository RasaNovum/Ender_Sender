package net.rasanovum.endersender.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.rasanovum.endersender.client.ClientStockCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiGraphics.class)
public abstract class ItemCountRenderMixin {

    @Unique
    private boolean isEnderSenderCount = false;

    @ModifyVariable(
            method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private String enderSender$overrideRenderText(String originalText, Font font, ItemStack stack, int x, int y) {
        Minecraft client = Minecraft.getInstance();
        this.isEnderSenderCount = false;
        if (client.player == null || client.level == null || stack.isEmpty()) return originalText;

        boolean isMainHand = stack == client.player.getMainHandItem();
        boolean isOffHand = stack == client.player.getOffhandItem();
        if (client.screen == null && (isMainHand || isOffHand)) {
            BlockPos playerPos = client.player.blockPosition();
            ResourceLocation dimension = client.level.dimension().location();

            if (isOffHand && !ClientStockCache.isOffhandAllowedNearby(dimension, playerPos)) {
                return originalText;
            }

            int senderStock = ClientStockCache.getStockForNearbySender(dimension, playerPos, stack.getItem());
            if (senderStock > 0) {
                int totalDisplayed = senderStock + stack.getCount();
                this.isEnderSenderCount = true;
                return totalDisplayed > 64 ? "64+" : String.valueOf(totalDisplayed); // TODO: Figure out how we want to word this
            }
        }
        return originalText;
    }


    @Redirect(
            method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I")
    )
    private int enderSender$changeColorOnDraw(GuiGraphics instance, Font font, String text, int x, int y, int color, boolean shadow) {
        int finalColor = this.isEnderSenderCount ? 0xDAB4FF : color;

        return instance.drawString(font, text, x, y, finalColor, shadow);
    }
}
