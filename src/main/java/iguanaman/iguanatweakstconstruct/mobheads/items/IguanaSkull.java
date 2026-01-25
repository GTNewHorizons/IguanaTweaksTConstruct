package iguanaman.iguanatweakstconstruct.mobheads.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.BlockSkull;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import iguanaman.iguanatweakstconstruct.mobheads.IguanaMobHeads;
import iguanaman.iguanatweakstconstruct.reference.Reference;

public class IguanaSkull extends ItemSkull {

    public static final int META_ENDERMAN = 0;
    public static final int META_PIGZOMBIE = 1;
    public static final int META_BLAZE = 2;
    public static final int META_BLIZZ = 3;

    // an entry for a head
    public static class HeadEntry {

        public String name;
        public String iconString;
        public IIcon icon;

        public HeadEntry(String name, String iconString) {
            this.name = name;
            this.iconString = iconString;
        }
    }

    private static final Map<Integer, HeadEntry> headEntries = new HashMap<Integer, HeadEntry>();
    // add vanilla heads
    static {
        addHead(0, "enderman", "skull_enderman");
        addHead(1, "pigman", "skull_pigman");
        addHead(2, "blaze", "skull_blaze");
    }

    public static void addHead(int meta, String name, String icon) {
        headEntries.put(meta, new HeadEntry(name, icon));
    }

    public static boolean isHeadRegistered(int meta) {
        return headEntries.containsKey(meta);
    }

    @SideOnly(Side.CLIENT)
    private IIcon field_94586_c[];

    public IguanaSkull() {
        super();
        this.setUnlocalizedName(Reference.prefix("skull"));
        this.setContainerItem(Items.skull);
    }

    /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     */
    @Override
    public String getUnlocalizedName(ItemStack par1ItemStack) {
        Integer i = par1ItemStack.getItemDamage();
        if (!headEntries.containsKey(i)) i = 0;

        return getUnlocalizedName() + "." + headEntries.get(i).name;
    }

    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
        for (Integer meta : headEntries.keySet()) par3List.add(new ItemStack(par1, 1, meta));
    }

    /**
     * Gets an icon index based on an items's damage value
     */
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta) {
        if (!headEntries.containsKey(meta)) meta = 0;

        return headEntries.get(meta).icon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        for (HeadEntry entry : headEntries.values())
            entry.icon = iconRegister.registerIcon(Reference.resource(entry.iconString));
    }

    @Override
    public boolean isValidArmor(ItemStack stack, int armorType, Entity entity) {
        return armorType == 0; // 0 = helmet
    }

    // copy'n'paste of the ItemSkull method, but places our own skull instead of the vanilla one
    public boolean onItemUse(ItemStack p_77648_1_, EntityPlayer p_77648_2_, World p_77648_3_, int p_77648_4_,
            int p_77648_5_, int p_77648_6_, int p_77648_7_, float p_77648_8_, float p_77648_9_, float p_77648_10_) {
        if (p_77648_7_ == 0) {
            return false;
        } else if (!p_77648_3_.getBlock(p_77648_4_, p_77648_5_, p_77648_6_).getMaterial().isSolid()) {
            return false;
        } else {
            if (p_77648_7_ == 1) {
                ++p_77648_5_;
            }

            if (p_77648_7_ == 2) {
                --p_77648_6_;
            }

            if (p_77648_7_ == 3) {
                ++p_77648_6_;
            }

            if (p_77648_7_ == 4) {
                --p_77648_4_;
            }

            if (p_77648_7_ == 5) {
                ++p_77648_4_;
            }

            if (!p_77648_3_.isRemote) {
                p_77648_3_.setBlock(p_77648_4_, p_77648_5_, p_77648_6_, IguanaMobHeads.skullBlock, p_77648_7_, 2);
                int i1 = 0;

                if (p_77648_7_ == 1) {
                    i1 = MathHelper.floor_double((double) (p_77648_2_.rotationYaw * 16.0F / 360.0F) + 0.5D) & 15;
                }

                TileEntity tileentity = p_77648_3_.getTileEntity(p_77648_4_, p_77648_5_, p_77648_6_);

                if (tileentity != null && tileentity instanceof TileEntitySkull) {

                    ((TileEntitySkull) tileentity).func_152107_a(p_77648_1_.getItemDamage());

                    ((TileEntitySkull) tileentity).func_145903_a(i1);
                    ((BlockSkull) IguanaMobHeads.skullBlock).func_149965_a(
                            p_77648_3_,
                            p_77648_4_,
                            p_77648_5_,
                            p_77648_6_,
                            (TileEntitySkull) tileentity);
                }

                --p_77648_1_.stackSize;
            }

            return true;
        }
    }
}
