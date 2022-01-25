package ht.treeplant.server.util;

import ht.treeplant.server.config.ConfigHandler;
import ht.treeplant.server.config.PlantingConfig;
import ht.treeplant.server.event.AutoPlant;
import net.minecraft.world.entity.item.ItemEntity;

import java.lang.ref.WeakReference;

public class ItemEntityWatcher implements Comparable<ItemEntityWatcher> {

    private long tick;
    private int totalNumTicks = 0;
    private final long lastPossibleTick;
    private final WeakReference<ItemEntity> ref;
    private final PlantingConfig plantingConfig;

    public ItemEntityWatcher(ItemEntity itemEntity, long tickZero, PlantingConfig plantingConfig) {
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
        if (itemEntity != null && itemEntity.isAlive() && PlantUtil.noNearbySaplings(itemEntity, plantingConfig)) {
            boolean successful = PlantUtil.tryToPlant(itemEntity);
            if (successful) {
                totalNumTicks += tick;
                if (totalNumTicks < lastPossibleTick) {
                    tick += ConfigHandler.numTicksBetweenTries;
                    AutoPlant.watch(this);
                }
            }
        }
    }

    @Override
    public int compareTo(ItemEntityWatcher other) {
        return Long.compare(this.tick, other.tick);
    }

}
