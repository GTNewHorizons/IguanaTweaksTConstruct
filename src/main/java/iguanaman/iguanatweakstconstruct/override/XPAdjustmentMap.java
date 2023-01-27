package iguanaman.iguanatweakstconstruct.override;

import java.util.HashMap;
import java.util.Map;

public class XPAdjustmentMap {
    private static Map<String, Float> map = new HashMap();

    public static float get(String s) {
        if (!map.containsKey(s)) {
            map.put(s, 1.0f);
        }
        return map.get(s);
    }

    public static void put(String s, float f) {
        map.put(s, f);
    }
}
