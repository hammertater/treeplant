package ht.treeplant.server.util;

import ht.treeplant.server.config.PlantingConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayerFactory;

public class PlantUtil {

    public static boolean tryToPlant(ItemEntity entity) {
        if (entity.getLevel() instanceof ServerLevel level) {
            InteractionResult result = entity.getItem().useOn(new UseOnContext(
                    level,
                    FakePlayerFactory.getMinecraft(level),
                    InteractionHand.MAIN_HAND,
                    entity.getItem(),
                    new BlockHitResult(entity.position(), Direction.DOWN, entity.blockPosition(), true))
            );
            return result == InteractionResult.SUCCESS;
        }

        return false;
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
        Block plant;
        if (item instanceof IPlantable plantItem) {
            plant = plantItem.getPlant(itemEntity.getLevel(), itemEntity.blockPosition()).getBlock();
        } else if (item instanceof BlockItem blockItem) {
            plant = blockItem.getBlock();
        } else {
            return true;
        }

        if (plant != Blocks.AIR) {
            return noNearbyBlocksOfType(
                    itemEntity.getLevel(),
                    itemEntity.blockPosition(),
                    plant,
                    plantingConfig.getMinDistanceBetweenSaplings()
            );
        }

        return true;
    }

}
