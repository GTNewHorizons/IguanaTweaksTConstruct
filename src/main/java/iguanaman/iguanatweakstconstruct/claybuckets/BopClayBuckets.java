package iguanaman.iguanatweakstconstruct.claybuckets;

import net.minecraft.item.Item;

import biomesoplenty.api.content.BOPCBlocks;
import biomesoplenty.api.content.BOPCFluids;
import cpw.mods.fml.common.registry.GameRegistry;
import iguanaman.iguanatweakstconstruct.claybuckets.items.ClayBucket;

public class BopClayBuckets {

    public static Item clayBucketPoison;
    public static Item clayBucketBlood;

    public static void register() {
        String poisonClayBucketName = "clayBucketPoison";
        clayBucketPoison = new ClayBucket(BOPCBlocks.poison, "clayBucket.poison", poisonClayBucketName);
        GameRegistry.registerItem(clayBucketPoison, poisonClayBucketName);
        ClayBucketHandler.registerClayBucket(clayBucketPoison, BOPCFluids.poison, BOPCBlocks.poison);

        String bloodClayBucketName = "clayBucketBlood";
        clayBucketBlood = new ClayBucket(BOPCBlocks.blood, "clayBucket.blood", bloodClayBucketName);
        GameRegistry.registerItem(clayBucketBlood, bloodClayBucketName);
        ClayBucketHandler.registerClayBucket(clayBucketBlood, BOPCFluids.blood, BOPCBlocks.blood);
    }
}
