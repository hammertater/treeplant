package ht.treeplant.server.util;

import ht.treeplant.TreePlant;
import ht.treeplant.server.config.ConfigHandler;
import ht.treeplant.server.config.PlantingConfig;
import net.minecraft.block.Block;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DirectionalPlaceContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.ref.WeakReference;

class ItemEntityWatcher implements Comparable<ItemEntityWatcher> {
    private static final int MAX_TICK_ENTROPY = 20;

    long tick;
    private int totalNumTicks = 0;
    private final long lastPossibleTick;
    private WeakReference<ItemEntity> ref;
    private final PlantingConfig plantingConfig;

    public ItemEntityWatcher(ItemEntity itemEntity, long tickZero, PlantingConfig plantingConfig) {
        this.ref = new WeakReference<>(itemEntity);
        this.tick = tickZero + plantingConfig.getNumTicksBeforePlanting() + (int) (RandomUtil.get(Math.min(MAX_TICK_ENTROPY, ConfigHandler.numTicksBetweenTries)));
        this.lastPossibleTick = tick + ConfigHandler.numTicksToRetryPlanting;
        this.plantingConfig = plantingConfig;
    }

    public void activate() {
        ItemEntity itemEntity = ref.get();
        if (itemEntity != null && itemEntity.isAlive()) {
            ItemStack itemStack = itemEntity.getItem();
            Item item = itemStack.getItem();
            if (item instanceof BlockItem) {
                if (noNearbySaplings(
                        itemEntity.getEntityWorld(),
                        itemEntity.getPosition(),
                        ((BlockItem) item).getBlock()
                )) {
                    ActionResultType result = ((BlockItem) item).tryPlace(
                            new DirectionalPlaceContext(
                                    itemEntity.world,
                                    itemEntity.getPosition(),
                                    Direction.DOWN,
                                    itemStack,
                                    Direction.UP
                            )
                    );

                    if (!result.isSuccess()) {
                        totalNumTicks += tick;
                        if (totalNumTicks < lastPossibleTick) {
                            tick += ConfigHandler.numTicksBetweenTries;
                            AutoPlant.watch(this);
                        }
                    }
                }
            } else {
                TreePlant.LOGGER.warn("I don't know how to plant " + item.getRegistryName() + " (" + item.getItem().getClass().getName() + " is not an instance of " + BlockItem.class.getName() + ")");
            }
        }
    }

    private boolean noNearbySaplings(World world, BlockPos pos, Block block) {
        int dis = plantingConfig.getMinDistanceBetweenSaplings() - 1;
        for (int x = -dis; x <= dis; ++x) {
            for (int z = -dis; z <= dis; ++z) {
                if (world.getBlockState(pos.add(x, 0, z)).getBlock() == block) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int compareTo(ItemEntityWatcher other) {
        return Long.compare(this.tick, other.tick);
    }
}
