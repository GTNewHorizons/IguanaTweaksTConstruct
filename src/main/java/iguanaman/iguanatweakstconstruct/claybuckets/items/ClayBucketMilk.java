package iguanaman.iguanatweakstconstruct.claybuckets.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBucketMilk;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import iguanaman.iguanatweakstconstruct.claybuckets.IguanaItems;
import iguanaman.iguanatweakstconstruct.reference.Reference;

public class ClayBucketMilk extends ItemBucketMilk {

    public ClayBucketMilk() {
        this.setContainerItem(IguanaItems.clayBucketFired);

        this.setUnlocalizedName(Reference.prefix("clayBucket.Milk"));
        this.setTextureName(Reference.resource("clayBucketMilk"));
    }

    @Override
    public ItemStack onEaten(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
        ItemStack result = super.onEaten(par1ItemStack, par2World, par3EntityPlayer);
        if (result.getItem() == Items.bucket) return new ItemStack(IguanaItems.clayBucketFired);
        return result;
    }
}
