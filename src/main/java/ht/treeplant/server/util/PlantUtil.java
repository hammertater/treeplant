package ht.treeplant.server.util;

import ht.treeplant.TreePlant;
import ht.treeplant.server.config.PlantingConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class PlantUtil {

    public static boolean tryToPlant(ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getItem();
        Item item = itemStack.getItem();
        if (item instanceof BlockItem) {
            InteractionResult result = ((BlockItem) item).place(
                    new DirectionalPlaceContext(
                            itemEntity.getLevel(),
                            itemEntity.blockPosition(),
                            Direction.DOWN,
                            itemStack,
                            Direction.UP
                    )
            );
            return result == InteractionResult.SUCCESS;
        } else {
            TreePlant.LOGGER.warn("I don't know how to plant " + item.getRegistryName() + " (" + item.getClass().getName() + " is not an instance of " + BlockItem.class.getName() + ")");
            return false;
        }
    }

    public static boolean noNearbyBlocksOfType(Level level, BlockPos pos, Block block, int minDistanceBetweenSaplings) {
        int dis = minDistanceBetweenSaplings - 1;
        for (int x = -dis; x <= dis; ++x) {
            for (int z = -dis; z <= dis; ++z) {
                if (level.getBlockState(pos.offset(x, 0, z)).getBlock() == block) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean randomlyWantsToPlant(PlantingConfig plantingConfig) {
        return RandomUtil.get(1.0) <= plantingConfig.getChanceOfPlanting();
    }

    public static boolean noNearbySaplings(ItemEntity itemEntity, PlantingConfig plantingConfig) {
        Item item = itemEntity.getItem().getItem();
        if (item instanceof BlockItem) {
            return noNearbyBlocksOfType(
                    itemEntity.getLevel(),
                    itemEntity.blockPosition(),
                    ((BlockItem) item).getBlock(),
                    plantingConfig.getMinDistanceBetweenSaplings()
            );
        } else {
            return false;
        }
    }

}
