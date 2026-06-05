package com.flier268.autoharvest;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;
// import net.minecraft.world.level.block.*;
import net.minecraft.core.BlockPos;
// import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.panda.Panda;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

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

    public static final Multimap<Item, Class<? extends Animal>> FEED_MAP;
    public static final Multimap<Item, Class<? extends Animal>> SHEAR_MAP;
    static {
        FEED_MAP = ArrayListMultimap.create();

        //金胡萝卜 - 马
        FEED_MAP.put(Items.GOLDEN_CARROT, Horse.class);

        //小麦 - 牛、羊、哞菇
        FEED_MAP.put(Items.WHEAT, Sheep.class);
        FEED_MAP.put(Items.WHEAT, Cow.class);
        FEED_MAP.put(Items.WHEAT, MushroomCow.class);

        //胡萝卜、马铃薯、甜菜根 - 猪
        FEED_MAP.put(Items.CARROT, Pig.class);
        FEED_MAP.put(Items.POTATO, Pig.class);
        FEED_MAP.put(Items.BEETROOT, Pig.class);

        //南瓜种子、西瓜种子、小麦种子、甜菜种子 - 鸡
        FEED_MAP.put(Items.PUMPKIN_SEEDS, Chicken.class);
        FEED_MAP.put(Items.MELON_SEEDS, Chicken.class);
        FEED_MAP.put(Items.WHEAT_SEEDS, Chicken.class);
        FEED_MAP.put(Items.BEETROOT_SEEDS, Chicken.class);

        //腐肉 - 狼（野生）
        FEED_MAP.put(Items.ROTTEN_FLESH, Wolf.class);

        //蒲公英、胡萝卜、小麦种子 - 兔子
        FEED_MAP.put(Items.DANDELION, Rabbit.class);
        FEED_MAP.put(Items.CARROT, Rabbit.class);
        FEED_MAP.put(Items.WHEAT_SEEDS, Parrot.class);

        // 1.11 - 干草捆 - 羊驼
        FEED_MAP.put(Items.HAY_BLOCK, Llama.class);

        // 1.13 - 海草 - 海龟
        FEED_MAP.put(Items.SEAGRASS, Turtle.class);

        // 1.14
        FEED_MAP.put(Items.KELP, Panda.class);        //海带 - 熊猫
        FEED_MAP.put(Items.SWEET_BERRIES, Fox.class); //甜浆果 - 狐狸
        FEED_MAP.put(Items.COD, Cat.class);           //生鲑鱼 - 猫
        FEED_MAP.put(Items.SALMON, Cat.class);        //生鲑鱼 - 猫

        // 1.15 - 各类花 - 蜜蜂
        FEED_MAP.put(Items.DANDELION, Bee.class);
        FEED_MAP.put(Items.POPPY, Bee.class);
        FEED_MAP.put(Items.BLUE_ORCHID, Bee.class);
        FEED_MAP.put(Items.ALLIUM, Bee.class);
        FEED_MAP.put(Items.AZURE_BLUET, Bee.class);
        FEED_MAP.put(Items.RED_TULIP, Bee.class);
        FEED_MAP.put(Items.ORANGE_TULIP, Bee.class);
        FEED_MAP.put(Items.WHITE_TULIP, Bee.class);
        FEED_MAP.put(Items.PINK_TULIP, Bee.class);
        FEED_MAP.put(Items.OXEYE_DAISY, Bee.class);
        FEED_MAP.put(Items.CORNFLOWER, Bee.class);
        FEED_MAP.put(Items.LILY_OF_THE_VALLEY, Bee.class);
        FEED_MAP.put(Items.WITHER_ROSE, Bee.class);
        FEED_MAP.put(Items.SUNFLOWER, Bee.class);
        FEED_MAP.put(Items.LILAC, Bee.class);
        FEED_MAP.put(Items.ROSE_BUSH, Bee.class);
        FEED_MAP.put(Items.PEONY, Bee.class);

        // 1.16
        FEED_MAP.put(Items.WARPED_FUNGUS, Strider.class); //诡异菌 - 炽足兽
        FEED_MAP.put(Items.CRIMSON_FUNGUS, Hoglin.class); //绯红菌 - 疣猪兽

        // 1.17
        FEED_MAP.put(Items.WHEAT, Goat.class); //小麦 - 山羊
        FEED_MAP.put(Items.GLOW_BERRIES, Fox.class); //发光浆果 - 狐狸
        // disabled due to complexity of interaction
        // FEED_MAP.put(Items.TROPICAL_FISH_BUCKET, AxolotlEntity.class);

        // 1.19
        FEED_MAP.put(Items.SLIME_BALL, Frog.class); // 粘液球 - 青蛙
        FEED_MAP.put(Items.CACTUS, Camel.class); // 仙人掌 - 骆驼
        FEED_MAP.put(Items.TORCHFLOWER_SEEDS, Sniffer.class); // 火把花种子 - 嗅探兽

        SHEAR_MAP = ArrayListMultimap.create();
        SHEAR_MAP.put(Items.SHEARS, Sheep.class);
    }

    public static boolean isWeedBlock(Level w, BlockPos pos) {
        Block b = w.getBlockState(pos).getBlock();
        return WEED_BLOCKS.contains(b);
    }

    public static boolean isFlowerBlock(Level w, BlockPos pos) {
        Block b = w.getBlockState(pos).getBlock();
        return FLOWER_BLOCKS.contains(b);
    }

    public static boolean isCropMature(Level w, BlockPos pos, BlockState stat, Block b) {
        if (b instanceof CropBlock) {
            return ((CropBlock) b).isMaxAge(stat);
        } else if (b == BERRY) {
            return stat.getValue(SweetBerryBushBlock.AGE) == 3;
        } else if (b == NETHER_WART) {
            if (b instanceof NetherWartBlock)
                return stat.getValue(NetherWartBlock.AGE) >= 3;
            return false;
        } else if (b == REED_BLOCK || b == BAMBOO || (b == KELP || b == KELP_PLANT)) {
            Block blockDown = w.getBlockState(pos.below()).getBlock();
            Block blockDown2 = w.getBlockState(pos.below(2)).getBlock();
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
            // 是水並且水是靜止的
            if (s.getBlock() == Blocks.WATER) {
            // 2. 安全地讀取水方塊的 LEVEL 屬性，判斷是否為 0 (水源)
            return s.getValue(BlockStateProperties.LEVEL) == 0;
        }
        return false;
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

    public static boolean canPlantOn(Item m, Level w, BlockPos p) {
        if (!SEED_MAP.containsValue(m))
            return false;
        return SEED_MAP.inverse().get(m).defaultBlockState().canSurvive(w, p);
    }
}
