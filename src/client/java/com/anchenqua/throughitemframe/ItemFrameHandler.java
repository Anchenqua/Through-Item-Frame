package com.anchenqua.throughitemframe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class ItemFrameHandler {

    public static InteractionResult onUseEntity(Player player, Level world, InteractionHand hand, Entity entity, EntityHitResult hitResult) {
        if (world.isClientSide() && player instanceof LocalPlayer localPlayer) {
            return handleClientInteraction(localPlayer, world, hand, entity);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult handleClientInteraction(LocalPlayer player, Level world, InteractionHand hand, Entity entity) {
        if (!ModConfig.getInstance().enabled) {
            return InteractionResult.PASS;
        }

        if (hand != InteractionHand.MAIN_HAND || player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        if (!(entity instanceof ItemFrame itemFrame) || itemFrame.getItem().isEmpty()) {
            return InteractionResult.PASS;
        }

        return tryOpenContainerBehind(player, itemFrame, hand);
    }

    public static InteractionResult tryOpenContainerBehind(LocalPlayer player, ItemFrame itemFrame, InteractionHand hand) {
        Minecraft client = Minecraft.getInstance();
        Level world = client.level;
        if (world == null) return InteractionResult.PASS;

        Direction facing = itemFrame.getDirection();
        BlockPos attachedPos = itemFrame.blockPosition().relative(facing.getOpposite());

        BlockState state = world.getBlockState(attachedPos);
        MenuProvider menuProvider = state.getMenuProvider(world, attachedPos);

        if (menuProvider == null) {
            return InteractionResult.PASS;
        }

        openContainer(player, hand, facing, attachedPos);
        return InteractionResult.CONSUME;
    }

    private static void openContainer(LocalPlayer player, InteractionHand hand, Direction facing, BlockPos containerPos) {
        Minecraft client = Minecraft.getInstance();
        // 计算击中的精确位置（方块中心略微偏向点击方向）
        Direction clickFace = facing.getOpposite();
        Vec3 hitPos = new Vec3(
                containerPos.getX() + 0.5 + clickFace.getStepX() * 0.5,
                containerPos.getY() + 0.5 + clickFace.getStepY() * 0.5,
                containerPos.getZ() + 0.5 + clickFace.getStepZ() * 0.5
        );

        BlockHitResult blockHit = new BlockHitResult(hitPos, clickFace, containerPos, false);

        if (client.gameMode != null) {
            client.gameMode.useItemOn(player, hand, blockHit);
        }
    }
}