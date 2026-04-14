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
                var holder = AutoConfig.getConfigHolder(ModConfig.class);
                ModConfig config = holder.getConfig();

                ConfigBuilder builder = ConfigBuilder.create()
                        .setParentScreen(parent)
                        .setTitle(Component.translatable("text.autoconfig.itemframecontainer.title"));

                ConfigCategory general = builder.getOrCreateCategory(Component.translatable("text.autoconfig.itemframecontainer.title"));
                ConfigEntryBuilder entryBuilder = builder.entryBuilder();

                general.addEntry(entryBuilder.startBooleanToggle(
                                Component.translatable("text.autoconfig.itemframecontainer.option.enabled"),
                                config.enabled)
                        .setDefaultValue(true) // 根据你的 ModConfig 默认值调整
                        .setTooltip(Component.translatable("text.autoconfig.itemframecontainer.option.enabled.@Tooltip"))
                        .setSaveConsumer(newValue -> config.enabled = newValue)
                        .build());

                builder.setSavingRunnable(holder::save);

                return builder.build();
            }
        };
    }
}