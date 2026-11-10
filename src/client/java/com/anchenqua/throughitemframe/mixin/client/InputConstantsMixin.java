package com.anchenqua.throughitemframe.mixin.client;

import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 26.3 的 {@link InputConstants#isKeyDown(int)} 对 -1（未绑定键）没有做保护，
 * 任何 mod 注册了 GLFW_KEY_UNKNOWN 的默认键时，KeyMapping.setAll 都会越界崩溃。
 * 这里直接吞掉负数键，返回 false。
 */
@Mixin(InputConstants.class)
public class InputConstantsMixin {

    @Inject(method = "isKeyDown", at = @At("HEAD"), cancellable = true)
    private static void throughItemFrame$guardInvalidKey(int key, CallbackInfoReturnable<Boolean> cir) {
        if (key < 0) {
            cir.setReturnValue(false);
        }
    }
}
