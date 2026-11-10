package com.anchenqua.throughitemframe.compat;

import com.anchenqua.throughitemframe.ModConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemFrameItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * 兼容“方块化”的物品展示框（例如 Fast Item Frames）。
 *
 * <p>这类模组会把展示框实体替换成方块，因此 {@code UseEntityCallback} 完全不会触发，
 * 本模组原有的“穿过展示框”逻辑也就失效了。这里在方块交互入口处识别这种展示框方块，
 * 并把命中点重定向到它后面的容器，从而复用原版完整的交互流程。</p>
 *
 * <p>识别方式不依赖任何第三方模组的类：优先看方块物品是否为 {@link ItemFrameItem}，
 * 再用方块实体判断框内是否有物品（1.20.x~1.21.x 的 Fast Item Frames 方块实体内部持有
 * 一个客户端实体，没有实现 Container，因此额外做一次反射兜底）。</p>
 */
public final class ItemFrameBlockCompat {

    private static final String FAST_ITEM_FRAMES_PACKAGE = "fuzs.fastitemframes.";

    private ItemFrameBlockCompat() {
    }

    /**
     * 如果本次点击命中的是“有物品的方块化展示框”，返回指向它后方容器的命中结果；否则返回 {@code null}。
     */
    @Nullable
    public static BlockHitResult redirectToContainerBehind(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult) {
        if (FastItemFramesCompat.isContainerRedirectSuppressed()) {
            return null;
        }

        if (!ModConfig.getInstance().enabled || hand != InteractionHand.MAIN_HAND || player.isShiftKeyDown()) {
            return null;
        }

        Level level = player.level();
        BlockPos framePos = hitResult.getBlockPos();
        BlockState frameState = level.getBlockState(framePos);

        if (!isItemFrameBlock(level, framePos, frameState) || !frameContainsItem(level, framePos)) {
            return null;
        }

        if (!frameState.hasProperty(BlockStateProperties.FACING)) {
            return null;
        }

        Direction facing = frameState.getValue(BlockStateProperties.FACING);
        Direction clickFace = facing.getOpposite();
        BlockPos containerPos = framePos.relative(clickFace);

        BlockState containerState = level.getBlockState(containerPos);
        MenuProvider menuProvider = containerState.getMenuProvider(level, containerPos);
        if (menuProvider == null) {
            return null;
        }

        Vec3 hitPos = new Vec3(
                containerPos.getX() + 0.5 + clickFace.getStepX() * 0.5,
                containerPos.getY() + 0.5 + clickFace.getStepY() * 0.5,
                containerPos.getZ() + 0.5 + clickFace.getStepZ() * 0.5
        );
        return new BlockHitResult(hitPos, clickFace, containerPos, false);
    }

    /**
     * 判断该位置是否是“带物品的方块化展示框”。
     */
    public static boolean isItemFrameBlockWithItem(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return isItemFrameBlock(level, pos, state) && frameContainsItem(level, pos);
    }

    private static boolean isItemFrameBlock(Level level, BlockPos pos, BlockState state) {
        if (state.getBlock().asItem() instanceof ItemFrameItem) {
            return true;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity != null && blockEntity.getClass().getName().startsWith(FAST_ITEM_FRAMES_PACKAGE);
    }

    private static boolean frameContainsItem(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return false;
        }
        if (blockEntity instanceof Container container) {
            return !container.isEmpty();
        }
        return fastItemFramesContainsItem(blockEntity);
    }

    /**
     * Fast Item Frames 1.20.x~1.21.x 的方块实体没有实现 Container，只暴露了 {@code getItem()}。
     * 这里通过反射读取，避免对本模组引入硬依赖。
     */
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
