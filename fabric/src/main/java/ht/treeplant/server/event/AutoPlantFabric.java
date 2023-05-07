package ht.treeplant.server.event;

import ht.treeplant.ModLoader;
import ht.treeplant.server.config.ConfigHandler;
import ht.treeplant.server.config.PlantingConfig;
import ht.treeplant.server.util.ItemEntityWatcher;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class AutoPlantFabric extends AutoPlant {

    private final TickHandler tickHandler;

    public AutoPlantFabric() {
        tickHandler = new MyTickHandler(this);
    }

    @Override
    public void init() {
        if (
                ConfigHandler.COMMON.brokenSaplings.getChanceOfPlanting() > 0.0
                || ConfigHandler.COMMON.tossedSaplings.getChanceOfPlanting() > 0.0
                || ConfigHandler.COMMON.naturalSaplings.getChanceOfPlanting() > 0.0
        ) {
            ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
                if (entity instanceof ItemEntity itemEntity) {
                    if (itemEntity.getThrowingEntity() instanceof Player) {
                        detectPlayerToss(itemEntity);
                    }

                    watchItemEntities(itemEntity);
                }
            });

            PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> detectPlayerBlockBreak(player, pos));
        }

        if (ConfigHandler.COMMON.despawningSaplings.getChanceOfPlanting() > 0.0) {
            ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
                if (entity instanceof ItemEntity itemEntity) {
                    plantOnDespawn(itemEntity);
                }
            });
        }

        // TODO: repeat this check in ConfigHandler.onReload
        if (!ConfigHandler.allowPlantingWithRightClick) {
            UseItemCallback.EVENT.register((player, world, hand) -> {
                ItemStack stack = player.getItemInHand(hand);
                if (!allowManualPlanting(player, player.getItemInHand(hand))) {
                    return InteractionResultHolder.fail(stack);
                } else {
                    return InteractionResultHolder.pass(ItemStack.EMPTY);
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
        return loader == ModLoader.FABRIC;
    }

    private static class MyTickHandler implements TickHandler, ServerTickEvents.StartTick {

        private final AutoPlant autoPlant;
        protected boolean active = false;

        public MyTickHandler(AutoPlant autoPlant) {
            this.autoPlant = autoPlant;
            ServerTickEvents.START_SERVER_TICK.register(this);
        }

        @Override
        public void onStartTick(MinecraftServer server) {
            if (active) {
                autoPlant.tick();
            }
        }

        public void activate() {
            active = true;
        }

        public void deactivate() {
            active = false;
        }
    }

}
