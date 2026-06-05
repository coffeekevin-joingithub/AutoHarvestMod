package com.flier268.autoharvest;


import java.util.ArrayList;
import net.minecraft.client.Minecraft;
// import net.minecraft.world.entity.player.Inventory;
// import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.inventory.InventoryMenu;

public class TaskManager {
    private final ArrayList<Line> taskList = new ArrayList<>();

    enum Commands {
        MOVEITEM,
        SKIPTICK
    }

    static class Line {
        public Line(Commands command, Object... args) {
            Command = command;
            Args = args;
        }

        Commands Command;
        Object[] Args;
    }

    public void Add_MoveItem(int slotNumber, int currentHotbarSlot) {
        taskList.add(new Line(Commands.MOVEITEM, slotNumber, currentHotbarSlot));
    }

    public void Add_TickSkip(int skipTick) {
        for (int i = 0; i < skipTick; i++)
            taskList.add(new Line(Commands.SKIPTICK));
    }

    public int Count() {
        return taskList.size();
    }

    public void RunATask() {
        if (taskList.size() == 0)
            return;
        Line line = taskList.get(0);
        switch (line.Command) {
            case MOVEITEM:
                Minecraft mc = Minecraft.getInstance();
                assert mc.player != null;
                InventoryMenu container = mc.player.inventoryMenu;
                if ((int) line.Args[0] < 9) {
                // 更新至1.21.8原本資料變成不可直接存取，需要更改指令
                    Minecraft.getInstance().player.getInventory().
                    setSelectedSlot((int) line.Args[0]);
                } else {
                    assert mc.gameMode != null;
                    mc.gameMode.handleContainerInput(
                        container.containerId,  // 1. 容器 ID
                        (int) line.Args[0],     // 2. 點擊的格子編號 (Slot Num)
                        (int) line.Args[1],     // 3. 點擊快捷鍵編號 (buttonNum)
                        net.minecraft.world.inventory.ContainerInput.SWAP, // 4. 全新的 26.1 點擊輸入類型 
                        mc.player);             // 5. 玩家實體
                }
                break;
            case SKIPTICK:
                break;
        }
        taskList.remove(0);
    }
}
