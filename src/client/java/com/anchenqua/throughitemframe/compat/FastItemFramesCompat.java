package com.anchenqua.throughitemframe.compat;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.hit.BlockHitResult;

/**
 * Fast Item Frames（FF）兼容层。
 *
 * <p>FF 加载时：Shift + 右键旋转物品、Shift + 左键隐藏/显示展示框；
 * 不加载时完全走原逻辑。</p>
 */
public final class FastItemFramesCompat {

    private static final String FAST_ITEM_FRAMES_MOD_ID = "fastitemframes";

    private static boolean suppressContainerRedirect = false;
    private static PlayerInput pendingRestoreInput = null;
    private static int restoreTicks = 0;
    private static int ignoreShiftSyncTicks = 0;

    private FastItemFramesCompat() {
    }

    public static boolean isLoaded() {
        return FabricLoader.getInstance().isModLoaded(FAST_ITEM_FRAMES_MOD_ID);
    }

    public static boolean isContainerRedirectSuppressed() {
        return suppressContainerRedirect;
    }

    public static boolean shouldIgnoreShiftSync() {
        return ignoreShiftSyncTicks > 0;
    }

    public static void toggleFrameVisibility(MinecraftClient client, ClientPlayerEntity player, BlockHitResult hitResult) {
        suppressContainerRedirect = true;
        try {
            client.interactionManager.interactBlock(player, Hand.MAIN_HAND, hitResult);
        } finally {
            suppressContainerRedirect = false;
        }
    }

    public static void sendNoShiftInput(ClientPlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() == null) {
            return;
        }
        PlayerInput original = player.getLastPlayerInput();
        if (original == null) {
            return;
        }
        PlayerInput noShift = new PlayerInput(
                original.forward(), original.backward(), original.left(), original.right(),
                original.jump(), false, original.sprint()
        );
        pendingRestoreInput = original;
        restoreTicks = 2;
        ignoreShiftSyncTicks = 5;
        client.getNetworkHandler().getConnection().send(new PlayerInputC2SPacket(noShift));
    }

    public static void restorePendingInput() {
        if (pendingRestoreInput == null) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() != null) {
            client.getNetworkHandler().getConnection().send(new PlayerInputC2SPacket(pendingRestoreInput));
        }
        pendingRestoreInput = null;
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (ignoreShiftSyncTicks > 0) {
                ignoreShiftSyncTicks--;
            }
            if (restoreTicks <= 0) {
                return;
            }
            restoreTicks--;
            ClientPlayerEntity player = client.player;
            if (player != null && client.options.sneakKey.isPressed() && !player.isSneaking()) {
                player.setSneaking(true);
            }
        });
    }
}
