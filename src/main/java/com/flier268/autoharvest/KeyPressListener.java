package com.flier268.autoharvest;

import com.flier268.autoharvest.Plugin.ClothConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
// fabric-api-0.140.3+26.1
// 開始配合微軟取消反混淆代碼的文化，使得KeyMappingHelper變成KeyMappingHelper
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class KeyPressListener {

    private final KeyMapping key_Switch;
    private final KeyMapping key_ModeChange;
    private final KeyMapping key_Config;
    private final KeyMapping key_HARVEST;
    private final KeyMapping key_PLANT;
    private final KeyMapping key_Farmer;
    private final KeyMapping key_SEED;
    private final KeyMapping key_FEED;
    private final KeyMapping key_FISHING;
    private final KeyMapping Key_BONEMEALING;

    public KeyPressListener() {
        // String categoryGeneral = Text.translatable("key.category.general").getString();
        // String categorySwitchTo = Text.translatable("key.category.switchTo").getString();
        key_ModeChange = new KeyMapping(
            "key.general.modechange",
            GLFW.GLFW_KEY_H,
            // categoryGeneral
            KeyMapping.Category.MISC
        );
        key_Switch = new KeyMapping(
            "key.general.switch",
                GLFW.GLFW_KEY_J,
                // categoryGeneral
                KeyMapping.Category.MISC
        );
        key_Config = new KeyMapping(
            "key.general.config",
                GLFW.GLFW_KEY_K,
                // categoryGeneral
                KeyMapping.Category.MISC
        );
        key_HARVEST = new KeyMapping("harvest",
                GLFW.GLFW_KEY_UNKNOWN,
                // categorySwitchTo
                KeyMapping.Category.MISC
        );
        key_PLANT = new KeyMapping("plant",
                GLFW.GLFW_KEY_UNKNOWN,
                // categorySwitchTo
                KeyMapping.Category.MISC
        );
        key_Farmer = new KeyMapping("farmer",
                GLFW.GLFW_KEY_UNKNOWN,
                // categorySwitchTo
                KeyMapping.Category.MISC
        );
        key_SEED = new KeyMapping("seed",
                GLFW.GLFW_KEY_UNKNOWN,
                // categorySwitchTo
                KeyMapping.Category.MISC
        );
        key_FEED = new KeyMapping("feed",
                GLFW.GLFW_KEY_UNKNOWN,
                // categorySwitchTo
                KeyMapping.Category.MISC
        );
        key_FISHING = new KeyMapping("fishing",
                GLFW.GLFW_KEY_UNKNOWN,
                // categorySwitchTo
                KeyMapping.Category.MISC
        );
        Key_BONEMEALING = new KeyMapping("bonemealing",
                GLFW.GLFW_KEY_UNKNOWN,
                // categorySwitchTo
                KeyMapping.Category.MISC
        );
        KeyMappingHelper.registerKeyMapping(key_ModeChange);
        KeyMappingHelper.registerKeyMapping(key_Switch);
        KeyMappingHelper.registerKeyMapping(key_Config);
        KeyMappingHelper.registerKeyMapping(key_HARVEST);
        KeyMappingHelper.registerKeyMapping(key_PLANT);
        KeyMappingHelper.registerKeyMapping(key_Farmer);
        KeyMappingHelper.registerKeyMapping(key_SEED);
        KeyMappingHelper.registerKeyMapping(key_FEED);
        KeyMappingHelper.registerKeyMapping(key_FISHING);
        KeyMappingHelper.registerKeyMapping(Key_BONEMEALING);

        ClientTickEvents.END_CLIENT_TICK.register(client -> onProcessKey());
    }

    public void onProcessKey() {
        if (key_Switch.consumeClick()) {
            AutoHarvest.instance.Switch = !AutoHarvest.instance.Switch;
            AutoHarvest.msg("notify.turn." + (AutoHarvest.instance.Switch ? "on" : "off"));
        } else if (key_Config.consumeClick()) {
            Minecraft.getInstance().setScreen(ClothConfig.openConfigScreen(Minecraft.getInstance().screen));
        } else {
            String modeName = null;
            if (key_ModeChange.consumeClick()) {
                if (AutoHarvest.instance.overlayRemainingTick == 0) {
                    AutoHarvest.instance.overlayRemainingTick = 60;
                    modeName = AutoHarvest.instance.mode.toString().toLowerCase();
                } else {
                    AutoHarvest.instance.overlayRemainingTick = 60;
                    modeName = AutoHarvest.instance.toNextMode().toString().toLowerCase();
                }
            } else if (key_HARVEST.consumeClick()) {
                modeName = AutoHarvest.instance.toSpecifiedMode(AutoHarvest.HarvestMode.HARVEST).toString().toLowerCase();
            } else if (key_PLANT.consumeClick()) {
                modeName = AutoHarvest.instance.toSpecifiedMode(AutoHarvest.HarvestMode.PLANT).toString().toLowerCase();
            } else if (key_Farmer.consumeClick()) {
                modeName = AutoHarvest.instance.toSpecifiedMode(AutoHarvest.HarvestMode.Farmer).toString().toLowerCase();
            } else if (key_SEED.consumeClick()) {
                modeName = AutoHarvest.instance.toSpecifiedMode(AutoHarvest.HarvestMode.SEED).toString().toLowerCase();
            } else if (key_FEED.consumeClick()) {
                modeName = AutoHarvest.instance.toSpecifiedMode(AutoHarvest.HarvestMode.FEED).toString().toLowerCase();
            } else if (key_FISHING.consumeClick()) {
                modeName = AutoHarvest.instance.toSpecifiedMode(AutoHarvest.HarvestMode.FISHING).toString().toLowerCase();
            } else if (Key_BONEMEALING.consumeClick()) {
                modeName = AutoHarvest.instance.toSpecifiedMode(AutoHarvest.HarvestMode.BONEMEALING).toString().toLowerCase();
            }
            if (modeName != null)
                AutoHarvest.msg("notify.switch_to", Component.translatable(modeName).getString());
        }
    }
}
