package ht.treeplant;

import ht.treeplant.server.event.AutoPlant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class TreePlant {
    public static final String MOD_ID = "treeplant";
    public static final String MOD_NAME = "HT's TreePlant";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
    public static AutoPlant AUTO_PLANTER;
}
