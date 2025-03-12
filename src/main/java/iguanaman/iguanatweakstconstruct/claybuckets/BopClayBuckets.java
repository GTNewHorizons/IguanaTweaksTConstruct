package iguanaman.iguanatweakstconstruct.claybuckets;

import net.minecraft.item.Item;

import biomesoplenty.api.content.BOPCBlocks;
import biomesoplenty.api.content.BOPCFluids;
import iguanaman.iguanatweakstconstruct.reference.Reference;

public class BopClayBuckets {

    public static Item clayBucketPoison;
    public static Item clayBucketBlood;

    public static void register() {
        String poisonClayBucketName = "clayBucketPoison";
        clayBucketPoison = ClayBucketHandler.registerClayBucket(
                poisonClayBucketName,
                Reference.prefix("clayBucket.poison"),
                Reference.resource(poisonClayBucketName),
                BOPCFluids.poison,
                BOPCBlocks.poison);
        String bloodClayBucketName = "clayBucketBlood";
        clayBucketBlood = ClayBucketHandler.registerClayBucket(
                bloodClayBucketName,
                Reference.prefix("clayBucket.blood"),
                Reference.resource(bloodClayBucketName),
                BOPCFluids.blood,
                BOPCBlocks.blood);
    }
}
