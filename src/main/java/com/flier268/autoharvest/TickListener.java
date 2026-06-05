package com.flier268.autoharvest;

import java.util.Collection;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class TickListener {
    private final Configure configure;
    private LocalPlayer p;

    private long fishBitesAt = 0L;
    private ItemStack lastUsedItem = null;

    public TickListener(Configure configure, LocalPlayer player) {
        this.configure = configure;
        this.p = player;
        ClientTickEvents.END_CLIENT_TICK.register(e -> {
            if (AutoHarvest.instance.overlayRemainingTick > 0) {
                AutoHarvest.instance.overlayRemainingTick--;
            }
            if (AutoHarvest.instance.Switch)
                onTick(e.player);
        });
    }

    public void Reset() {
        lastUsedItem = null;
        fishBitesAt = 0L;
    }

    public void onTick(LocalPlayer player) {
        try {
            if (player != p) {
                this.p = player;
                AutoHarvest.instance.Switch = false;
                AutoHarvest.msg("notify.turn.off");
                return;
            }
            if (AutoHarvest.instance.taskManager.Count() > 0) {
                AutoHarvest.instance.taskManager.RunATask();
                return;
            }
            switch (AutoHarvest.instance.mode) {
                case SEED -> seedTick();
                case HARVEST -> harvestTick();
                case PLANT -> plantTick();
                case Farmer -> {
                    harvestTick();
                    plantTick();
                }
                case FEED -> feedTick();
                case FISHING -> fishingTick();
                case BONEMEALING -> bonemealingTick();
            }
            if (AutoHarvest.instance.mode != AutoHarvest.HarvestMode.FISHING)
                AutoHarvest.instance.taskManager.Add_TickSkip(AutoHarvest.instance.configure.tickSkip.value);
        } catch (Exception ex) {
            AutoHarvest.msg("notify.tick_error");
            AutoHarvest.msg("notify.turn.off");
            ex.printStackTrace();
            AutoHarvest.instance.Switch = false;
        }
    }

    /* clear all grass on land */
    private void seedTick() {
        Level w = p.level();
        int X = (int) Math.floor(p.getX());
        int Y = (int) Math.floor(p.getY());// the "leg block"
        int Z = (int) Math.floor(p.getZ());
        for (int deltaY = 3; deltaY >= -2; --deltaY)
            for (int deltaX = -configure.effect_radius.value; deltaX <= configure.effect_radius.value; ++deltaX)
                for (int deltaZ = -configure.effect_radius.value; deltaZ <= configure.effect_radius.value; ++deltaZ) {
                    BlockPos pos = new BlockPos(X + deltaX, Y + deltaY, Z + deltaZ);
                    if (CropManager.isWeedBlock(w, pos) || (AutoHarvest.instance.configure.flowerISseed.value
                            && CropManager.isFlowerBlock(w, pos))) {
                        assert Minecraft.getInstance().gameMode != null;
                        Minecraft.getInstance().gameMode.startDestroyBlock(pos, Direction.UP);
                        return;
                    }
                }
    }

    /* harvest all mature crops */
    private void harvestTick() {
        // 從getWorld改成getEntityWorld
        Level w = p.level();
        int X = (int) Math.floor(p.getX());
        int Y = (int) Math.floor(p.getY() + 0.2D);// the "leg block", in case in soul sand
        int Z = (int) Math.floor(p.getZ());
        for (int deltaX = -configure.effect_radius.value; deltaX <= configure.effect_radius.value; ++deltaX)
            for (int deltaZ = -configure.effect_radius.value; deltaZ <= configure.effect_radius.value; ++deltaZ) {
                for (int deltaY = -1; deltaY <= 1; ++deltaY) {
                    BlockPos pos = new BlockPos(X + deltaX, Y + deltaY, Z + deltaZ);
                    BlockState state = w.getBlockState(pos);
                    Block block = state.getBlock();
                    if (CropManager.isCropMature(w, pos, state, block)) {
                        if (block == Blocks.SWEET_BERRY_BUSH) {
                            BlockHitResult blockHitResult = new BlockHitResult(
                                    new Vec3(X + deltaX + 0.5, Y + deltaY - 0.5, Z + deltaZ + 0.5), Direction.UP,
                                    pos,
                                    false);
                            assert Minecraft.getInstance().gameMode != null;
                            Minecraft.getInstance().gameMode.useItemOn(p,
                                    InteractionHand.MAIN_HAND, blockHitResult);

                        } else {
                            assert Minecraft.getInstance().gameMode != null;
                            Minecraft.getInstance().gameMode.startDestroyBlock(pos, Direction.UP);
                        }
                        return;
                    }
                }
            }
    }

    private void minusOneInHand() {
        ItemStack st = p.getMainHandItem();
        if (st != null) {
            if (st.getCount() <= 1) {
                p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            } else {
                st.setCount(st.getCount() - 1);
            }
        }
    }

    private ItemStack tryFillItemInHand() {
        ItemStack itemStack = p.getMainHandItem();
        if (itemStack.isEmpty()) {
            if (lastUsedItem != null && !lastUsedItem.isEmpty()) {
                // DefaultedList<ItemStack> inv = p.getInventory().getStack();
                for (int idx = 0; idx < 36; ++idx) {
                    ItemStack s = p.getInventory().getItem(idx);
                    if (s.getItem() == lastUsedItem.getItem() &&
                            s.getDamageValue() == lastUsedItem.getDamageValue() &&
                            s.getComponents().isEmpty()) {
                        AutoHarvest.instance.taskManager.Add_MoveItem(idx, p.getInventory().getSelectedSlot());
                        return s;
                    }
                }
            }
            return null;
        } else {
            return itemStack;
        }
    }

    /**
     * @return -1: does't have rod; 0: no change; change
     **/
    private int tryReplacingFishingRod() {
        ItemStack itemStack = p.getMainHandItem();
        if (CropManager.isRod(itemStack)
                && (!configure.keepFishingRodAlive.value || itemStack.getMaxDamage() - itemStack.getDamageValue() > 1)) {
            return 0;
        } else {
            // DefaultedList<ItemStack> inv = p.getInventory().getStack(idx);
            for (int idx = 0; idx < 36; ++idx) {
                ItemStack s = p.getInventory().getItem(idx);
                if (CropManager.isRod(s)
                        && (!configure.keepFishingRodAlive.value || s.getMaxDamage() - s.getDamageValue() > 1)) {
                    AutoHarvest.instance.taskManager.Add_MoveItem(idx, p.getInventory().getSelectedSlot());
                    return 1;
                }
            }
            return -1;
        }
    }

    private void plantTick() {
        ItemStack handItem = tryFillItemInHand();
        // Toto: 透過PlantBlock檢查
        if (handItem == null)
            return;
        if (!CropManager.isSeed(handItem)) {
            if (CropManager.isCocoa(handItem)) {
                plantCocoaTick(handItem);
            }
            return;
        }

        Level w = p.level();
        int X = (int) Math.floor(p.getX());
        int Y = (int) Math.floor(p.getY() + 0.2D);// the "leg block" , in case in soul sand
        int Z = (int) Math.floor(p.getZ());

        
        for (int deltaX = -configure.effect_radius.value; deltaX <= configure.effect_radius.value; ++deltaX)
            for (int deltaZ = -configure.effect_radius.value; deltaZ <= configure.effect_radius.value; ++deltaZ) {
                BlockPos pos = new BlockPos(X + deltaX, Y, Z + deltaZ);
                if (CropManager.canPaint(w.getBlockState(pos), handItem) == false)
                continue;
                if (CropManager.canPlantOn(handItem.getItem(), w, pos)) {
                    if (w.getBlockState(pos.below()).getBlock() == Blocks.KELP)
                        continue;
                    lastUsedItem = handItem.copy();
                    assert Minecraft.getInstance().gameMode != null;
                    BlockPos downPos = pos.below();
                    BlockHitResult blockHitResult = new BlockHitResult(new Vec3(X + deltaX + 0.5, Y, Z + deltaZ + 0.5),
                            Direction.UP, downPos, false);
                    Minecraft.getInstance().gameMode.useItemOn(Minecraft.getInstance().player,
                            InteractionHand.MAIN_HAND, blockHitResult);
                    minusOneInHand();
                    return;
                }
            }
    }

    private void plantCocoaTick(ItemStack handItem) {
        Level w = p.level();
        int X = (int) Math.floor(p.getX());
        int Y = (int) Math.floor(p.getY() + 0.2D);// the "leg block" , in case in soul sand
        int Z = (int) Math.floor(p.getZ());

        for (int deltaX = -configure.effect_radius.value; deltaX <= configure.effect_radius.value; ++deltaX) {
            for (int deltaZ = -configure.effect_radius.value; deltaZ <= configure.effect_radius.value; ++deltaZ) {
                for (int deltaY = 0; deltaY <= 7; ++deltaY) {
                    BlockPos pos = new BlockPos(X + deltaX, Y + deltaY, Z + deltaZ);
                    if (!canReachBlock(p, pos))
                        continue;
                    BlockState jungleBlock = w.getBlockState(pos);
                    if (CropManager.isJungleLog(jungleBlock)) {
                        BlockPos tmpPos;

                        Direction tmpFace = Direction.EAST;
                        tmpPos = pos.offset(tmpFace.getUnitVec3i());
                        if (w.getBlockState(tmpPos).getBlock() == Blocks.AIR) {
                            lastUsedItem = handItem.copy();
                            BlockHitResult blockHitResult = new BlockHitResult(
                                    new Vec3(X + deltaX + 1, Y + deltaY + 0.5, Z + deltaZ + 0.5), tmpFace, pos, false);
                            assert Minecraft.getInstance().gameMode != null;
                            Minecraft.getInstance().gameMode.useItemOn(
                                    Minecraft.getInstance().player, InteractionHand.MAIN_HAND, blockHitResult);
                            minusOneInHand();
                            return;
                        }

                        tmpFace = Direction.WEST;
                        tmpPos = pos.offset(tmpFace.getUnitVec3i());
                        if (w.getBlockState(tmpPos).getBlock() == Blocks.AIR) {
                            lastUsedItem = handItem.copy();
                            BlockHitResult blockHitResult = new BlockHitResult(
                                    new Vec3(X + deltaX, Y + deltaY + 0.5, Z + deltaZ + 0.5), tmpFace, pos, false);
                            assert Minecraft.getInstance().gameMode != null;
                            Minecraft.getInstance().gameMode.useItemOn(
                                    Minecraft.getInstance().player,
                                    InteractionHand.MAIN_HAND, blockHitResult);
                            minusOneInHand();
                            return;
                        }

                        tmpFace = Direction.SOUTH;
                        tmpPos = pos.offset(tmpFace.getUnitVec3i());
                        if (w.getBlockState(tmpPos).getBlock() == Blocks.AIR) {
                            lastUsedItem = handItem.copy();
                            BlockHitResult blockHitResult = new BlockHitResult(
                                    new Vec3(X + deltaX + 0.5, Y + deltaY + 0.5, Z + deltaZ + 1), tmpFace, pos, false);
                            assert Minecraft.getInstance().gameMode != null;
                            Minecraft.getInstance().gameMode.useItemOn(
                                    Minecraft.getInstance().player,
                                    InteractionHand.MAIN_HAND, blockHitResult);
                            minusOneInHand();
                            return;
                        }

                        tmpFace = Direction.NORTH;
                        tmpPos = pos.offset(tmpFace.getUnitVec3i());
                        if (w.getBlockState(tmpPos).getBlock() == Blocks.AIR) {
                            lastUsedItem = handItem.copy();
                            BlockHitResult blockHitResult = new BlockHitResult(
                                    new Vec3(X + deltaX + 0.5, Y + deltaY + 0.5, Z + deltaZ), tmpFace, pos, false);
                            assert Minecraft.getInstance().gameMode != null;
                            Minecraft.getInstance().gameMode.useItemOn(
                                    Minecraft.getInstance().player,
                                    InteractionHand.MAIN_HAND, blockHitResult);
                            minusOneInHand();
                            return;
                        }
                    }
                }
            }
        }
    }

    private boolean canReachBlock(LocalPlayer playerEntity, BlockPos blockpos) {
        double d0 = playerEntity.getX() - ((double) blockpos.getX() + 0.5D);
        double d1 = playerEntity.getY() - ((double) blockpos.getY() + 0.5D) + 1.5D;
        double d2 = playerEntity.getZ() - ((double) blockpos.getZ() + 0.5D);
        double d3 = d0 * d0 + d1 * d1 + d2 * d2;
        return d3 <= 36D;
    }

    private void feedTick() {
        ItemStack handItem = tryFillItemInHand();
        if (handItem == null)
            return;

        // if (animalList.isEmpty()) return;
        AABB box = new AABB(p.getX() - configure.effect_radius.value, p.getY() - configure.effect_radius.value,
                p.getZ() - configure.effect_radius.value,
                p.getX() + configure.effect_radius.value, p.getY() + configure.effect_radius.value,
                p.getZ() + configure.effect_radius.value);
        Collection<Class<? extends Animal>> needShearAnimalList = CropManager.SHEAR_MAP.get(handItem.getItem());
        for (Class<? extends Animal> type : needShearAnimalList) {
            for (Animal e : p.level().getEntitiesOfClass(
                    type,
                    box,
                    animalEntity -> {
                        if (animalEntity instanceof Sheep) {
                            return !animalEntity.isBaby() && !((Sheep) animalEntity).isSheared();
                        }
                        return false;
                    })) {
                lastUsedItem = handItem.copy();
                assert Minecraft.getInstance().gameMode != null;
                // 1. 先從遊戲本體中，抓出玩家目前滑鼠準心正看著的目標（HitResult）
                var hitResult = Minecraft.getInstance().hitResult;
                // 2. 確保這個 HitResult 真的有射中一個實體（安全檢查，防止遊戲崩潰）
                if (hitResult instanceof net.minecraft.world.phys.EntityHitResult entityHitResult) {
                // 3. 呼叫官方的互動函數，把完整的 4 個參數依序餵進去：
                // (玩家, 動物實體, 精準點擊結果, 主手)
                Minecraft.getInstance().gameMode.interact(
                p, 
                e, 
                entityHitResult, // 補上這個原本漏掉的關鍵參數！
                InteractionHand.MAIN_HAND
                );
                } else{
                    // 4. 萬一防呆機制觸發（例如準心剛好移開了），用最基本的方式盲點
                    // 建立一個假的、以動物腳底為基準的點擊向量
                    net.minecraft.world.phys.EntityHitResult fakeHit = new net.minecraft.world.phys.EntityHitResult(e);
                    Minecraft.getInstance().gameMode.interact(p, e, fakeHit, InteractionHand.MAIN_HAND);
                }
                // Minecraft.getInstance().gameMode.interact(p, e, InteractionHand.MAIN_HAND);
                return;
            }
        }
        /*
         * Special handling for axolotls: the food is a single use item and after it is
         * used, it is replaced with a water
         * bucket. The interaction is resolved on the server - if the client doesn't
         * match, the next animal to be
         * interacted with gets scooped up rather than fed.
         */
        Collection<Class<? extends Animal>> needFeedAnimalList = CropManager.FEED_MAP.get(handItem.getItem());
        for (Class<? extends Animal> type : needFeedAnimalList) {
            for (Animal e : p.level().getEntitiesOfClass(
                    type,
                    box,
                    animalEntity -> animalEntity.getAge() >= 0 && !animalEntity.isInLove())) {
                lastUsedItem = handItem.copy();

                assert Minecraft.getInstance().gameMode != null;
                // 修正錯誤
                // Minecraft.getInstance().gameMode.interact(p, e, InteractionHand.MAIN_HAND);
                // 1. 先從遊戲本體中，抓出玩家目前滑鼠準心正看著的目標（HitResult）
                var hitResult = Minecraft.getInstance().hitResult;
                // 2. 確保這個 HitResult 真的有射中一個實體（安全檢查，防止遊戲崩潰）
                if (hitResult instanceof net.minecraft.world.phys.EntityHitResult entityHitResult) {
                // 3. 呼叫官方的互動函數，把完整的 4 個參數依序餵進去：
                // (玩家, 動物實體, 精準點擊結果, 主手)
                Minecraft.getInstance().gameMode.interact(
                p, 
                e, 
                entityHitResult, // 補上這個原本漏掉的關鍵參數！
                InteractionHand.MAIN_HAND
                );
                } else{
                    // 4. 萬一防呆機制觸發（例如準心剛好移開了），用最基本的方式盲點
                    // 建立一個假的、以動物腳底為基準的點擊向量
                    net.minecraft.world.phys.EntityHitResult fakeHit = new net.minecraft.world.phys.EntityHitResult(e);
                    Minecraft.getInstance().gameMode.interact(p, e, fakeHit, InteractionHand.MAIN_HAND);
                }
            }
        }

    }

    private long getEntityWorldTime() {
        assert Minecraft.getInstance().level != null;
        return Minecraft.getInstance().level.getGameTime();
    }

    private boolean isFishBites(LocalPlayer player) {
        FishingHook fishEntity = player.fishing;
        return fishEntity != null 
                && (fishEntity.xo - fishEntity.getX()) == 0
                && (fishEntity.zo - fishEntity.getZ()) == 0 
                && (fishEntity.yo - fishEntity.getY()) < -0.05d;
    }

    private void fishingTick() {
        switch (tryReplacingFishingRod()) {
            case -1:
                AutoHarvest.msg("notify.turn.off");
                AutoHarvest.instance.Switch = false;
                break;
            case 0:
                /* Reel */
                if (fishBitesAt == 0 && isFishBites(p)) {
                    fishBitesAt = getEntityWorldTime();
                    assert Minecraft.getInstance().gameMode != null;
                    Minecraft.getInstance().gameMode.useItem(
                            p,
                            InteractionHand.MAIN_HAND);
                }

                /* Cast */
                if (fishBitesAt != 0 && fishBitesAt + 20 <= getEntityWorldTime()) {
                    assert Minecraft.getInstance().gameMode != null;
                    Minecraft.getInstance().gameMode.useItem(
                            p,
                            InteractionHand.MAIN_HAND);
                    fishBitesAt = 0;
                }
                break;
            case 1:
        }
    }

    /* clear all grass on land */
    // 催熟模式程式碼
    private void bonemealingTick() {
        ItemStack handItem = p.getMainHandItem();
        if (handItem == null || !CropManager.isBoneMeal(handItem)) {
            return;
        } else {
            handItem = tryFillItemInHand();
        }

        Level w = p.level();
        // 使用 Vec3d 獲取精確位置，避免 floor 造成的偏差
        Vec3 playerPos = new Vec3(p.getX(), p.getY(), p.getZ());
        int X = (int) Math.floor(p.getX());
        int Y = (int) Math.floor(p.getY());
        // the "leg block"
        int Z = (int) Math.floor(p.getZ());

        // 迴圈偵測
        for (int deltaY = 3; deltaY >= -2; --deltaY)
            for (int deltaX = -configure.effect_radius.value; deltaX <= configure.effect_radius.value; ++deltaX)
                for (int deltaZ = -configure.effect_radius.value; deltaZ <= configure.effect_radius.value; ++deltaZ) {
                    BlockPos pos = new BlockPos(X + deltaX, Y + deltaY, Z + deltaZ);
                    // --- 新增：距離檢查 (關鍵！) ---
                    // 如果目標方塊離玩家中心超過 4.5 格，就跳過，避免伺服器拒絕
                    // if (pos.getSquaredDistance(p.getPos()) > 20.25) { // 4.5 * 4.5 = 20.25
                    // continue;
                    double distanceSq = pos.distToCenterSqr(playerPos);
                    if (distanceSq > 20.25) { // 4.5 * 4.5 = 20.25
                    continue;
                    }
                    BlockState blockState = w.getBlockState(pos);
                    Block block = blockState.getBlock();
                    if (block instanceof BonemealableBlock) {
                        if (((BonemealableBlock) block).isValidBonemealTarget(w, pos, blockState)) {
                            BlockHitResult blockHitResult = new BlockHitResult(
                                    // new Vec3d(X + deltaX + 0.5,
                                    // 這裡的 Y 是玩家的腳部座標。
                                    // Y, Z + deltaZ + 0.5), Direction.UP, pos, false

                                    pos.getCenter(), // 這裡修正了原本的 Y 座標錯誤
                                    Direction.UP, 
                                    pos, 
                                    false
                                    );
                            assert handItem != null;
                            lastUsedItem = handItem.copy();

                            // assert MinecraftClient.getInstance().interactionManager != null;
                            // MinecraftClient.getInstance().interactionManager.interactBlock(
                                    // MinecraftClient.getInstance().player,
                                    // Hand.MAIN_HAND, blockHitResult);

                            if (Minecraft.getInstance().gameMode != null) {
                            Minecraft.getInstance().gameMode.useItemOn(
                                    Minecraft.getInstance().player,
                                    InteractionHand.MAIN_HAND, 
                                    blockHitResult
                            );
                            }
                            minusOneInHand();
                            return;
                            // 成功點擊一次後結束，防止單 tick 多次操作
                        }
                    }
                }
    }
}
