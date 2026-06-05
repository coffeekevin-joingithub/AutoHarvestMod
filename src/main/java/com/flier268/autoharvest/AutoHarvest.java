package com.flier268.autoharvest;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class AutoHarvest implements ClientModInitializer {
    public static final String MOD_NAME = "autoharvest";
    public static AutoHarvest instance;
    public HarvestMode mode = HarvestMode.FISHING;
    public int overlayRemainingTick = 0;
    public TickListener listener = null;
    public KeyPressListener KeyListener = null;

    TaskManager taskManager = new TaskManager();

    public boolean Switch = false;
    public Configure configure = new Configure();

    @Override
    public void onInitializeClient() {
        if (AutoHarvest.instance == null)
            AutoHarvest.instance = new AutoHarvest();
        if (AutoHarvest.instance.KeyListener == null) {
            AutoHarvest.instance.KeyListener = new KeyPressListener();
        }
        AutoHarvest.instance.configure.load();
    }

    public enum HarvestMode {
        HARVEST,  // Harvest only
        PLANT,  // Plant only
        Farmer,  //Harvest then re-plant
        SEED,   // Harvest seeds & flowers
        BONEMEALING,
        FEED,   // Feed animals
        FISHING;// Fishing
        private static HarvestMode[] vals = values();

        public AutoHarvest.HarvestMode next() {
            return vals[(this.ordinal() + 1) % vals.length];
        }
    }


    public HarvestMode toSpecifiedMode(HarvestMode mode) {
        //setDisabled();
        if (listener == null) {
            listener = new TickListener(configure, Minecraft.getInstance().player);
        } else
            listener.Reset();
        this.mode = mode;
        return mode;
    }

    public HarvestMode toNextMode() {
        //setDisabled();
        if (listener == null) {
            listener = new TickListener(configure, Minecraft.getInstance().player);
        } else
            listener.Reset();
        mode = mode.next();
        return mode;
    }

    public static void msg(String key, Object... obj) {
        if (Minecraft.getInstance() == null)
            return;
        if (Minecraft.getInstance().player == null)
            return;
        Minecraft.getInstance().player.sendSystemMessage(
			Component.translatable("notify.prefix")
            .append(Component.translatable(key, obj))
            // 這個 代表顯示在 Hotbar (快捷欄) 上方
    );
    }
}
