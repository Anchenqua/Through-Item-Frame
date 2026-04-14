package com.anchenqua.throughitemframe;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return new ConfigScreenFactory<Screen>() {
            @Override
            public Screen create(Screen parent) {
                // 获取配置持有者与当前配置实例
                var holder = AutoConfig.getConfigHolder(ModConfig.class);
                ModConfig config = holder.getConfig();

                // 创建配置构建器，标题使用已有翻译键
                ConfigBuilder builder = ConfigBuilder.create()
                        .setParentScreen(parent)
                        .setTitle(Component.translatable("text.autoconfig.itemframecontainer.title"));

                // 获取默认分类与条目构建器
                ConfigCategory general = builder.getOrCreateCategory(Component.translatable("text.autoconfig.itemframecontainer.title"));
                ConfigEntryBuilder entryBuilder = builder.entryBuilder();

                // 添加配置项：启用容器开启功能
                general.addEntry(entryBuilder.startBooleanToggle(
                                Component.translatable("text.autoconfig.itemframecontainer.option.enabled"),
                                config.enabled)
                        .setDefaultValue(true) // 根据你的 ModConfig 默认值调整
                        .setTooltip(Component.translatable("text.autoconfig.itemframecontainer.option.enabled.@Tooltip"))
                        .setSaveConsumer(newValue -> config.enabled = newValue)
                        .build());

                // 如有更多配置项，继续添加...

                // 设置保存回调
                builder.setSavingRunnable(holder::save);

                return builder.build();
            }
        };
    }
}