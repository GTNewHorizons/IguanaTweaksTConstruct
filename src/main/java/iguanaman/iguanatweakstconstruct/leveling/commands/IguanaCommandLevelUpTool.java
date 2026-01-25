package iguanaman.iguanatweakstconstruct.leveling.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import iguanaman.iguanatweakstconstruct.leveling.LevelingLogic;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.tools.ToolCore;

public class IguanaCommandLevelUpTool extends CommandBase {

    @Override
    public String getCommandName() {
        return "leveluptool";
    }

    /**
     * Return the required permission level for this command.
     */
    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void processCommand(ICommandSender icommandsender, String[] astring) {
        EntityPlayerMP entityplayermp = astring.length >= 1 ? getPlayer(icommandsender, astring[0])
                : getCommandSenderAsPlayer(icommandsender);
        ItemStack equipped = entityplayermp.getCurrentEquippedItem();
        if (equipped != null && equipped.getItem() instanceof ToolCore) {
            NBTTagCompound tags = equipped.getTagCompound().getCompoundTag("InfiTool");
            if (tags.hasKey("ToolLevel")) {
                int level = tags.getInteger("ToolLevel");
                int hLevel = tags.hasKey("HarvestLevel") ? hLevel = tags.getInteger("HarvestLevel") : -1;

                if (level >= 1 && level <= 5 || hLevel >= TConstructRegistry.getMaterial("Copper").harvestLevel()
                        || hLevel < TConstructRegistry.getMaterial("Manyullyn").harvestLevel()) {
                    Long toolXP = tags.hasKey("ToolEXP") ? tags.getLong("ToolEXP") : -1;
                    Long headXP = tags.hasKey("HeadEXP") ? tags.getLong("HeadEXP") : -1;
                    long requiredToolXP = LevelingLogic.getRequiredXp(equipped, tags) - toolXP;
                    long requiredHeadXP = tags.hasKey("HeadEXP")
                            && hLevel >= TConstructRegistry.getMaterial("Copper").harvestLevel()
                            && hLevel < TConstructRegistry.getMaterial("Manyullyn").harvestLevel()
                                    ? LevelingLogic.getRequiredXp(equipped, tags) - headXP
                                    : -1;

                    if (requiredHeadXP < requiredToolXP && requiredHeadXP > 0) LevelingLogic
                            .updateXP(equipped, entityplayermp, toolXP + requiredHeadXP, headXP + requiredHeadXP);
                    else LevelingLogic
                            .updateXP(equipped, entityplayermp, toolXP + requiredToolXP, headXP + requiredToolXP);

                    if (entityplayermp != icommandsender) {
                        // TODO: Find replacement for notifyAdmins
                        // notifyAdmins(icommandsender, 1, "Leveled up %s's tool", new
                        // Object[]{entityplayermp.getEntityName()});
                    } else {
                        // TODO: Find replacement for notifyAdmins
                        // notifyAdmins(icommandsender, 1, "Leveled up their own tool", new Object[]{});
                    }

                } else throw new WrongUsageException("Players tool is already max level", new Object[0]);
            } else throw new WrongUsageException(
                    "Player must have a levelable Tinker's Construct tool in hand",
                    new Object[0]);
        } else throw new WrongUsageException("Player must have a Tinker's Construct tool in hand", new Object[0]);
    }

    /**
     * Parses an int from the given sring with a specified minimum.
     */
    public static int parseIntWithMinMax(ICommandSender par0ICommandSender, String par1Str, int min, int max) {
        return parseIntBounded(par0ICommandSender, par1Str, min, max);
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender) {
        return null;
    }

    @Override
    public int compareTo(Object arg0) {
        return 0;
    }
}
