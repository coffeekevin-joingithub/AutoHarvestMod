package com.flier268.autoharvest;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;
import net.minecraft.block.*;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class CropManager {
    public static final Block REED_BLOCK = Blocks.SUGAR_CANE;
    public static final Block NETHER_WART = Blocks.NETHER_WART;
    public static final Block BERRY = Blocks.SWEET_BERRY_BUSH;
    public static final Block BAMBOO = Blocks.BAMBOO;
    public static final Block KELP = Blocks.KELP;
    public static final Block KELP_PLANT = Blocks.KELP_PLANT;

    public static final Set<Block> WEED_BLOCKS = new HashSet<>() {
        {
            add(Blocks.OAK_SAPLING);
            add(Blocks.SPRUCE_SAPLING);
            add(Blocks.BIRCH_SAPLING);
            add(Blocks.JUNGLE_SAPLING);
            add(Blocks.ACACIA_SAPLING);
            add(Blocks.DARK_OAK_SAPLING);
            add(Blocks.FERN);
            add(Blocks.SHORT_GRASS);
            add(Blocks.DEAD_BUSH);
            add(Blocks.BROWN_MUSHROOM);
            add(Blocks.RED_MUSHROOM);
            add(Blocks.TALL_GRASS);
            add(Blocks.LARGE_FERN);
            add(Blocks.SEAGRASS);
            add(Blocks.TALL_SEAGRASS);
            add(Blocks.KELP);
            add(Blocks.KELP_PLANT);
            // 1.16
            add(Blocks.CRIMSON_ROOTS);
            add(Blocks.WARPED_ROOTS);
        }
    };

    public static final Set<Block> FLOWER_BLOCKS = new HashSet<>() {
        {
            add(Blocks.DANDELION);
            add(Blocks.POPPY);
            add(Blocks.BLUE_ORCHID);
            add(Blocks.ALLIUM);
            add(Blocks.AZURE_BLUET);
            add(Blocks.RED_TULIP);
            add(Blocks.ORANGE_TULIP);
            add(Blocks.WHITE_TULIP);
            add(Blocks.PINK_TULIP);
            add(Blocks.OXEYE_DAISY);
            add(Blocks.CORNFLOWER);
            add(Blocks.LILY_OF_THE_VALLEY);
            add(Blocks.WITHER_ROSE);
            add(Blocks.SUNFLOWER);
            add(Blocks.LILAC);
            add(Blocks.ROSE_BUSH);
            add(Blocks.PEONY);
        }
    };

    public static final BiMap<Block, Item> SEED_MAP = HashBiMap.create(
            new HashMap<>() {
                {
                    //注释内容为对应植株（非种子）最早加入时间，而不是其变为可种植作物的最早时间
                    put(Blocks.WHEAT, Items.WHEAT_SEEDS);                   // Indev 20100206 - 小麦
                    put(Blocks.SUGAR_CANE, Items.SUGAR_CANE);               // Alpha 1.0.11 - 甘蔗
                    put(Blocks.PUMPKIN_STEM, Items.PUMPKIN_SEEDS);          // Alpha 1.2.0 - 南瓜
                    put(Blocks.SHORT_GRASS, Items.SHORT_GRASS);             // Beta 1.6 TB3 - 草（小型）
                    put(Blocks.MELON_STEM, Items.MELON_SEEDS);              // Beta 1.8-pre1 - 西瓜
                    put(Blocks.NETHER_WART, Items.NETHER_WART);             // 1.0.0 - 下界疣
                    put(Blocks.POTATOES, Items.POTATO);                     // 1.4.2 - 马铃薯
                    put(Blocks.CARROTS, Items.CARROT);                      // 1.4.2 - 胡萝卜
                    put(Blocks.BEETROOTS, Items.BEETROOT_SEEDS);            // 1.9 - 甜菜根
                    put(Blocks.CHORUS_PLANT, Items.CHORUS_FRUIT);           // 1.9 - 紫颂植株
                    put(Blocks.KELP, Items.KELP);                           // 1.13 - 海带
                    put(Blocks.BAMBOO, Items.BAMBOO);                       // 1.14 - 竹子
                    put(Blocks.SWEET_BERRY_BUSH, Items.SWEET_BERRIES);      // 1.14 - 甜浆果
                    put(Blocks.CRIMSON_FUNGUS, Items.CRIMSON_FUNGUS);       // 1.16 - 绯红菌
                    put(Blocks.WARPED_FUNGUS, Items.WARPED_FUNGUS);         // 1.16 - 诡异菌
                    put(Blocks.PITCHER_CROP, Items.PITCHER_POD);            // 1.19.4 - 瓶子草
                    put(Blocks.TORCHFLOWER_CROP, Items.TORCHFLOWER_SEEDS);  // 1.19.4 - 火把花
                }
            });

    public static final Multimap<Item, Class<? extends AnimalEntity>> FEED_MAP;
    public static final Multimap<Item, Class<? extends AnimalEntity>> SHEAR_MAP;
    static {
        FEED_MAP = ArrayListMultimap.create();

        //金胡萝卜 - 马
        FEED_MAP.put(Items.GOLDEN_CARROT, HorseEntity.class);

        //小麦 - 牛、羊、哞菇
        FEED_MAP.put(Items.WHEAT, SheepEntity.class);
        FEED_MAP.put(Items.WHEAT, CowEntity.class);
        FEED_MAP.put(Items.WHEAT, MooshroomEntity.class);

        //胡萝卜、马铃薯、甜菜根 - 猪
        FEED_MAP.put(Items.CARROT, PigEntity.class);
        FEED_MAP.put(Items.POTATO, PigEntity.class);
        FEED_MAP.put(Items.BEETROOT, PigEntity.class);

        //南瓜种子、西瓜种子、小麦种子、甜菜种子 - 鸡
        FEED_MAP.put(Items.PUMPKIN_SEEDS, ChickenEntity.class);
        FEED_MAP.put(Items.MELON_SEEDS, ChickenEntity.class);
        FEED_MAP.put(Items.WHEAT_SEEDS, ChickenEntity.class);
        FEED_MAP.put(Items.BEETROOT_SEEDS, ChickenEntity.class);

        //腐肉 - 狼（野生）
        FEED_MAP.put(Items.ROTTEN_FLESH, WolfEntity.class);

        //蒲公英、胡萝卜、小麦种子 - 兔子
        FEED_MAP.put(Items.DANDELION, RabbitEntity.class);
        FEED_MAP.put(Items.CARROT, RabbitEntity.class);
        FEED_MAP.put(Items.WHEAT_SEEDS, ParrotEntity.class);

        // 1.11 - 干草捆 - 羊驼
        FEED_MAP.put(Items.HAY_BLOCK, LlamaEntity.class);

        // 1.13 - 海草 - 海龟
        FEED_MAP.put(Items.SEAGRASS, TurtleEntity.class);

        // 1.14
        FEED_MAP.put(Items.KELP, PandaEntity.class);        //海带 - 熊猫
        FEED_MAP.put(Items.SWEET_BERRIES, FoxEntity.class); //甜浆果 - 狐狸
        FEED_MAP.put(Items.COD, CatEntity.class);           //生鲑鱼 - 猫
        FEED_MAP.put(Items.SALMON, CatEntity.class);        //生鲑鱼 - 猫

        // 1.15 - 各类花 - 蜜蜂
        FEED_MAP.put(Items.DANDELION, BeeEntity.class);
        FEED_MAP.put(Items.POPPY, BeeEntity.class);
        FEED_MAP.put(Items.BLUE_ORCHID, BeeEntity.class);
        FEED_MAP.put(Items.ALLIUM, BeeEntity.class);
        FEED_MAP.put(Items.AZURE_BLUET, BeeEntity.class);
        FEED_MAP.put(Items.RED_TULIP, BeeEntity.class);
        FEED_MAP.put(Items.ORANGE_TULIP, BeeEntity.class);
        FEED_MAP.put(Items.WHITE_TULIP, BeeEntity.class);
        FEED_MAP.put(Items.PINK_TULIP, BeeEntity.class);
        FEED_MAP.put(Items.OXEYE_DAISY, BeeEntity.class);
        FEED_MAP.put(Items.CORNFLOWER, BeeEntity.class);
        FEED_MAP.put(Items.LILY_OF_THE_VALLEY, BeeEntity.class);
        FEED_MAP.put(Items.WITHER_ROSE, BeeEntity.class);
        FEED_MAP.put(Items.SUNFLOWER, BeeEntity.class);
        FEED_MAP.put(Items.LILAC, BeeEntity.class);
        FEED_MAP.put(Items.ROSE_BUSH, BeeEntity.class);
        FEED_MAP.put(Items.PEONY, BeeEntity.class);

        // 1.16
        FEED_MAP.put(Items.WARPED_FUNGUS, StriderEntity.class); //诡异菌 - 炽足兽
        FEED_MAP.put(Items.CRIMSON_FUNGUS, HoglinEntity.class); //绯红菌 - 疣猪兽

        // 1.17
        FEED_MAP.put(Items.WHEAT, GoatEntity.class); //小麦 - 山羊
        FEED_MAP.put(Items.GLOW_BERRIES, FoxEntity.class); //发光浆果 - 狐狸
        // disabled due to complexity of interaction
        // FEED_MAP.put(Items.TROPICAL_FISH_BUCKET, AxolotlEntity.class);

        // 1.19
        FEED_MAP.put(Items.SLIME_BALL, FrogEntity.class); // 粘液球 - 青蛙
        FEED_MAP.put(Items.CACTUS, CamelEntity.class); // 仙人掌 - 骆驼
        FEED_MAP.put(Items.TORCHFLOWER_SEEDS, SnifferEntity.class); // 火把花种子 - 嗅探兽

        SHEAR_MAP = ArrayListMultimap.create();
        SHEAR_MAP.put(Items.SHEARS, SheepEntity.class);
    }

    public static boolean isWeedBlock(World w, BlockPos pos) {
        Block b = w.getBlockState(pos).getBlock();
        return WEED_BLOCKS.contains(b);
    }

    public static boolean isFlowerBlock(World w, BlockPos pos) {
        Block b = w.getBlockState(pos).getBlock();
        return FLOWER_BLOCKS.contains(b);
    }

    public static boolean isCropMature(World w, BlockPos pos, BlockState stat, Block b) {
        if (b instanceof CropBlock) {
            return ((CropBlock) b).isMature(stat);
        } else if (b == BERRY) {
            return stat.get(SweetBerryBushBlock.AGE) == 3;
        } else if (b == NETHER_WART) {
            if (b instanceof NetherWartBlock)
                return stat.get(NetherWartBlock.AGE) >= 3;
            return false;
        } else if (b == REED_BLOCK || b == BAMBOO || (b == KELP || b == KELP_PLANT)) {
            Block blockDown = w.getBlockState(pos.down()).getBlock();
            Block blockDown2 = w.getBlockState(pos.down(2)).getBlock();
            return (blockDown == REED_BLOCK && blockDown2 != REED_BLOCK) ||
                    (blockDown == BAMBOO && blockDown2 != BAMBOO) ||
                    (blockDown == KELP_PLANT && blockDown2 != KELP_PLANT);
        }
        return false;
    }

    public static boolean isBoneMeal(ItemStack stack) {
        return (!stack.isEmpty()
                && stack.getItem() == Items.BONE_MEAL);
    }

    public static boolean isSeed(ItemStack stack) {
        return (!stack.isEmpty()
                && SEED_MAP.containsValue(stack.getItem()));
    }

    public static boolean isCocoa(ItemStack stack) {
        return (!stack.isEmpty()
                && stack.getItem() == Items.COCOA_BEANS);
    }

    public static boolean canPaint(BlockState s, ItemStack stack) {
        if (stack.getItem() == Items.KELP) {
            // is water and the water is stationary
            return s.getBlock() == Blocks.WATER && s.getEntries().values().toArray()[0].equals(0);
        }
        return s.getBlock() == Blocks.AIR;
    }

    public static boolean isJungleLog(BlockState s) {
        return s.getBlock() == Blocks.JUNGLE_LOG;
    }

    public static boolean isRod(ItemStack stack) {
        return (!stack.isEmpty()
                && stack.getItem() == Items.FISHING_ROD);
    }

    public static boolean canPlantOn(Item m, World w, BlockPos p) {
        if (!SEED_MAP.containsValue(m))
            return false;
        return SEED_MAP.inverse().get(m).getDefaultState().canPlaceAt(w, p);
    }
}
