package com.anchenqua.throughitemframe.mixin.client;

import com.anchenqua.throughitemframe.compat.FastItemFramesCompat;
import com.anchenqua.throughitemframe.compat.ItemFrameBlockCompat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 交互入口：
 * <ul>
 *     <li>不潜行右键：命中方块化展示框时重定向到后方容器。</li>
 *     <li>潜行右键（仅 FF 加载）：临时让服务端按非 secondary 处理，从而旋转物品。</li>
 *     <li>潜行左键：阻止破坏包，避免 FF 把展示框里的物品掉出来。</li>
 * </ul>
 */
@Mixin(ClientPlayerInteractionManager.class)
public class PlayerInteractionMixin {

    @ModifyVariable(method = "interactBlock", at = @At("HEAD"), argsOnly = true, index = 3)
    private BlockHitResult throughItemFrame$redirectToContainerBehind(BlockHitResult hitResult, ClientPlayerEntity player, Hand hand) {
        if (FastItemFramesCompat.isContainerRedirectSuppressed()) {
            return hitResult;
        }
        if (FastItemFramesCompat.isLoaded() && player.isSneaking()
                && ItemFrameBlockCompat.isItemFrameBlockWithItem(player.getWorld(), hitResult.getBlockPos())) {
            FastItemFramesCompat.sendNoShiftInput(player);
            return hitResult;
        }
        BlockHitResult redirected = ItemFrameBlockCompat.redirectToContainerBehind(player, hand, hitResult);
        return redirected != null ? redirected : hitResult;
    }

    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    private void throughItemFrame$blockDestroy(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (!FastItemFramesCompat.isLoaded()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null || !player.isSneaking()) {
            return;
        }
        if (ItemFrameBlockCompat.isItemFrameBlockWithItem(client.world, pos)) {
            cir.setReturnValue(false);
        }
    }
}
