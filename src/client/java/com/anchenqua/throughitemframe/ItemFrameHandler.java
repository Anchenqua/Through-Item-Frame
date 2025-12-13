package com.anchenqua.throughitemframe;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemFrameHandler {
    public static ActionResult tryOpenContainerBehind(
            ClientPlayerEntity player,
            ItemFrameEntity itemFrame,
            Hand hand
    ) {
        if (!ModConfig.getInstance().enabled) {
            return ActionResult.PASS;
        }
        if (itemFrame.getHeldItemStack().isEmpty()) {
            return ActionResult.PASS;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        World world = client.world;
        if (world == null) return ActionResult.PASS;

        Direction facing = itemFrame.getHorizontalFacing();
        BlockPos attachedPos = itemFrame.getBlockPos().offset(facing.getOpposite());

        BlockState state = world.getBlockState(attachedPos);
        NamedScreenHandlerFactory factory = state.createScreenHandlerFactory(world, attachedPos);

        if (factory == null) {
            return ActionResult.PASS;
        }

        openContainer(player, hand, itemFrame, attachedPos);
        return ActionResult.CONSUME;
    }

    private static void openContainer(
            ClientPlayerEntity player,
            Hand hand,
            ItemFrameEntity itemFrame,
            BlockPos containerPos
    ) {
        MinecraftClient client = MinecraftClient.getInstance();
        Direction face = itemFrame.getHorizontalFacing().getOpposite();

        Vec3d hitPos = new Vec3d(
                containerPos.getX() + 0.5 + face.getUnitVector().x * 0.5,
                containerPos.getY() + 0.5 + face.getUnitVector().y * 0.5,
                containerPos.getZ() + 0.5 + face.getUnitVector().z * 0.5
        );

        BlockHitResult blockHit = new BlockHitResult(
                hitPos,
                face,
                containerPos,
                false
        );

        if (client.interactionManager != null) {
            client.interactionManager.interactBlock(
                    player,
                    hand,
                    blockHit
            );
        }
    }
}