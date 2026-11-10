package com.anchenqua.throughitemframe.mixin.client;

import com.anchenqua.throughitemframe.compat.FastItemFramesCompat;
import com.anchenqua.throughitemframe.compat.ItemFrameBlockCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 装了 Fast Item Frames 时，把它的“Shift + 右键隐藏/显示展示框”映射到 Shift + 左键。
 *
 * <p>拦截点放在按键处理层（{@code handleKeybinds}），早于原版攻击和 FF 的攻击拦截，
 * 命中带物品的方块化展示框时消耗掉这次左键，改为发送一次 secondary use。</p>
 */
@Mixin(Minecraft.class)
public class MinecraftAttackMixin {

    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void throughItemFrame$toggleVisibilityOnLeftClick(CallbackInfo ci) {
        FastItemFramesCompat.restorePendingInput();

        if (!FastItemFramesCompat.isLoaded()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || client.level == null || client.gameMode == null) {
            return;
        }
        if (!player.isShiftKeyDown()) {
            return;
        }
        if (!(client.hitResult instanceof BlockHitResult blockHit)
                || !ItemFrameBlockCompat.isItemFrameBlockWithItem(client.level, blockHit.getBlockPos())) {
            return;
        }
        if (!client.options.keyAttack.consumeClick()) {
            return;
        }

        FastItemFramesCompat.toggleFrameVisibility(client, player, blockHit);
    }
}
