/*
 * 本模组采用 知识共享 署名-非商业性使用 4.0 国际 许可证 (CC BY-NC 4.0)
 *
 * 允许：
 * - 自由使用、修改、分发
 * - 基于本模组创作衍生作品
 *
 * 要求：
 * - 必须标注原作者和原始项目链接
 * - 不得用于任何商业用途
 *
 * 完整许可证：https://creativecommons.org/licenses/by-nc/4.0/
 */
package com.anchenqua.throughitemframe;

import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;

public class ThroughItemFrameClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
        ModConfig.init();
        KeyBind.register();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // 使用官方文档推荐的 while 循环处理点击
            while (KeyBind.TOGGLE_KEY.consumeClick()) {
                ModConfig config = ModConfig.getInstance();
                config.enabled = !config.enabled;
                AutoConfig.getConfigHolder(ModConfig.class).save();

                if (client.player != null) {
                    Component message = Component.translatable(
                            config.enabled ? "message.throughitemframe.enabled" : "message.throughitemframe.disabled"
                    );
                    client.player.sendSystemMessage(message);
                }
            }
        });
        AutoConfig.getConfigHolder(ModConfig.class).registerSaveListener((manager, config) -> {
            System.out.println("[ItemFrameContainer] Config saved: enabled=" + config.enabled);
            return InteractionResult.SUCCESS;
        });
        UseEntityCallback.EVENT.register(ItemFrameHandler::onUseEntity);
	}
}