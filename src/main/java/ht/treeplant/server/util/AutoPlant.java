package ht.treeplant.server.util;

import ht.treeplant.server.config.ConfigHandler;
import ht.treeplant.server.config.PlantingConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
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
        MinecraftForge.EVENT_BUS.addListener(AutoPlant::watchItemEntities);
        MinecraftForge.EVENT_BUS.addListener(AutoPlant::detectPlayerToss);
        MinecraftForge.EVENT_BUS.addListener(AutoPlant::detectPlayerBlockBreak);

        // TODO: repeat this check in ConfigHandler.onReload
        if (!ConfigHandler.allowPlantingWithRightClick) {
            MinecraftForge.EVENT_BUS.addListener(AutoPlant::preventManualPlanting);
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
        if (!event.getPlayer().isCreative() && event.getItemStack().getItem().getTags().contains(ConfigHandler.itemTagForSaplings)) {
            event.setUseItem(Event.Result.DENY);
        }
    }

    public static void watchItemEntities(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof ItemEntity) {
            ItemStack itemStack = ((ItemEntity) entity).getItem();
            if (!itemStack.isEmpty() && itemStack.getItem().getTags().contains(ConfigHandler.itemTagForSaplings)) {
                PlantingConfig plantingConfig;
                if (entity == lastTossedItem) {
                    plantingConfig = ConfigHandler.COMMON.tossedSaplings;
                } else if (entity.getPosition().equals(lastBrokenPos)) {
                    plantingConfig = ConfigHandler.COMMON.brokenSaplings;
                } else {
                    plantingConfig = ConfigHandler.COMMON.naturalSaplings;
                }

                if (RandomUtil.get(1.0) <= plantingConfig.getChanceOfPlanting()) {
                    watch((ItemEntity) entity, plantingConfig);
                }
            }
        }
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
                    if (top != null && top.tick <= tick) {
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
