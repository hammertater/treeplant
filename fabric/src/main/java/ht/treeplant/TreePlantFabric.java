package ht.treeplant;

import ht.treeplant.server.config.ConfigHandler;
import ht.treeplant.server.event.AutoPlantFabric;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.api.fml.event.config.ModConfigEvents;
import net.minecraftforge.fml.config.ModConfig;

public class TreePlantFabric extends TreePlant implements ModInitializer {
    @Override
    public void onInitialize() {
        AUTO_PLANTER = new AutoPlantFabric();

        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> ConfigHandler.updateTags());

        ModConfigEvents.loading(MOD_ID).register(TreePlantFabric::onLoad);
        ModConfigEvents.reloading(MOD_ID).register(TreePlantFabric::onReload);
        ModLoadingContext.registerConfig(MOD_ID, ModConfig.Type.COMMON, ConfigHandler.COMMON_SPEC);


    }

    private static void onLoad(ModConfig config) {
        if (config.getModId().equals(MOD_ID)) {
            ConfigHandler.onLoad();
        }
    }

    private static void onReload(ModConfig config) {
        if (config.getModId().equals(MOD_ID)) {
            ConfigHandler.onReload();
        }
    }
}
