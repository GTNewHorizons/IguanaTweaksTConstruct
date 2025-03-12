package iguanaman.iguanatweakstconstruct.claybuckets;

import net.minecraft.item.Item;

import buildcraft.BuildCraftEnergy;
import iguanaman.iguanatweakstconstruct.reference.Reference;

public class BuildcraftClayBuckets {

    public static Item clayBucketOil;

    public static void register() {
        String oilClayBucketName = "clayBucketOil";
        clayBucketOil = ClayBucketHandler.registerClayBucket(
                oilClayBucketName,
                Reference.prefix("clayBucket.oil"),
                Reference.resource(oilClayBucketName),
                BuildCraftEnergy.fluidOil,
                BuildCraftEnergy.blockOil);
    }
}
