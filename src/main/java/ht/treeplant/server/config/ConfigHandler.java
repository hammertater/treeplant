package ht.treeplant.server.config;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ConfigHandler {

    public static int numTicksToRetryPlanting;
    public static int numTicksBetweenTries;
    public static ResourceLocation itemTagForSaplings;
    public static boolean allowPlantingWithRightClick;

    public static void onReload() {
        numTicksToRetryPlanting = Math.max(1, secondsToTicks(COMMON.numSecondsToRetryPlanting.get()));
        numTicksBetweenTries = Math.max(1, secondsToTicks(COMMON.numSecondsBetweenTries.get()));
        itemTagForSaplings = new ResourceLocation(COMMON.itemTagForPlantableItems.get());
        allowPlantingWithRightClick = COMMON.allowPlantingWithRightClick.get();

        COMMON.naturalSaplings.refresh();
        COMMON.tossedSaplings.refresh();
        COMMON.brokenSaplings.refresh();
    }

    public static int secondsToTicks(double seconds) {
        return (int) Math.ceil(seconds * 20.0);
    }

    public static class Common {

        protected final ForgeConfigSpec.ConfigValue<String> itemTagForPlantableItems;
        protected final ForgeConfigSpec.DoubleValue numSecondsToRetryPlanting;
        protected final ForgeConfigSpec.DoubleValue numSecondsBetweenTries;
        protected final ForgeConfigSpec.BooleanValue allowPlantingWithRightClick;

        public final PlantingConfig naturalSaplings;
        public final PlantingConfig tossedSaplings;
        public final PlantingConfig brokenSaplings;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.push("What to plant");
            itemTagForPlantableItems = builder
                    .comment("Items with this tag will be automatically planted when possible")
                    .define("itemTagForPlantableItems", "treeplant:auto_plantables");
            builder.pop();

            builder.push("When to plant");
            naturalSaplings = new PlantingConfig(builder,
                    "Saplings that fall naturally (e.g. when leaves decay or when using tree felling mods)",
                    2.0,
                    1.0,
                    2
            );
            tossedSaplings = new PlantingConfig(builder,
                    "Saplings that drop when a player breaks a block",
                    2.0,
                    0.0,
                    1
            );
            brokenSaplings = new PlantingConfig(builder,
                    "Saplings tossed by players",
                    2.0,
                    0.0,
                    1
            );
            builder.pop();

            builder.push("Miscellaneous");
            numSecondsToRetryPlanting = builder
                    .comment("The number of seconds to keep trying to plant a sapling after the first attempt")
                    .defineInRange("numSecondsToRetryPlanting", 120.0, 0, 100000);
            numSecondsBetweenTries = builder
                    .comment("The number of seconds to wait before trying to plant after a failed attempt")
                    .defineInRange("numSecondsBetweenTries", 1.0, 0, 100000);
            allowPlantingWithRightClick = builder
                    .comment("Whether to allow players to plant saplings by right-clicking on dirt")
                    .define("allowRightClickPlanting", true);
            builder.pop();
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
