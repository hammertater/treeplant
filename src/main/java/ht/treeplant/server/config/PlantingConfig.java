package ht.treeplant.server.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Collections;
import java.util.Optional;

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
            Double defaultNumSecondsBeforePlanting,
            Double defaultWiggle,
            Double defaultChanceOfPlanting,
            Integer defaultMinDistanceBetweenSaplings
    ) {
        builder.push(Collections.singletonList(description));

        numSecondsBeforePlanting = (defaultNumSecondsBeforePlanting != null) ? builder
                .comment("The number of seconds to wait before planting saplings")
                .defineInRange("numSecondsBeforePlanting", defaultNumSecondsBeforePlanting, 0.0, 100000.0) : null;

        wiggle = (defaultWiggle != null) ? builder
                .comment("A random number of seconds to add to the wait time")
                .defineInRange("wiggle", defaultWiggle, 0.0, 100000.0) : null;

        chanceOfPlanting = (defaultChanceOfPlanting != null) ? builder
                .comment("Chance that a dropped sapling will be planted if possible")
                .defineInRange("chanceOfPlanting", defaultChanceOfPlanting, 0.0, 1.0) : null;

        minDistanceBetweenSaplings = (defaultMinDistanceBetweenSaplings != null) ? builder
                    .comment("The minimum allowed distance between saplings of the same type")
                    .defineInRange("minDistanceBetweenSaplings", defaultMinDistanceBetweenSaplings, 1, 16) : null;

        builder.pop();
    }

    public void refresh() {
        bakedNumTicksBeforePlanting = ConfigHandler.secondsToTicks(getConfigValue(numSecondsBeforePlanting).orElse(0.0));
        bakedWiggleTicks = ConfigHandler.secondsToTicks(getConfigValue(wiggle).orElse(0.0));
        bakedMinDistanceBetweenSaplings = getConfigValue(minDistanceBetweenSaplings).orElse(0);
        bakedChanceToTryPlanting = getConfigValue(chanceOfPlanting).orElse(0.0);
    }

    private <T extends Number> Optional<T> getConfigValue(ForgeConfigSpec.ConfigValue<T> numSecondsBeforePlanting) {
        return (numSecondsBeforePlanting != null)
                ? Optional.of(numSecondsBeforePlanting.get())
                : Optional.empty();
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
