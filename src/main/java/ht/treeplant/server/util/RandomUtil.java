package ht.treeplant.server.util;

import net.minecraft.loot.RandomValueRange;

import java.util.Random;

public class RandomUtil {
    private static final Random RANDOM = new Random();
    private static final RandomValueRange RANDOM_VALUE_RANGE = new RandomValueRange(0, 1);

    static double get(double max) {
        return get((float) max);
    }

    static float get(float max) {
        return RANDOM_VALUE_RANGE.generateFloat(RANDOM) * max;
    }
}
