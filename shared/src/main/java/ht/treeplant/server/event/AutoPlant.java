package ht.treeplant.server.event;

import ht.treeplant.ModLoader;
import ht.treeplant.server.config.ConfigHandler;
import ht.treeplant.server.config.PlantingConfig;
import ht.treeplant.server.util.ItemEntityWatcher;
import ht.treeplant.server.util.PlantUtil;
import ht.treeplant.server.util.RandomUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.PriorityQueue;

public abstract class AutoPlant {
    private static final PriorityQueue<ItemEntityWatcher> watchers = new PriorityQueue<>();
    private static long tick = 0;
    private static ItemEntity lastTossedItem;
    private static BlockPos lastBrokenPos;

    public abstract void init();

    protected abstract TickHandler getTickHandler();

    public abstract void watch(ItemEntity itemEntity, PlantingConfig plantingConfig, long tick);

    public void watch(ItemEntityWatcher watcher) {
        getTickHandler().activate();
        watchers.add(watcher);
    }


    public void watchItemEntities(Entity entity) {
        if (entity instanceof ItemEntity) {
            ItemStack itemStack = ((ItemEntity) entity).getItem();
            if (itemStackIsPlantable(itemStack)) {
                PlantingConfig plantingConfig;
                if (entity == lastTossedItem) {
                    plantingConfig = ConfigHandler.COMMON.tossedSaplings;
                } else if (entity.blockPosition().equals(lastBrokenPos)) {
                    plantingConfig = ConfigHandler.COMMON.brokenSaplings;
                } else {
                    plantingConfig = ConfigHandler.COMMON.naturalSaplings;
                }

                if (randomlyWantsToPlant(plantingConfig)) {
                    watch((ItemEntity) entity, plantingConfig, tick);
                }
            }
        }
    }

    public static boolean randomlyWantsToPlant(PlantingConfig plantingConfig) {
        return RandomUtil.get(1.0) <= plantingConfig.getChanceOfPlanting();
    }

    public boolean allowManualPlanting(Player entity, ItemStack stack) {
        return entity.isCreative() || !itemStackIsPlantable(stack);
    }

    public void plantOnDespawn(ItemEntity entity) {
        if (itemStackIsPlantable(entity.getItem()) && randomlyWantsToPlant(ConfigHandler.COMMON.despawningSaplings)) {
            PlantUtil.tryToPlant(entity);
        }
    }

    public void detectPlayerToss(ItemEntity entity) {
        lastTossedItem = entity;
    }

    public void detectPlayerBlockBreak(Player player, BlockPos pos) {
        if (player != null && pos != null) {
            lastBrokenPos = pos;
        }
    }

    public boolean itemStackIsPlantable(ItemStack itemStack) {
        return !itemStack.isEmpty() && ConfigHandler.isSapling(itemStack.getItem());
    }

    public void tick() {
        if (!watchers.isEmpty()) {
            ++tick;

            while (true) {
                ItemEntityWatcher top = watchers.peek();
                if (top != null && top.getTick() <= tick) {
                    watchers.poll();
                    top.activate();
                } else {
                    break;
                }
            }
        } else {
            tick = 0;
            getTickHandler().deactivate();
        }

        lastBrokenPos = null;
    }

    public Block getPlantBlock(ItemEntity itemEntity) {
        Item item = itemEntity.getItem().getItem();
        if (item instanceof BlockItem blockItem) {
            return blockItem.getBlock();
        } else {
            return null;
        }
    }

    public abstract boolean uses(ModLoader loader);
}
