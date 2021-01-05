package ht.treeplant.server.util;

import ht.treeplant.server.config.ConfigHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.LinkedList;
import java.util.Queue;

public class AutoPlant {

    private static Queue<ItemEntityWatcher> watchers = new LinkedList<>();
    private static long tick = 0;
    private static ItemEntity lastTossedItem;
    private static BlockPos lastBrokenPos;

    public static void init() {
        if (!ConfigHandler.plantTossedSaplings) {
            MinecraftForge.EVENT_BUS.addListener(AutoPlant::onToss);
        }

        if (!ConfigHandler.replantBrokenSaplings) {
            MinecraftForge.EVENT_BUS.addListener(AutoPlant::onBreak);
        }

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
        if (
                entity instanceof ItemEntity
                && ((ItemEntity) entity).getItem().getItem().getTags().contains(ConfigHandler.itemTagForSaplings)
                && entity != lastTossedItem
                && !entity.getPosition().equals(lastBrokenPos)
                && RandomUtil.get(1.0) <= ConfigHandler.chanceToTryPlanting
        ) {
            watch((ItemEntity) entity);
        }
    }

    public static void watch(ItemEntity itemEntity) {
        if (!TickHandler.isRegistered) {
            MinecraftForge.EVENT_BUS.register(TickHandler.class);
            TickHandler.isRegistered = true;
        }
        watchers.add(new ItemEntityWatcher(itemEntity, tick));
    }

    public static void keepWatching(ItemEntityWatcher watcher) {
        watchers.add(watcher);
    }

    private static class TickHandler {
        protected static boolean isRegistered = false;

        @SubscribeEvent
        public static void onTick(TickEvent.ServerTickEvent event) {
            if (watchers.peek() != null) {
                ++tick;

                while (true) {
                    ItemEntityWatcher top = watchers.peek();
                    if (top != null && top.tick <= tick) {
                        top.activate();
                        watchers.poll();
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
