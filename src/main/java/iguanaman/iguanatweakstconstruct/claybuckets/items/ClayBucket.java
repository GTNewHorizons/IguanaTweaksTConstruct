package iguanaman.iguanatweakstconstruct.claybuckets.items;

import iguanaman.iguanatweakstconstruct.IguanaTweaksTConstruct;
import iguanaman.iguanatweakstconstruct.claybuckets.IguanaItems;
import iguanaman.iguanatweakstconstruct.mobheads.IguanaMobHeads;
import iguanaman.iguanatweakstconstruct.reference.Reference;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ClayBucket extends ItemBucket {
    private final boolean isHot;

    public ClayBucket(Block contents, String name, String texture) {
        this(contents);

        this.setUnlocalizedName(Reference.prefix(name));
        this.setTextureName(Reference.resource(texture));
    }

    public ClayBucket(Block contents) {
        super(contents);

        if (contents == Blocks.flowing_lava) {
            isHot = true;
            this.setContainerItem(null);
        } else {
            isHot = false;
            this.setContainerItem(IguanaItems.clayBucketFired);
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        ItemStack result = super.onItemRightClick(itemStack, world, player);

        if (result.getItem() == Items.bucket) {
            // bucket is destroyed if it's a hot fluid
            if (isHot) {
                itemStack.stackSize--;

                // very very rarely, you'll get a broken bucket!
                if (IguanaTweaksTConstruct.pulsar.isPulseLoaded(Reference.PULSE_MOBHEADS)
                        && itemStack.stackSize == 0
                        && IguanaTweaksTConstruct.random.nextInt(1000) == 0)
                    return new ItemStack(IguanaMobHeads.wearables, 1, 1);

                return itemStack;
            }

            return new ItemStack(IguanaItems.clayBucketFired);
        }
        // water/lava pickup is handled in the ClayBucketHandler
        // if(result.getItem() == Items.water_bucket) return new ItemStack(IguanaItems.clayBucketWater);
        // if(result.getItem() == Items.lava_bucket) return new ItemStack(IguanaItems.clayBucketLava);
        return result;
    }
}
