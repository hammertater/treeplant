package ht.treeplant.server.util;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class PlantUtil {

    public static boolean tryToPlant(ItemEntity entity) {
        try {
            InteractionResult result = entity.getItem().useOn(new UseOnContextAccessor(
                    entity.getLevel(),
                    null,
                    InteractionHand.MAIN_HAND,
                    entity.getItem(),
                    new BlockHitResult(entity.position(), Direction.DOWN, entity.blockPosition(), true))
            );
            return result == InteractionResult.SUCCESS;
        } catch (NullPointerException ignored) {
            return false;
        }
    }

    private static class UseOnContextAccessor extends UseOnContext {
        protected UseOnContextAccessor(Level level, @Nullable Player player, InteractionHand hand, ItemStack stack, BlockHitResult hitResult) {
            super(level, player, hand, stack, hitResult);
        }
    }

}
