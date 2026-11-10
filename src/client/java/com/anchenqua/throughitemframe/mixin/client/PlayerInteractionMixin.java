package com.anchenqua.throughitemframe.mixin.client;

import com.anchenqua.throughitemframe.ItemFrameHandler;
import com.anchenqua.throughitemframe.compat.FastItemFramesCompat;
import com.anchenqua.throughitemframe.compat.ItemFrameBlockCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 交互入口：
 * <ul>
 *     <li>实体路径：点中“有物品的物品展示框实体”时直接在这里打开后方容器。相比 Fabric 的
 *     {@code UseEntityCallback}，这样不会额外补发 {@code ServerboundInteractPacket}，
 *     服务端也就不会把展示框里的物品旋转一下。</li>
 *     <li>方块路径：把命中“方块化展示框”（例如 Fast Item Frames）的交互重定向到其后方容器，
 *     后续的客户端预测、发包、挥手、右键冷却等都沿用原版流程。</li>
 * </ul>
 */
@Mixin(MultiPlayerGameMode.class)
public class PlayerInteractionMixin {

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void throughItemFrame$redirectEntityInteraction(
            Player player,
            Entity entity,
            EntityHitResult hitResult,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (player instanceof LocalPlayer localPlayer) {
            InteractionResult result = ItemFrameHandler.onUseEntity(localPlayer, localPlayer.level(), hand, entity, hitResult);
            if (result != InteractionResult.PASS) {
                cir.setReturnValue(result);
            }
        }
    }

    @ModifyVariable(method = "useItemOn", at = @At("HEAD"), argsOnly = true, index = 3)
    private BlockHitResult throughItemFrame$redirectToContainerBehind(BlockHitResult hitResult, LocalPlayer player, InteractionHand hand) {
        if (FastItemFramesCompat.isContainerRedirectSuppressed()) {
            return hitResult;
        }
        // Shift + 右键：让服务端按非 secondary 处理，触发 FF 的旋转而不是隐藏
        if (FastItemFramesCompat.isLoaded() && player.isShiftKeyDown()
                && ItemFrameBlockCompat.isItemFrameBlockWithItem(player.level(), hitResult.getBlockPos())) {
            FastItemFramesCompat.sendNoShiftInput(player);
            return hitResult;
        }
        BlockHitResult redirected = ItemFrameBlockCompat.redirectToContainerBehind(player, hand, hitResult);
        return redirected != null ? redirected : hitResult;
    }

    /**
     * Fast Item Frames 加载时，Shift + 左键不应该发出破坏包（FF 的 onBreakBlock 会让物品掉出来）。
     */
    @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void throughItemFrame$blockDestroy(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (throughItemFrame$shouldBlockDestroy(pos)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "continueDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void throughItemFrame$blockContinueDestroy(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (throughItemFrame$shouldBlockDestroy(pos)) {
            cir.setReturnValue(false);
        }
    }

    private static boolean throughItemFrame$shouldBlockDestroy(BlockPos pos) {
        if (!FastItemFramesCompat.isLoaded()) {
            return false;
        }
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || client.level == null || !player.isShiftKeyDown()) {
            return false;
        }
        return ItemFrameBlockCompat.isItemFrameBlockWithItem(client.level, pos);
    }
}

