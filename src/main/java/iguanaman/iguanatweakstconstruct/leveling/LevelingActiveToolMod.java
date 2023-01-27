package iguanaman.iguanatweakstconstruct.leveling;

import iguanaman.iguanatweakstconstruct.IguanaTweaksTConstruct;
import iguanaman.iguanatweakstconstruct.leveling.modifiers.ModCritical;
import java.util.Arrays;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.oredict.OreDictionary;
import tconstruct.library.ActiveToolMod;
import tconstruct.library.tools.HarvestTool;
import tconstruct.library.tools.ToolCore;

// This class is responsible for actually getting XP when mining!
public class LevelingActiveToolMod extends ActiveToolMod {
    static List<Material> materialBlacklist = Arrays.asList(
            Material.leaves, Material.vine, Material.circuits, Material.glass, Material.piston, Material.snow);

    @Override
    public boolean beforeBlockBreak(ToolCore tool, ItemStack stack, int x, int y, int z, EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) return false;
        // nope, you don't use an autonomous activator!
        if (entity instanceof FakePlayer) return false;
        // why are you breaking this block with that tool! It's not a harvest tool derp!
        if (!(tool instanceof HarvestTool)) return false;

        Block block = entity.worldObj.getBlock(x, y, z);
        int meta = entity.worldObj.getBlockMetadata(x, y, z);

        if (block == null || materialBlacklist.contains(block.getMaterial())) return false;

        NBTTagCompound tags = stack.getTagCompound().getCompoundTag("InfiTool");
        HarvestTool harvestTool = (HarvestTool) tool;

        boolean harvestable = false, effective = false, strong = false;

        harvestable = harvestTool.canHarvestBlock(block, stack);

        Float strength = harvestTool.calculateStrength(tags, block, meta);
        strong = strength >= 1.0f;
        effective = harvestTool.isEffective(block.getMaterial());

        // look for an oredict entry that suggests that the block is an ore
        // todo: might actually be worth it caching this stuff
        boolean blockIsOre = false;
        ItemStack blockStack = new ItemStack(Item.getItemFromBlock(block), 1, meta);
        for (int id : OreDictionary.getOreIDs(blockStack))
            if (OreDictionary.getOreName(id).startsWith("ore")) {
                blockIsOre = true;
                break;
            }

        // only give xp if the use makes sense
        if (harvestable && effective && strong) {
            int xp = 1;
            // bonus xp for mining ores!
            if (blockIsOre) xp++;
            LevelingLogic.addXP(stack, (EntityPlayer) entity, xp);

            // bonus-chances for lapis when mining blocks with drops!
            if (block.quantityDropped(IguanaTweaksTConstruct.random) > 1)
                RandomBonuses.addModifierExtraWeight(RandomBonuses.Modifier.LAPIS, 1, tags);
            // or redstone!
            else RandomBonuses.addModifierExtraWeight(RandomBonuses.Modifier.REDSTONE, 1, tags);

            // block was next to lava or hot liquid in general?
            boolean itsHotInHere = false;
            itsHotInHere |= entity.worldObj.getBlock(x + 1, y, z).getMaterial() == Material.lava;
            itsHotInHere |= entity.worldObj.getBlock(x - 1, y, z).getMaterial() == Material.lava;
            itsHotInHere |= entity.worldObj.getBlock(x, y + 1, z).getMaterial() == Material.lava;
            itsHotInHere |= entity.worldObj.getBlock(x, y - 1, z).getMaterial() == Material.lava;
            itsHotInHere |= entity.worldObj.getBlock(x, y, z + 1).getMaterial() == Material.lava;
            itsHotInHere |= entity.worldObj.getBlock(x, y, z - 1).getMaterial() == Material.lava;
            // it only took 7 lines to make this pun
            if (itsHotInHere) RandomBonuses.addModifierExtraWeight(RandomBonuses.Modifier.AUTOSMELT, 10, tags);
        }

        return false;
    }

    @Override
    public boolean doesCriticalHit(
            ToolCore tool,
            NBTTagCompound tags,
            NBTTagCompound toolTags,
            ItemStack stack,
            EntityLivingBase player,
            Entity entity) {
        // crit modifier adds additional 10% crit chance
        if (toolTags.hasKey(ModCritical.modCritical.key)) return IguanaTweaksTConstruct.random.nextInt(10) == 0;

        return false;
    }
}
