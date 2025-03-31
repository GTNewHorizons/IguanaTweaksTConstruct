package iguanaman.iguanatweakstconstruct.claybuckets.items;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import iguanaman.iguanatweakstconstruct.IguanaTweaksTConstruct;
import iguanaman.iguanatweakstconstruct.mobheads.IguanaMobHeads;
import iguanaman.iguanatweakstconstruct.reference.Reference;

public class ClayBucketHot extends BaseClayBucket {

    public ClayBucketHot(Block contents, String name, String texture) {
        super(contents, name, texture);
        this.setContainerItem(null);
    }

    @Override
    protected ItemStack getEmptyBucket(ItemStack itemStack) {
        // bucket is destroyed if it's a hot fluid
        itemStack.stackSize--;

        // very very rarely, you'll get a broken bucket!
        if (IguanaTweaksTConstruct.pulsar.isPulseLoaded(Reference.PULSE_MOBHEADS) && itemStack.stackSize == 0
                && IguanaTweaksTConstruct.random.nextInt(1000) == 0) {
            return new ItemStack(IguanaMobHeads.wearables, 1, 1);
        }

        return itemStack;
    }

}
