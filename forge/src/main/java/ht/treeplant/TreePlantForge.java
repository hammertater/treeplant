package ht.treeplant;

import ht.treeplant.server.config.ConfigHandler;
import ht.treeplant.server.event.AutoPlantForge;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(TreePlantForge.MOD_ID)
public class TreePlantForge extends TreePlant {
    public TreePlantForge() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON_SPEC);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        modBus.addListener((ModConfigEvent.Loading e) -> ConfigHandler.onLoad());
        modBus.addListener((ModConfigEvent.Reloading e) -> ConfigHandler.onReload());
        MinecraftForge.EVENT_BUS.addListener((TagsUpdatedEvent event) -> ConfigHandler.updateTags());

        AUTO_PLANTER = new AutoPlantForge();
    }
}
