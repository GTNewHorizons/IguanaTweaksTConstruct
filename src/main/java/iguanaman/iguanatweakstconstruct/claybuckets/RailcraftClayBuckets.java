package iguanaman.iguanatweakstconstruct.claybuckets;

import net.minecraft.item.Item;

import iguanaman.iguanatweakstconstruct.reference.Reference;
import mods.railcraft.common.fluids.RailcraftFluids;

public class RailcraftClayBuckets {

    public static Item clayBucketCreosote;

    public static void register() {
        String creosoteClayBucketName = "clayBucketCreosote";
        clayBucketCreosote = ClayBucketHandler.registerClayBucket(
                creosoteClayBucketName,
                Reference.prefix("clayBucket.creosote"),
                Reference.resource(creosoteClayBucketName),
                RailcraftFluids.CREOSOTE.standardFluid.get(),
                RailcraftFluids.CREOSOTE.getBlock());
    }
}
