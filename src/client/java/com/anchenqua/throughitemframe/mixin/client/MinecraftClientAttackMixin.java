package com.anchenqua.throughitemframe.mixin.client;

import com.anchenqua.throughitemframe.compat.FastItemFramesCompat;
import com.anchenqua.throughitemframe.compat.ItemFrameBlockCompat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 装了 Fast Item Frames 时，把“Shift + 右键隐藏/显示展示框”映射到 Shift + 左键。
 */
@Mixin(MinecraftClient.class)
public class MinecraftClientAttackMixin {

    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    private void throughItemFrame$toggleVisibilityOnLeftClick(CallbackInfo ci) {
        FastItemFramesCompat.restorePendingInput();

        if (!FastItemFramesCompat.isLoaded()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null || client.interactionManager == null) {
            return;
        }
        if (!player.isSneaking()) {
            return;
        }
        if (!(client.crosshairTarget instanceof BlockHitResult blockHit)
                || !ItemFrameBlockCompat.isItemFrameBlockWithItem(client.world, blockHit.getBlockPos())) {
            return;
        }
        if (!client.options.attackKey.wasPressed()) {
            return;
        }
        FastItemFramesCompat.toggleFrameVisibility(client, player, blockHit);
    }
}
