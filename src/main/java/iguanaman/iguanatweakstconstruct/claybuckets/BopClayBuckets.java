package iguanaman.iguanatweakstconstruct.claybuckets;

import net.minecraft.item.Item;

import biomesoplenty.api.content.BOPCBlocks;
import biomesoplenty.api.content.BOPCFluids;

public class BopClayBuckets {

    public static Item clayBucketPoison;
    public static Item clayBucketBlood;

    public static void register() {
        clayBucketPoison = ClayBucketHandler
                .registerClayBucket("clayBucketPoison", "clayBucket.poison", BOPCFluids.poison, BOPCBlocks.poison);
        clayBucketBlood = ClayBucketHandler
                .registerClayBucket("clayBucketBlood", "clayBucket.blood", BOPCFluids.blood, BOPCBlocks.blood);
    }
}
