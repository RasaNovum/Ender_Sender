package net.rasanovum.endersender.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.rasanovum.endersender.EnderSenderScreenHandler;

public class EnderSenderScreen extends AbstractContainerScreen<EnderSenderScreenHandler> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");

    public EnderSenderScreen(EnderSenderScreenHandler handler, net.minecraft.world.entity.player.Inventory inventory, net.minecraft.network.chat.Component title) {
        super(handler, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = 72;
        this.titleLabelY = 6;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // ender sender container
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, 71);

        // inventory label and "container"
        graphics.blit(TEXTURE, x, y + 71, 0, 126, this.imageWidth, 95);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}