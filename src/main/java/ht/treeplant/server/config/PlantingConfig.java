package ht.treeplant.server.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Collections;

public class PlantingConfig {

    private final ForgeConfigSpec.DoubleValue numSecondsBeforePlanting;
    private final ForgeConfigSpec.DoubleValue wiggle;
    private final ForgeConfigSpec.IntValue minDistanceBetweenSaplings;
    private final ForgeConfigSpec.DoubleValue chanceOfPlanting;

    private int bakedNumTicksBeforePlanting;
    private int bakedWiggleTicks;
    private int bakedMinDistanceBetweenSaplings;
    private double bakedChanceToTryPlanting;

    protected PlantingConfig(
            ForgeConfigSpec.Builder builder,
            String description,
            double defaultNumSecondsBeforePlanting,
            double defaultWiggle,
            double defaultChanceOfPlanting,
            int defaultMinDistanceBetweenSaplings
    ) {
        builder.push(Collections.singletonList(description));
        numSecondsBeforePlanting = builder
                .comment("The number of seconds to wait before planting saplings")
                .defineInRange("numSecondsBeforePlanting", defaultNumSecondsBeforePlanting, 0.0, 100000.0);
        wiggle = builder
                .comment("A random number of seconds to add to the wait time")
                .defineInRange("wiggle", defaultWiggle, 0.0, 100000.0);
        chanceOfPlanting = builder
                .comment("Chance that a dropped sapling will be planted if possible")
                .defineInRange("chanceOfPlanting", defaultChanceOfPlanting, 0.0, 1.0);
        minDistanceBetweenSaplings = builder
                .comment("The minimum allowed distance between saplings of the same type")
                .defineInRange("minDistanceBetweenSaplings", defaultMinDistanceBetweenSaplings, 1, 16);
        builder.pop();
    }

    public void refresh() {
        bakedNumTicksBeforePlanting = ConfigHandler.secondsToTicks(numSecondsBeforePlanting.get());
        bakedWiggleTicks = ConfigHandler.secondsToTicks(wiggle.get());
        bakedMinDistanceBetweenSaplings = minDistanceBetweenSaplings.get();
        bakedChanceToTryPlanting = chanceOfPlanting.get();
    }

    public int getNumTicksBeforePlanting() {
        return bakedNumTicksBeforePlanting;
    }

    public int getMinDistanceBetweenSaplings() {
        return bakedMinDistanceBetweenSaplings;
    }

    public double getChanceOfPlanting() {
        return bakedChanceToTryPlanting;
    }

    public int getWiggle() {
        return bakedWiggleTicks;
    }
}
