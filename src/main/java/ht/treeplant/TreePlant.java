package ht.treeplant;

import ht.treeplant.server.config.ConfigHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("treeplant")
public class TreePlant {
    public static final String MOD_ID = "treeplant";
    public static final String MOD_NAME = "HT's TreePlant";

    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public TreePlant() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON_SPEC);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        modBus.addListener((ModConfig.Loading e) -> ConfigHandler.onLoad());
        modBus.addListener((ModConfig.Reloading e) -> ConfigHandler.onReload());
    }
}
