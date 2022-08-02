package ht.treeplant.server.config;

import ht.treeplant.server.event.AutoPlant;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ConfigHandler {

    public static int numTicksToRetryPlanting;
    public static int numTicksBetweenTries;
    public static Set<Item> saplings = Collections.emptySet();
    public static boolean allowPlantingWithRightClick;

    public static void onLoad() {
        onReload();
        AutoPlant.init();
    }

    public static void onReload() {
        numTicksToRetryPlanting = Math.max(1, secondsToTicks(COMMON.numSecondsToRetryPlanting.get()));
        numTicksBetweenTries = Math.max(1, secondsToTicks(COMMON.numSecondsBetweenTries.get()));
        allowPlantingWithRightClick = COMMON.allowPlantingWithRightClick.get();

        COMMON.naturalSaplings.refresh();
        COMMON.tossedSaplings.refresh();
        COMMON.brokenSaplings.refresh();
        COMMON.despawningSaplings.refresh();
    }

    public static int secondsToTicks(double seconds) {
        return (int) Math.ceil(seconds * 20.0);
    }

    public static boolean isSapling(Item item) {
        return saplings.contains(item);
    }

    public static void UpdateTags(TagsUpdatedEvent event) {
        ITagManager<Item> itemTags = ForgeRegistries.ITEMS.tags();
        if (itemTags != null) {
            saplings = COMMON.itemTagsForPlantableItems.get().stream()
                    .map(ResourceLocation::tryParse)
                    .filter(Objects::nonNull)
                    .flatMap(tagId -> itemTags.getTag(ItemTags.create(tagId)).stream())
                    .collect(Collectors.toSet());
        }
    }

    public static class Common {

        protected final ForgeConfigSpec.ConfigValue<List<? extends String>> itemTagsForPlantableItems;
        protected final ForgeConfigSpec.DoubleValue numSecondsToRetryPlanting;
        protected final ForgeConfigSpec.DoubleValue numSecondsBetweenTries;
        protected final ForgeConfigSpec.BooleanValue allowPlantingWithRightClick;

        public final PlantingConfig naturalSaplings;
        public final PlantingConfig tossedSaplings;
        public final PlantingConfig brokenSaplings;
        public final PlantingConfig despawningSaplings;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.push("What to plant");
            Predicate<String> itemIdValidator;
            itemTagsForPlantableItems = builder
                    .comment("Items with this tag will be automatically planted when possible")
                    .defineList("itemTagsForPlantableItems", Arrays.asList("treeplant:auto_plantables", "dynamictrees:seeds"),always -> true);
            builder.pop();

            builder.comment("Set chanceOfPlanting = 0.0 to disable").push("When to plant");
            naturalSaplings = new PlantingConfig(builder,
                    "Saplings that fall naturally (e.g. when leaves decay or when using tree felling mods)",
                    1.0,
                    3.0,
                    1.0,
                    2
            );
            brokenSaplings = new PlantingConfig(builder,
                    "Saplings that drop when a player breaks a block",
                    10.0,
                    3.0,
                    1.0,
                    1
            );
            tossedSaplings = new PlantingConfig(builder,
                    "Saplings tossed by players",
                    5.0,
                    3.0,
                    0.0,
                    1
            );
            despawningSaplings = new PlantingConfig(builder,
                    "Saplings that are about to despawn, no matter where they came from",
                    null,
                    null,
                    1.0,
                    2);
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
