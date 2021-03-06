package ht.treeplant.server.util;

import java.util.Random;

public class RandomUtil {
    private static final Random RANDOM = new Random();

    public static double get(double max) {
        return get((float) max);
    }

    public static float get(float max) {
        return RANDOM.nextFloat() * max;
    }
}
