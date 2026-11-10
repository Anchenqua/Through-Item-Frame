package com.anchenqua.throughitemframe.compat;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Fast Item Frames（FF）兼容层。
 *
 * <p>FF 把展示框方块化之后，它的“Shift + 右键隐藏/显示展示框”由
 * {@code ItemFrameBlock.useItemOn} 的 secondary use 分支处理。这里把该功能映射到 Shift + 左键：
 * 左键时主动发送一次 secondary use（此时玩家确实按着 Shift），服务端 FF 就会切换隐形状态。</p>
 *
 * <p>另外，Shift + 右键需要走 FF 的“旋转”分支，所以会临时把服务端的潜行状态改成 false，
 * 发完 use 后下一 tick 再恢复。</p>
 */
public final class FastItemFramesCompat {

    private static final String FAST_ITEM_FRAMES_MOD_ID = "fastitemframes";

    private static boolean suppressContainerRedirect = false;
    private static Input pendingRestoreInput = null;
    private static int restoreTicks = 0;
    private static int ignoreShiftSyncTicks = 0;

    private FastItemFramesCompat() {
    }

    public static boolean isLoaded() {
        return FabricLoader.getInstance().isModLoaded(FAST_ITEM_FRAMES_MOD_ID);
    }

    /**
     * 本模组在发送这次 use 时不要把它重定向到后方容器。
     */
    public static boolean isContainerRedirectSuppressed() {
        return suppressContainerRedirect;
    }

    /**
     * 发送一次 secondary use，让 Fast Item Frames 切换展示框的隐形状态。
     */
    public static void toggleFrameVisibility(Minecraft client, LocalPlayer player, BlockHitResult hitResult) {
        suppressContainerRedirect = true;
        try {
            client.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hitResult);
        } finally {
            suppressContainerRedirect = false;
        }
    }

    /**
     * 把服务端的潜行状态临时改成 false（只发一次 input 包），
     * 让下一次 use 走 Fast Item Frames 的非 secondary（旋转）分支。
     * 原输入会在下一 tick 由 {@link #restorePendingInput()} 恢复。
     */
    public static void sendNoShiftInput(LocalPlayer player) {
        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() == null) {
            return;
        }
        Input original = player.getLastSentInput();
        if (original == null) {
            return;
        }
        Input noShift = new Input(
                original.forward(), original.backward(), original.left(), original.right(),
                original.jump(), false, original.sprint()
        );
        pendingRestoreInput = original;
        restoreTicks = 2;
        ignoreShiftSyncTicks = 5;
        client.getConnection().send(new ServerboundPlayerInputPacket(noShift));
    }

    /**
     * 恢复上一 tick 为了旋转而临时关闭的潜行状态。
     */
    public static void restorePendingInput() {
        if (pendingRestoreInput == null) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() != null) {
            client.getConnection().send(new ServerboundPlayerInputPacket(pendingRestoreInput));
        }
        pendingRestoreInput = null;
    }

    /**
     * 服务端把潜行状态改回 false 后，元数据同步会让本地玩家模型“站起来”一下。
     * 在这里的几 tick 内，如果玩家实际还按着 Shift，就把客户端状态按回去，避免动画闪烁。
     */
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (ignoreShiftSyncTicks > 0) {
                ignoreShiftSyncTicks--;
            }
            if (restoreTicks <= 0) {
                return;
            }
            restoreTicks--;
            LocalPlayer player = client.player;
            if (player != null && client.options.keyShift.isDown() && !player.isShiftKeyDown()) {
                player.setShiftKeyDown(true);
            }
        });
    }

    /**
     * 旋转后的几 tick 内，忽略服务端同步过来的“未潜行”状态，避免本地模型起立。
     */
    public static boolean shouldIgnoreShiftSync() {
        return ignoreShiftSyncTicks > 0;
    }
}
