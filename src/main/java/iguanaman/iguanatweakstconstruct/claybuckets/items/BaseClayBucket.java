package iguanaman.iguanatweakstconstruct.claybuckets.items;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import iguanaman.iguanatweakstconstruct.claybuckets.IguanaItems;

public class BaseClayBucket extends ItemBucket {

    public BaseClayBucket(Block contents, String name, String texture) {
        super(contents);
        if (name != null) {
            this.setUnlocalizedName(name);
        }
        if (texture != null) {
            this.setTextureName(texture);
        }
        this.setContainerItem(IguanaItems.clayBucketFired);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        ItemStack result = super.onItemRightClick(itemStack, world, player);
        if (result.getItem() == Items.bucket) {
            return getEmptyBucket(itemStack);
        }
        return result;
    }

    protected ItemStack getEmptyBucket(ItemStack itemStack) {
        return getContainerItem(itemStack);
    }
}
