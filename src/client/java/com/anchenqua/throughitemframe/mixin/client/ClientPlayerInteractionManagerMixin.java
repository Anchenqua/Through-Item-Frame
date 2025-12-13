package com.anchenqua.throughitemframe.mixin.client;

import com.anchenqua.throughitemframe.ModConfig;
import com.anchenqua.throughitemframe.ItemFrameHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Inject(
            method = "interactEntityAtLocation",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onInteractEntity(
            PlayerEntity player, Entity entity, EntityHitResult hitResult, Hand hand, CallbackInfoReturnable<ActionResult> cir
    ) {
        if (!ModConfig.getInstance().enabled) {
            return; // 不处理事件
        }
        if (entity instanceof ItemFrameEntity) {
            System.out.println("[ItemFrameContainer] Detected item frame interaction");
            System.out.println("[ItemFrameContainer] Player sneaking: " + player.isSneaking());
            System.out.println("[ItemFrameContainer] Hand used: " + hand);
        }

        if (hand == Hand.MAIN_HAND
                && !player.isSneaking()
                && entity instanceof ItemFrameEntity itemFrame) {

            ActionResult result = ItemFrameHandler.tryOpenContainerBehind(
                    (ClientPlayerEntity) player,
                    itemFrame,
                    hand
            );

            if (result == ActionResult.CONSUME) {
                System.out.println("[ItemFrameContainer] Successfully opened container behind frame");
                cir.setReturnValue(ActionResult.CONSUME);
            }
        }
    }
}