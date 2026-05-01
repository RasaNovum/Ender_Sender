package net.rasanovum.endersender.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.rasanovum.endersender.EnderSender;
import net.rasanovum.endersender.block.entity.EnderSenderBlockEntity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.Optional;

public class EnderSenderBlockEntityRenderer implements BlockEntityRenderer<EnderSenderBlockEntity> {
    private static final ResourceLocation EYE_TEXTURE = new ResourceLocation(EnderSender.MOD_ID, "textures/block/ender_siphon_eye.png");
    private static final int MIN_IDLE_LOOK_TICKS = 20;
    private static final int RANDOM_IDLE_LOOK_TICKS = 50;

    public EnderSenderBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(EnderSenderBlockEntity be, float tickDelta, PoseStack matrices, MultiBufferSource buffers, int light, int overlay) {
        Level level = be.getLevel();
        if (level == null) return;

        updateYaw(be, level.getGameTime());
        EnderSenderBlockEntity.EyeAnimation eye = be.eyeAnimation;
        float yaw = Mth.rotLerp(tickDelta, eye.prevYaw, eye.yaw);
        float pitch = Mth.lerp(tickDelta, eye.prevPitch, eye.pitch);

        matrices.pushPose();
        matrices.translate(0.5F, 13.0F / 16.0F, 0.5F);
        matrices.mulPose(Axis.YP.rotationDegrees(-yaw));
        matrices.mulPose(Axis.XP.rotationDegrees(pitch));

        VertexConsumer vertices = buffers.getBuffer(RenderType.entityCutout(EYE_TEXTURE));
        drawEye(matrices, vertices, light, overlay);
        matrices.popPose();
    }

    private static void updateYaw(EnderSenderBlockEntity be, long tick) {
        EnderSenderBlockEntity.EyeAnimation eye = be.eyeAnimation;
        if (eye.lastTick == tick) return;

        eye.lastTick = tick;
        eye.prevYaw = eye.yaw;
        eye.prevPitch = eye.pitch;

        EyeTarget target = target(be, tick);
        eye.yaw += Mth.wrapDegrees(target.yaw() - eye.yaw) * 0.25F;
        eye.pitch += (target.pitch() - eye.pitch) * 0.25F;
    }

    private static EyeTarget target(EnderSenderBlockEntity be, long tick) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return idleTarget(be, tick);

        BlockPos pos = be.getBlockPos();
        Optional<Integer> radius = ClientStockCache.getRadius(pos);
        if (radius.isEmpty()) return idleTarget(be, tick);

        double dx = player.getX() - (pos.getX() + 0.5D);
        double dy = player.getY() - (pos.getY() + 0.5D);
        double dz = player.getZ() - (pos.getZ() + 0.5D);
        int radiusValue = radius.get();

        if ((dx * dx) + (dy * dy) + (dz * dz) <= radiusValue * radiusValue) {
            return new EyeTarget((float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F, 15.0F);
        }
        return idleTarget(be, tick);
    }

    private static EyeTarget idleTarget(EnderSenderBlockEntity be, long tick) {
        EnderSenderBlockEntity.EyeAnimation eye = be.eyeAnimation;
        if (eye.nextIdleTick <= tick) {
            RandomSource random = RandomSource.create(be.getBlockPos().asLong() ^ tick);
            eye.idleYaw = random.nextFloat() * 360.0F - 180.0F;
            eye.nextIdleTick = tick + MIN_IDLE_LOOK_TICKS + random.nextInt(RANDOM_IDLE_LOOK_TICKS);
        }
        return new EyeTarget(eye.idleYaw, 0.0F);
    }

    private record EyeTarget(float yaw, float pitch) {}

    private static void drawEye(PoseStack matrices, VertexConsumer vertices, int light, int overlay) {
        PoseStack.Pose pose = matrices.last();
        Matrix4f position = pose.pose();
        Matrix3f normal = pose.normal();
        float minX = -4.0F / 16.0F;
        float maxX = 4.0F / 16.0F;
        float minY = 0.0F;
        float maxY = 3.0F / 16.0F;
        float minZ = -4.0F / 16.0F;
        float maxZ = 4.0F / 16.0F;

        quad(vertices, position, normal, minX, minY, minZ, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ, 0, 12, 8, 8, 0, 0, -1, light, overlay);
        quad(vertices, position, normal, maxX, minY, maxZ, minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ, 0, 12, 8, 8, 0, 0, 1, light, overlay);
        quad(vertices, position, normal, minX, minY, maxZ, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ, 0, 12, 8, 8, -1, 0, 0, light, overlay);
        quad(vertices, position, normal, maxX, minY, minZ, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, 0, 12, 8, 8, 1, 0, 0, light, overlay);
        quad(vertices, position, normal, minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, minX, maxY, maxZ, 0, 12, 8, 20, 0, 1, 0, light, overlay);
        quad(vertices, position, normal, minX, minY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, minX, minY, minZ, 0, 0, 8, 8, 0, -1, 0, light, overlay);
    }

    private static void quad(VertexConsumer vertices, Matrix4f position, Matrix3f normal,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3, float x4, float y4, float z4,
                             float u1, float v1, float u2, float v2,
                             float nx, float ny, float nz, int light, int overlay) {
        vertex(vertices, position, normal, x4, y4, z4, u1, v1, nx, ny, nz, light, overlay);
        vertex(vertices, position, normal, x3, y3, z3, u2, v1, nx, ny, nz, light, overlay);
        vertex(vertices, position, normal, x2, y2, z2, u2, v2, nx, ny, nz, light, overlay);
        vertex(vertices, position, normal, x1, y1, z1, u1, v2, nx, ny, nz, light, overlay);
    }

    private static void vertex(VertexConsumer vertices, Matrix4f position, Matrix3f normal,
                               float x, float y, float z, float u, float v,
                               float nx, float ny, float nz, int light, int overlay) {
        vertices.vertex(position, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u / 8, v / 20)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(normal, nx, ny, nz)
                .endVertex();
    }
}
