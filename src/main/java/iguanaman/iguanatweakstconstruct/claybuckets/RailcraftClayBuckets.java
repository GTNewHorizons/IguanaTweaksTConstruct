package iguanaman.iguanatweakstconstruct.claybuckets;

import net.minecraft.item.Item;

import mods.railcraft.common.fluids.RailcraftFluids;

public class RailcraftClayBuckets {

    public static Item clayBucketCreosote;

    public static void register() {
        clayBucketCreosote = ClayBucketHandler.registerClayBucket(
                "clayBucketCreosote",
                "clayBucket.creosote",
                RailcraftFluids.CREOSOTE.standardFluid.get(),
                RailcraftFluids.CREOSOTE.getBlock());
    }
}
