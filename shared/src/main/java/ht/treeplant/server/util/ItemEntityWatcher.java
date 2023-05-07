package ht.treeplant.server.util;

import ht.treeplant.server.config.ConfigHandler;
import ht.treeplant.server.config.PlantingConfig;
import ht.treeplant.server.event.AutoPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.lang.ref.WeakReference;

public class ItemEntityWatcher implements Comparable<ItemEntityWatcher> {

    private long tick;
    private int totalNumTicks = 0;
    private final long lastPossibleTick;
    private final WeakReference<ItemEntity> ref;
    private final PlantingConfig plantingConfig;
    private final AutoPlant autoPlant;

    public ItemEntityWatcher(AutoPlant autoPlant, ItemEntity itemEntity, long tickZero, PlantingConfig plantingConfig) {
        this.autoPlant = autoPlant;
        this.ref = new WeakReference<>(itemEntity);
        this.tick = tickZero + plantingConfig.getNumTicksBeforePlanting() + (int) RandomUtil.get(plantingConfig.getWiggle());
        this.lastPossibleTick = tick + ConfigHandler.numTicksToRetryPlanting;
        this.plantingConfig = plantingConfig;
    }

    public long getTick() {
        return tick;
    }

    public void activate() {
        ItemEntity itemEntity = ref.get();
        if (itemEntity != null && itemEntity.isAlive() && noNearbySaplings(itemEntity, plantingConfig)) {
            boolean successful = PlantUtil.tryToPlant(itemEntity);
            if (successful) {
                totalNumTicks += tick;
                if (totalNumTicks < lastPossibleTick) {
                    tick += ConfigHandler.numTicksBetweenTries;
                    autoPlant.watch(this);
                }
            }
        }
    }

    @Override
    public int compareTo(ItemEntityWatcher other) {
        return Long.compare(this.tick, other.tick);
    }

    private boolean noNearbyBlocksOfType(Level level, BlockPos pos, Block block, int minDistanceBetweenSaplings) {
        int dis = minDistanceBetweenSaplings - 1;
        for (int x = -dis; x <= dis; ++x) {
            for (int z = -dis; z <= dis; ++z) {
                if (level.getBlockState(pos.offset(x, 0, z)).getBlock() == block) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean noNearbySaplings(ItemEntity itemEntity, PlantingConfig plantingConfig) {
        Block plant = autoPlant.getPlantBlock(itemEntity);
        if (plant == null || plant == Blocks.AIR) {
            return true;
        }

        return noNearbyBlocksOfType(
                itemEntity.getLevel(),
                itemEntity.blockPosition(),
                plant,
                plantingConfig.getMinDistanceBetweenSaplings()
        );
    }


}
