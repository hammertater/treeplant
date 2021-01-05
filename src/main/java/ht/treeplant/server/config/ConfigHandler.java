package ht.treeplant.server.config;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ConfigHandler {

    public static int numTicksBeforePlanting;
    public static int numTicksToRetryPlanting;
    public static int numTicksBetweenTries;
    public static int minDistanceBetweenSaplings;
    public static double chanceToTryPlanting;
    public static ResourceLocation itemTagForSaplings;
    public static boolean plantTossedSaplings;
    public static boolean replantBrokenSaplings;

    public static void onReload() {
        numTicksBeforePlanting = secondsToTicks(COMMON.numSecondsBeforePlanting.get());
        numTicksToRetryPlanting = Math.max(1, secondsToTicks(COMMON.numSecondsToRetryPlanting.get()));
        numTicksBetweenTries = Math.max(1, secondsToTicks(COMMON.numSecondsBetweenTries.get()));
        itemTagForSaplings = new ResourceLocation(COMMON.itemTagForPlantableItems.get());
        chanceToTryPlanting = COMMON.chanceToTryPlanting.get();
        minDistanceBetweenSaplings = COMMON.minDistanceBetweenSaplings.get();
        plantTossedSaplings = COMMON.plantTossedSaplings.get();
        replantBrokenSaplings = COMMON.replantBrokenSaplings.get();
    }

    public static int secondsToTicks(double seconds) {
        return (int) Math.ceil(seconds * 20.0);
    }

    public static class Common {

        protected final ForgeConfigSpec.DoubleValue numSecondsBeforePlanting;
        protected final ForgeConfigSpec.DoubleValue numSecondsToRetryPlanting;
        protected final ForgeConfigSpec.DoubleValue numSecondsBetweenTries;
        protected final ForgeConfigSpec.DoubleValue chanceToTryPlanting;
        protected final ForgeConfigSpec.IntValue minDistanceBetweenSaplings;
        protected final ForgeConfigSpec.ConfigValue<String> itemTagForPlantableItems;
        protected final ForgeConfigSpec.BooleanValue plantTossedSaplings;
        protected final ForgeConfigSpec.BooleanValue replantBrokenSaplings;

        public Common(ForgeConfigSpec.Builder builder) {
            numSecondsBeforePlanting = builder
                    .comment("The number of seconds to wait before automatically planting saplings")
                    .defineInRange("numSecondsBeforePlanting", 2.0, 0, 100000);
            numSecondsToRetryPlanting = builder
                    .comment("The number of seconds to keep trying to plant a sapling after the first attempt")
                    .defineInRange("numSecondsToRetryPlanting", 120.0, 0, 100000);
            numSecondsBetweenTries = builder
                    .comment("The number of seconds to wait before trying to plant after a failed attempt")
                    .defineInRange("numSecondsBetweenTries", 1.0, 0, 100000);
            itemTagForPlantableItems = builder
                    .comment("Items with this tag will be automatically planted if possible")
                    .define("itemTagForPlantableItems", "treeplant:auto_plantables");
            chanceToTryPlanting = builder
                    .comment("Chance that a dropped sapling will be automatically planted if possible")
                    .defineInRange("chanceToTryPlanting", 1.0, 0.0, 1.0);
            minDistanceBetweenSaplings = builder
                    .comment("The minimum allowed distance between saplings of the same type")
                    .defineInRange("minDistanceBetweenSaplings", 2, 1, 16);
            plantTossedSaplings = builder
                    .comment("Whether to automatically plant saplings tossed by players")
                    .define("plantTossedSaplings", false);
            replantBrokenSaplings = builder
                    .comment("Whether to automatically plant saplings dropped when a player breaks a block")
                    .define("replantBrokenSaplings", false);
        }
    }

    public static final Common COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

}
