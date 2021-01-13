package ht.treeplant.server.util;

import ht.treeplant.server.config.ConfigHandler;
import ht.treeplant.server.config.PlantingConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class AutoPlant {

    private static PriorityQueue<ItemEntityWatcher> watchers = new PriorityQueue<>();
    private static long tick = 0;
    private static ItemEntity lastTossedItem;
    private static BlockPos lastBrokenPos;

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(AutoPlant::onToss);
        MinecraftForge.EVENT_BUS.addListener(AutoPlant::onBreak);
        MinecraftForge.EVENT_BUS.register(AutoPlant.class);
    }

    public static void onToss(ItemTossEvent event) {
        lastTossedItem = event.getEntityItem();
    }

    public static void onBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() != null) {
            lastBrokenPos = event.getPos();
        }
    }

    @SubscribeEvent()
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof ItemEntity && ((ItemEntity) entity).getItem().getItem().getTags().contains(ConfigHandler.itemTagForSaplings)) {
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
