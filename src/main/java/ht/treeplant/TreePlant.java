package ht.treeplant;

import ht.treeplant.server.config.ConfigHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(TreePlant.MOD_ID)
public class TreePlant {
    public static final String MOD_ID = "treeplant";
    public static final String MOD_NAME = "HT's TreePlant";

    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public TreePlant() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON_SPEC);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        modBus.addListener((ModConfigEvent.Loading e) -> ConfigHandler.onLoad());
        modBus.addListener((ModConfigEvent.Reloading e) -> ConfigHandler.onReload());
        MinecraftForge.EVENT_BUS.addListener(ConfigHandler::UpdateTags);
    }
}
