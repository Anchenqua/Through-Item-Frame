package com.anchenqua.throughitemframe;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class KeyBind {
    private static final InputConstants.Key DEFAULT_KEY = InputConstants.getKey("key.keyboard.f6");

    public static final String MOD_ID = "throughitemframe";

    public static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "key_category"));

    public static final KeyMapping TOGGLE_KEY = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.throughitemframe.toggle",
                    DEFAULT_KEY.getType(),
                    DEFAULT_KEY.getValue(),
                    CATEGORY
            )
    );

    public static void register() {
    }
}
