package iguanaman.iguanatweakstconstruct.claybuckets;

import net.minecraft.item.Item;

import cpw.mods.fml.common.registry.GameRegistry;
import iguanaman.iguanatweakstconstruct.claybuckets.items.BaseClayBucket;
import iguanaman.iguanatweakstconstruct.reference.Reference;
import mods.railcraft.common.fluids.RailcraftFluids;

public class RailcraftClayBuckets {

    public static Item clayBucketCreosote;

    public static void register() {
        String creosoteClayBucketName = "clayBucketCreosote";
        clayBucketCreosote = new BaseClayBucket(
                RailcraftFluids.CREOSOTE.getBlock(),
                Reference.prefix("clayBucket.creosote"),
                Reference.resource(creosoteClayBucketName));
        GameRegistry.registerItem(clayBucketCreosote, creosoteClayBucketName);
        ClayBucketHandler.registerClayBucket(
                clayBucketCreosote,
                RailcraftFluids.CREOSOTE.standardFluid.get(),
                RailcraftFluids.CREOSOTE.getBlock());
    }
}
