package ht.treeplant.server.util;

import ht.treeplant.TreePlant;
import ht.treeplant.server.config.ConfigHandler;
import ht.treeplant.server.config.PlantingConfig;
import ht.treeplant.server.event.AutoPlant;
import net.minecraft.block.Block;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DirectionalPlaceContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlantUtil {

    public static boolean tryToPlant(ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getItem();
        Item item = itemStack.getItem();
        if (item instanceof BlockItem) {
            ActionResultType result = ((BlockItem) item).tryPlace(
                    new DirectionalPlaceContext(
                            itemEntity.world,
                            itemEntity.getPosition(),
                            Direction.DOWN,
                            itemStack,
                            Direction.UP
                    )
            );
            return result.isSuccess();
        } else {
            TreePlant.LOGGER.warn("I don't know how to plant " + item.getRegistryName() + " (" + item.getItem().getClass().getName() + " is not an instance of " + BlockItem.class.getName() + ")");
            return false;
        }
    }

    public static boolean noNearbyBlocksOfType(World world, BlockPos pos, Block block, int minDistanceBetweenSaplings) {
        int dis = minDistanceBetweenSaplings - 1;
        for (int x = -dis; x <= dis; ++x) {
            for (int z = -dis; z <= dis; ++z) {
                if (world.getBlockState(pos.add(x, 0, z)).getBlock() == block) {
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
                    itemEntity.getEntityWorld(),
                    itemEntity.getPosition(),
                    ((BlockItem) item).getBlock(),
                    plantingConfig.getMinDistanceBetweenSaplings()
            );
        } else {
            return false;
        }
    }

}
