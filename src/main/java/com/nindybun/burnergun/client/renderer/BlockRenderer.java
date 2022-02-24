package com.nindybun.burnergun.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.nindybun.burnergun.common.BurnerGun;
import com.nindybun.burnergun.common.capabilities.burnergunmk1.BurnerGunMK1Info;
import com.nindybun.burnergun.common.capabilities.burnergunmk1.BurnerGunMK1InfoProvider;
import com.nindybun.burnergun.common.capabilities.burnergunmk2.BurnerGunMK2Info;
import com.nindybun.burnergun.common.capabilities.burnergunmk2.BurnerGunMK2InfoProvider;
import com.nindybun.burnergun.common.items.burnergunmk1.BurnerGunMK1;
import com.nindybun.burnergun.common.items.burnergunmk2.BurnerGunMK2;
import com.nindybun.burnergun.util.WorldUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;


@Mod.EventBusSubscriber(modid = BurnerGun.MOD_ID, value = Dist.CLIENT)
public class BlockRenderer {
    private static final Logger LOGGER = LogManager.getLogger();
    public static void drawBoundingBoxAtBlockPos(PoseStack matrixStackIn, AABB aabbIn, float red, float green, float blue, float alpha, BlockPos pos, BlockPos aimed) {
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        double camX = cam.x, camY = cam.y, camZ = cam.z;

        matrixStackIn.pushPose();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        drawShapeOutline(matrixStackIn, Shapes.create(aabbIn), pos.getX() - camX, pos.getY() - camY, pos.getZ() - camZ, red, green, blue, alpha, pos, aimed);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        matrixStackIn.popPose();
    }

    private static void drawShapeOutline(PoseStack matrixStack, VoxelShape voxelShape, double originX, double originY, double originZ, float red, float green, float blue, float alpha, BlockPos pos, BlockPos aimed) {
        Matrix4f matrix4f = matrixStack.last().pose();

        MultiBufferSource.BufferSource renderTypeBuffer = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer bufferIn = renderTypeBuffer.getBuffer(RenderType.lines());

        voxelShape.forAllEdges((x0, y0, z0, x1, y1, z1) -> {
            if (!pos.equals(aimed)){
                bufferIn.vertex(matrix4f, (float) (x0 + originX), (float) (y0 + originY), (float) (z0 + originZ)).color(red, green, blue, alpha).endVertex();
                bufferIn.vertex(matrix4f, (float) (x1 + originX), (float) (y1 + originY), (float) (z1 + originZ)).color(red, green, blue, alpha).endVertex();
            }

        });

        renderTypeBuffer.endBatch(RenderType.lines());
    }

    public static void drawArea(ItemStack gun, Player player, AABB test, PoseStack matrixStack){
        BurnerGunMK1Info infoMK1 = gun.getCapability(BurnerGunMK1InfoProvider.burnerGunInfoMK1Capability).orElse(null);
        BurnerGunMK2Info infoMK2 = gun.getCapability(BurnerGunMK2InfoProvider.burnerGunInfoMK2Capability).orElse(null);
        BlockHitResult ray = WorldUtil.getLookingAt(player.level, player, ClipContext.Fluid.NONE, infoMK1 != null ? infoMK1.getRaycastRange() : infoMK2.getRaycastRange());
        if (player.level.getBlockState(ray.getBlockPos()) == Blocks.AIR.defaultBlockState())
            return;
        int xRad = infoMK1 != null ? infoMK1.getHorizontal() : infoMK2.getHorizontal();
        int yRad = infoMK1 != null ? infoMK1.getVertical() : infoMK2.getVertical();
        BlockPos aimedPos = ray.getBlockPos();
        if (ray.getType() != BlockHitResult.Type.BLOCK)
            return;
        Vec3 size = WorldUtil.getDim(ray, xRad, yRad, player);
        float[] color = new float[3];
        color[0] = infoMK1 != null ? infoMK1.getColor().getCompound(0).getFloat("Red") : infoMK2.getColor().getCompound(0).getFloat("Red");
        color[1] = infoMK1 != null ? infoMK1.getColor().getCompound(0).getFloat("Green") : infoMK2.getColor().getCompound(0).getFloat("Green");
        color[2] = infoMK1 != null ? infoMK1.getColor().getCompound(0).getFloat("Blue") : infoMK2.getColor().getCompound(0).getFloat("Blue");
        drawBoundingBoxAtBlockPos(matrixStack, test, color[0], color[1], color[2], 1.0F, aimedPos.relative(ray.getDirection()), aimedPos.relative(ray.getDirection()));
        drawBoundingBoxAtBlockPos(matrixStack, test, color[0], color[1], color[2], 1.0F, aimedPos, aimedPos.relative(ray.getDirection()));
        if (player.isCrouching())
            return;
        for (int xPos = aimedPos.getX() - (int)size.x(); xPos <= aimedPos.getX() + (int)size.x(); ++xPos){
            for (int yPos = aimedPos.getY() - (int)size.y(); yPos <= aimedPos.getY() + (int)size.y(); ++yPos){
                for (int zPos = aimedPos.getZ() - (int)size.z(); zPos <= aimedPos.getZ() + (int)size.z(); ++zPos){
                    BlockPos thePos = new BlockPos(xPos, yPos, zPos);
                    if (thePos != aimedPos && player.level.getBlockState(thePos) != Blocks.AIR.defaultBlockState() && player.level.getBlockState(thePos) != Blocks.CAVE_AIR.defaultBlockState())
                        drawBoundingBoxAtBlockPos(matrixStack, test, color[0], color[1], color[2], 1.0F, thePos, aimedPos);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderWorldEvent(RenderWorldLastEvent e) {
        final GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
        Player player = Minecraft.getInstance().player;
        ItemStack gun = !BurnerGunMK2.getGun(player).isEmpty() ? BurnerGunMK2.getGun(player) : BurnerGunMK1.getGun(player);
        if (gun.isEmpty())
            return;
        gameRenderer.resetProjectionMatrix(e.getProjectionMatrix());

        final AABB test = new AABB(0, 0, 0, 1, 1, 1);
        drawArea(gun, player, test, e.getMatrixStack());

        //drawBoundingBoxAtBlockPos(e.getMatrixStack(), test, 1.0F, 0.0F, 0.0F, 1.0F, new BlockPos(0, 65, 0), new BlockPos(0, 65, 0));
        //drawBoundingBoxAtBlockPos(e.getMatrixStack(), test, 1.0F, 0.0F, 0.0F, 1.0F, new BlockPos(1, 65, 0), new BlockPos(0, 65, 0));
        /*drawBoundingBoxAtBlockPos(e.getMatrixStack(), test, 1.0F, 0.0F, 0.0F, 1.0F, new BlockPos(0, 65, 1));
        drawBoundingBoxAtBlockPos(e.getMatrixStack(), test, 1.0F, 0.0F, 0.0F, 1.0F, new BlockPos(0, 65, -1));

        drawBoundingBoxAtBlockPos(e.getMatrixStack(), test, 1.0F, 0.0F, 0.0F, 1.0F, new BlockPos(0, 64, 0));
        drawBoundingBoxAtBlockPos(e.getMatrixStack(), test, 1.0F, 0.0F, 0.0F, 1.0F, new BlockPos(0, 64, 1));
        drawBoundingBoxAtBlockPos(e.getMatrixStack(), test, 1.0F, 0.0F, 0.0F, 1.0F, new BlockPos(0, 64, -1));

        drawBoundingBoxAtBlockPos(e.getMatrixStack(), test, 1.0F, 0.0F, 0.0F, 1.0F, new BlockPos(0, 66, 0));
        drawBoundingBoxAtBlockPos(e.getMatrixStack(), test, 1.0F, 0.0F, 0.0F, 1.0F, new BlockPos(0, 66, 1));
        drawBoundingBoxAtBlockPos(e.getMatrixStack(), test, 1.0F, 0.0F, 0.0F, 1.0F, new BlockPos(0, 66, -1));*/
    }

}
