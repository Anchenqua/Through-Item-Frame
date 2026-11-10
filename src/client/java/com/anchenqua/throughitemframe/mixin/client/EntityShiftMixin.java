package com.anchenqua.throughitemframe.mixin.client;

import com.anchenqua.throughitemframe.compat.FastItemFramesCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 旋转展示框时我们会临时让服务端认为玩家没有潜行，服务端随后同步回来的
 * “未潜行”状态会让本地玩家模型起立一帧。这里在旋转后的几 tick 内忽略这次同步，
 * 客户端始终按玩家真实按键状态保持潜行。
 */
@Mixin(Entity.class)
public class EntityShiftMixin {

    @Inject(method = "setShiftKeyDown", at = @At("HEAD"), cancellable = true)
    private void throughItemFrame$ignoreServerShiftSync(boolean shiftKeyDown, CallbackInfo ci) {
        if (shiftKeyDown || !((Object) this instanceof LocalPlayer)) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.options != null
                && client.options.keyShift.isDown()
                && FastItemFramesCompat.shouldIgnoreShiftSync()) {
            ci.cancel();
        }
    }
}
