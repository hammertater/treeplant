package ht.treeplant.server.event;

import ht.treeplant.server.config.ConfigHandler;
import ht.treeplant.server.config.PlantingConfig;
import ht.treeplant.server.util.ItemEntityWatcher;
import ht.treeplant.server.util.PlantUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.PriorityQueue;

public class AutoPlant {

    private static PriorityQueue<ItemEntityWatcher> watchers = new PriorityQueue<>();
    private static long tick = 0;
    private static ItemEntity lastTossedItem;
    private static BlockPos lastBrokenPos;

    public static void init() {
        if (
                ConfigHandler.COMMON.brokenSaplings.getChanceOfPlanting() > 0.0
                || ConfigHandler.COMMON.tossedSaplings.getChanceOfPlanting() > 0.0
                || ConfigHandler.COMMON.naturalSaplings.getChanceOfPlanting() > 0.0
        ) {
            MinecraftForge.EVENT_BUS.addListener(AutoPlant::watchItemEntities);
            MinecraftForge.EVENT_BUS.addListener(AutoPlant::detectPlayerToss);
            MinecraftForge.EVENT_BUS.addListener(AutoPlant::detectPlayerBlockBreak);
        }

        if (ConfigHandler.COMMON.despawningSaplings.getChanceOfPlanting() > 0.0) {
            MinecraftForge.EVENT_BUS.addListener(AutoPlant::plantOnDespawn);
        }

        // TODO: repeat this check in ConfigHandler.onReload
        if (!ConfigHandler.allowPlantingWithRightClick) {
            MinecraftForge.EVENT_BUS.addListener(AutoPlant::preventManualPlanting);
        }
    }

    private static void plantOnDespawn(ItemExpireEvent event) {
        ItemEntity entity = event.getEntityItem();
        if (itemStackIsPlantable(entity.getItem()) && PlantUtil.randomlyWantsToPlant(ConfigHandler.COMMON.despawningSaplings)) {
            PlantUtil.tryToPlant(entity);
        }
    }

    public static void detectPlayerToss(ItemTossEvent event) {
        lastTossedItem = event.getEntityItem();
    }

    public static void detectPlayerBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() != null) {
            lastBrokenPos = event.getPos();
        }
    }

    public static void preventManualPlanting(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getPlayer().isCreative() && itemStackIsPlantable(event.getItemStack())) {
            event.setUseItem(Event.Result.DENY);
        }
    }

    public static void watchItemEntities(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof ItemEntity) {
            ItemStack itemStack = ((ItemEntity) entity).getItem();
            if (itemStackIsPlantable(itemStack)) {
                PlantingConfig plantingConfig;
                if (entity == lastTossedItem) {
                    plantingConfig = ConfigHandler.COMMON.tossedSaplings;
                } else if (entity.blockPosition().equals(lastBrokenPos)) {
                    plantingConfig = ConfigHandler.COMMON.brokenSaplings;
                } else {
                    plantingConfig = ConfigHandler.COMMON.naturalSaplings;
                }

                if (PlantUtil.randomlyWantsToPlant(plantingConfig)) {
                    watch((ItemEntity) entity, plantingConfig);
                }
            }
        }
    }

    public static boolean itemStackIsPlantable(ItemStack itemStack) {
        return !itemStack.isEmpty() && itemStack.getItem().getTags().contains(ConfigHandler.itemTagForSaplings);
    }

    public static void watch(ItemEntity itemEntity, PlantingConfig plantingConfig) {
        watch(new ItemEntityWatcher(itemEntity, tick, plantingConfig));
    }

    public static void watch(ItemEntityWatcher watcher) {
        if (!TickHandler.isRegistered) {
            MinecraftForge.EVENT_BUS.register(TickHandler.class);
            TickHandler.isRegistered = true;
        }
        watchers.add(watcher);
    }

    private static class TickHandler {
        protected static boolean isRegistered = false;

        @SubscribeEvent
        public static void onTick(TickEvent.ServerTickEvent event) {
            if (!watchers.isEmpty()) {
                ++tick;

                while (true) {
                    ItemEntityWatcher top = watchers.peek();
                    if (top != null && top.getTick() <= tick) {
                        watchers.poll();
                        top.activate();
                    } else {
                        break;
                    }
                }
            } else {
                tick = 0;
                MinecraftForge.EVENT_BUS.unregister(TickHandler.class);
                TickHandler.isRegistered = false;
            }

            lastBrokenPos = null;
        }
    }
}
