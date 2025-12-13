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
import net.minecraft.util.ActionResult;

public class ThroughItemFrameClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
        ModConfig.init();

        AutoConfig.getConfigHolder(ModConfig.class).registerSaveListener((manager, config) -> {
            System.out.println("[ItemFrameContainer] Config saved: enabled=" + config.enabled);
            return ActionResult.SUCCESS;
        });
	}
}