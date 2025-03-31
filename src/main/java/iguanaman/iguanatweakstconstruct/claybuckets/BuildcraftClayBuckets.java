package iguanaman.iguanatweakstconstruct.claybuckets;

import net.minecraft.item.Item;

import buildcraft.BuildCraftEnergy;
import cpw.mods.fml.common.registry.GameRegistry;
import iguanaman.iguanatweakstconstruct.claybuckets.items.ClayBucket;

public class BuildcraftClayBuckets {

    public static Item clayBucketOil;

    public static void register() {
        String oilClayBucketName = "clayBucketOil";
        clayBucketOil = new ClayBucket(BuildCraftEnergy.blockOil, "clayBucket.oil", oilClayBucketName);
        GameRegistry.registerItem(clayBucketOil, oilClayBucketName);
        ClayBucketHandler.registerClayBucket(clayBucketOil, BuildCraftEnergy.fluidOil, BuildCraftEnergy.blockOil);
    }
}
