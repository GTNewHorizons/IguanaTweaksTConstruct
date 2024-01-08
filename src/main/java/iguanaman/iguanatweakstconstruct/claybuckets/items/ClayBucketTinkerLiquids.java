package iguanaman.iguanatweakstconstruct.claybuckets.items;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import iguanaman.iguanatweakstconstruct.reference.Reference;
import tconstruct.smeltery.items.FilledBucket;

public class ClayBucketTinkerLiquids extends FilledBucket {

    public ClayBucketTinkerLiquids(Block b) {
        super(b);

        this.setUnlocalizedName(Reference.prefix("clayBucket"));
        this.setContainerItem(null);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        ItemStack result = super.onItemRightClick(itemStack, world, player);

        if (result.getItem() == Items.bucket) {
            itemStack.stackSize--;
            return itemStack;
        }
        return result;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        this.icons = new IIcon[textureNames.length];

        for (int i = 0; i < this.icons.length; ++i) {
            this.icons[i] = iconRegister.registerIcon(Reference.resource("clayBucket_" + textureNames[i]));
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        int arr = MathHelper.clamp_int(stack.getItemDamage(), 0, materialNames.length);
        return getUnlocalizedName() + "." + materialNames[arr];
    }
}
