package com.anchenqua.throughitemframe.mixin.client;

import com.anchenqua.throughitemframe.compat.FastItemFramesCompat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 旋转展示框时服务端会短暂同步“未潜行”，这里在旋转后的几 tick 内忽略该同步，
 * 避免本地玩家模型起立一帧。
 */
@Mixin(Entity.class)
public class EntityShiftMixin {

    @Inject(method = "setSneaking", at = @At("HEAD"), cancellable = true)
    private void throughItemFrame$ignoreServerShiftSync(boolean sneaking, CallbackInfo ci) {
        if (sneaking || !((Object) this instanceof ClientPlayerEntity)) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options != null
                && client.options.sneakKey.isPressed()
                && FastItemFramesCompat.shouldIgnoreShiftSync()) {
            ci.cancel();
        }
    }
}
