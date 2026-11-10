package com.anchenqua.throughitemframe.compat;

import com.anchenqua.throughitemframe.ModConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemFrameItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.lang.reflect.Method;

/**
 * 兼容“方块化”的物品展示框（例如 Fast Item Frames）。
 *
 * <p>这类模组会把展示框实体替换成方块，因此实体交互不会触发，本模组会把命中点
 * 重定向到展示框后面的容器。只在玩家没有潜行时生效；潜行右键交给
 * {@link FastItemFramesCompat} 处理成“旋转”。</p>
 */
public final class ItemFrameBlockCompat {

    private static final String FAST_ITEM_FRAMES_PACKAGE = "fuzs.fastitemframes.";

    private ItemFrameBlockCompat() {
    }

    public static BlockHitResult redirectToContainerBehind(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult) {
        if (FastItemFramesCompat.isContainerRedirectSuppressed()) {
            return null;
        }
        if (!ModConfig.getInstance().enabled || hand != Hand.MAIN_HAND || player.isSneaking()) {
            return null;
        }

        World world = MinecraftClient.getInstance().world;
        if (world == null) {
            return null;
        }

        BlockPos framePos = hitResult.getBlockPos();
        BlockState frameState = world.getBlockState(framePos);

        if (!isItemFrameBlock(world, framePos, frameState) || !frameContainsItem(world, framePos)) {
            return null;
        }
        if (!frameState.contains(Properties.FACING)) {
            return null;
        }

        Direction facing = frameState.get(Properties.FACING);
        Direction clickFace = facing.getOpposite();
        BlockPos containerPos = framePos.offset(clickFace);

        NamedScreenHandlerFactory factory = world.getBlockState(containerPos)
                .createScreenHandlerFactory(world, containerPos);
        if (factory == null) {
            return null;
        }

        Vec3d hitPos = new Vec3d(
                containerPos.getX() + 0.5 + clickFace.getOffsetX() * 0.5,
                containerPos.getY() + 0.5 + clickFace.getOffsetY() * 0.5,
                containerPos.getZ() + 0.5 + clickFace.getOffsetZ() * 0.5
        );
        return new BlockHitResult(hitPos, clickFace, containerPos, false);
    }

    public static boolean isItemFrameBlockWithItem(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return isItemFrameBlock(world, pos, state) && frameContainsItem(world, pos);
    }

    private static boolean isItemFrameBlock(World world, BlockPos pos, BlockState state) {
        if (state.getBlock().asItem() instanceof ItemFrameItem) {
            return true;
        }
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity != null && blockEntity.getClass().getName().startsWith(FAST_ITEM_FRAMES_PACKAGE);
    }

    private static boolean frameContainsItem(World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity == null) {
            return false;
        }
        if (blockEntity instanceof Inventory inventory) {
            return !inventory.isEmpty();
        }
        return fastItemFramesContainsItem(blockEntity);
    }

    private static boolean fastItemFramesContainsItem(BlockEntity blockEntity) {
        if (!blockEntity.getClass().getName().startsWith(FAST_ITEM_FRAMES_PACKAGE)) {
            return false;
        }
        try {
            Method getItem = blockEntity.getClass().getMethod("getItem");
            Object item = getItem.invoke(blockEntity);
            return item instanceof ItemStack stack && !stack.isEmpty();
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }
}
