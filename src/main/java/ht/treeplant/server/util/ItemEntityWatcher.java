package ht.treeplant.server.util;

import ht.treeplant.TreePlant;
import ht.treeplant.server.config.ConfigHandler;
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

class ItemEntityWatcher {
    private static final int MAX_TICK_ENTROPY = 20;

    long tick;
    private int totalNumTicks = 0;
    private final long lastPossibleTick;
    private WeakReference<ItemEntity> ref;

    public ItemEntityWatcher(ItemEntity itemEntity, long tickZero) {
        ref = new WeakReference<>(itemEntity);
        tick = tickZero + ConfigHandler.numTicksBeforePlanting + (int) (RandomUtil.get(Math.min(MAX_TICK_ENTROPY, ConfigHandler.numTicksBetweenTries)));
        lastPossibleTick = tick + ConfigHandler.numTicksToRetryPlanting;
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
                            AutoPlant.keepWatching(this);
                        }
                    }
                }
            } else {
                TreePlant.LOGGER.warn("I don't know how to plant " + item.getRegistryName() + " (" + item.getItem().getClass().getName() + " is not an instance of " + BlockItem.class.getName() + ")");
            }
        }
    }

    private boolean noNearbySaplings(World world, BlockPos pos, Block block) {
        int dis = ConfigHandler.minDistanceBetweenSaplings - 1;
        for (int x = -dis; x <= dis; ++x) {
            for (int z = -dis; z <= dis; ++z) {
                if (world.getBlockState(pos.add(x, 0, z)).getBlock() == block) {
                    return false;
                }
            }
        }
        return true;
    }
}
