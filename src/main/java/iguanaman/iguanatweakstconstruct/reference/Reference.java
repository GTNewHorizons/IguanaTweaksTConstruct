package iguanaman.iguanatweakstconstruct.reference;

import iguanaman.iguanatweakstconstruct.IguanaTweaksTConstruct;
import java.io.File;

public class Reference {
    public static final String MOD_ID = "IguanaTweaksTConstruct";
    public static final String MOD_NAME = "Iguana Tinker Tweaks";
    public static final String TCON_MOD_ID = "TConstruct";

    public static final String RESOURCE = MOD_ID.toLowerCase();

    public static final String PULSE_LEVELING = "ToolLeveling";
    public static final String PULSE_HARVESTTWEAKS = "HarvestLevelTweaks";
    public static final String PULSE_REPLACING = "ToolPartReplacing";
    public static final String PULSE_MOBHEADS = "MobHeads";
    public static final String PULSE_TWEAKS = "Tweaks";
    public static final String PULSE_ITEMS = "Items";
    public static final String PULSE_RESTRICTIONS = "Restrictions";
    public static final String PULSE_WORLDGEN = "WorldGen";
    public static final String PULSE_OVERRIDE = "Override";

    public static final String PULSE_COMPAT_FMP = "MultipartCompat";

    public static final String PROXY_CLIENT_CLASS = "iguanaman.iguanatweakstconstruct.proxy.ClientProxy";
    public static final String PROXY_SERVER_CLASS = "iguanaman.iguanatweakstconstruct.proxy.ServerProxy";

    public static String resource(String res) {
        return String.format("%s:%s", RESOURCE, res);
    }

    public static String prefix(String name) {
        return String.format("iguana.tcon.%s", name);
    }

    public static File configFile(String fileName) {
        return new File(IguanaTweaksTConstruct.configPath, fileName);
    }
}
