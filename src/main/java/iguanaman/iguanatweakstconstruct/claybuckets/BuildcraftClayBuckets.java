package iguanaman.iguanatweakstconstruct.claybuckets;

import net.minecraft.item.Item;

import buildcraft.BuildCraftEnergy;

public class BuildcraftClayBuckets {

    public static Item clayBucketOil;

    public static void register() {
        clayBucketOil = ClayBucketHandler.registerClayBucket(
                "clayBucketOil",
                "clayBucket.oil",
                BuildCraftEnergy.fluidOil,
                BuildCraftEnergy.blockOil);
    }
}
