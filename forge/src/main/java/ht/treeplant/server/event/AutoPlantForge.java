package ht.treeplant.server.event;

import ht.treeplant.ModLoader;
import ht.treeplant.server.config.ConfigHandler;
import ht.treeplant.server.config.PlantingConfig;
import ht.treeplant.server.util.ItemEntityWatcher;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AutoPlantForge extends AutoPlant {

    private final TickHandler tickHandler;

    public AutoPlantForge() {
        tickHandler = new MyTickHandler(this);
    }

    @Override
    public void init() {
        if (
                ConfigHandler.COMMON.brokenSaplings.getChanceOfPlanting() > 0.0
                || ConfigHandler.COMMON.tossedSaplings.getChanceOfPlanting() > 0.0
                || ConfigHandler.COMMON.naturalSaplings.getChanceOfPlanting() > 0.0
        ) {
            MinecraftForge.EVENT_BUS.addListener((EntityJoinLevelEvent event) -> watchItemEntities(event.getEntity()));
            MinecraftForge.EVENT_BUS.addListener((ItemTossEvent event) -> detectPlayerToss(event.getEntity()));
            MinecraftForge.EVENT_BUS.addListener((BlockEvent.BreakEvent event) -> detectPlayerBlockBreak(event.getPlayer(), event.getPos()));
        }

        if (ConfigHandler.COMMON.despawningSaplings.getChanceOfPlanting() > 0.0) {
            MinecraftForge.EVENT_BUS.addListener((ItemExpireEvent event) -> plantOnDespawn(event.getEntity()));
        }

        // TODO: repeat this check in ConfigHandler.onReload
        if (!ConfigHandler.allowPlantingWithRightClick) {
            MinecraftForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickBlock event) -> {
                if (!allowManualPlanting(event.getEntity(), event.getItemStack())) {
                    event.setUseItem(Event.Result.DENY);
                }
            });
        }
    }

    @Override
    protected TickHandler getTickHandler() {
        return tickHandler;
    }

    @Override
    public void watch(ItemEntity itemEntity, PlantingConfig plantingConfig, long tick) {
        watch(new ItemEntityWatcher(this, itemEntity, tick, plantingConfig));
    }

    @Override
    public boolean uses(ModLoader loader) {
        return loader == ModLoader.FORGE;
    }

    private static class MyTickHandler implements TickHandler {

        private final AutoPlant autoPlant;
        protected boolean isRegistered = false;

        public MyTickHandler(AutoPlant autoPlant) {
            this.autoPlant = autoPlant;
        }

        @SubscribeEvent
        public void onTick(TickEvent.ServerTickEvent event) {
            autoPlant.tick();
        }

        public void activate() {
            if (!isRegistered) {
                MinecraftForge.EVENT_BUS.register(this);
                isRegistered = true;
            }
        }

        public void deactivate() {
            MinecraftForge.EVENT_BUS.unregister(this);
            isRegistered = false;
        }
    }

}
