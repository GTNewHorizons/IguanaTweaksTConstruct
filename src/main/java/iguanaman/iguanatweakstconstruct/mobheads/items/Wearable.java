package iguanaman.iguanatweakstconstruct.mobheads.items;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import iguanaman.iguanatweakstconstruct.mobheads.IguanaMobHeads;
import iguanaman.iguanatweakstconstruct.reference.Reference;

public class Wearable extends Item {

    private static final String[] textureTypes = new String[] { "bucketHoley", "clayBucketCracked", "endermanJaw",
            "bathat" };
    private IIcon[] icons;

    public Wearable() {
        super();
        this.setContainerItem(IguanaMobHeads.wearables);
        this.setUnlocalizedName(Reference.prefix("wearable"));

        this.setMaxStackSize(1);
    }

    @Override
    public boolean isValidArmor(ItemStack stack, int armorType, Entity entity) {
        return armorType == 0; // 0 = helmet
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addInformation(ItemStack item, EntityPlayer player, List tooltips, boolean advanced) {
        // specul tooltips
        tooltips.add(
                EnumChatFormatting.DARK_GRAY
                        + StatCollector.translateToLocal("tooltip." + textureTypes[item.getItemDamage()]));
    }

    @Override
    public String getUnlocalizedName(ItemStack par1ItemStack) {
        int i = par1ItemStack.getItemDamage();

        if (i < 0 || i >= textureTypes.length) i = 0;

        return getUnlocalizedName() + "." + textureTypes[i];
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
        for (int j = 0; j < textureTypes.length; ++j) par3List.add(new ItemStack(par1, 1, j));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IconRegister) {
        icons = new IIcon[textureTypes.length];

        for (int i = 0; i < textureTypes.length; ++i)
            icons[i] = par1IconRegister.registerIcon(Reference.resource(textureTypes[i]));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int par1) {
        if (par1 < 0 || par1 >= textureTypes.length) par1 = 0;

        return icons[par1];
    }
}
