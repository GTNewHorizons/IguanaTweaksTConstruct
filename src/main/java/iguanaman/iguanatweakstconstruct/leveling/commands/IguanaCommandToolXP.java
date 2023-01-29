package iguanaman.iguanatweakstconstruct.leveling.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import tconstruct.library.TConstructRegistry;
import tconstruct.library.tools.ToolCore;
import iguanaman.iguanatweakstconstruct.leveling.LevelingLogic;

public class IguanaCommandToolXP extends CommandBase {

    @Override
    public String getCommandName() {
        return "toolxp";
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
        if (astring.length > 0) {
            EntityPlayerMP entityplayermp = astring.length >= 2 ? getPlayer(icommandsender, astring[0])
                    : getCommandSenderAsPlayer(icommandsender);
            int xp = astring.length >= 2 ? parseIntWithMinMax(icommandsender, astring[1], 0, Integer.MAX_VALUE)
                    : parseIntWithMinMax(icommandsender, astring[0], 0, Integer.MAX_VALUE);
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

                        LevelingLogic.updateXP(equipped, entityplayermp, toolXP + xp, headXP + xp);

                        if (entityplayermp != icommandsender) {
                            // TODO: find replacement for notifyAdmins
                            // possibly RConConsoleSource?
                            // notifyAdmins(icommandsender, 1, "Added " + xp + " to %s's tool", new
                            // Object[]{entityplayermp.getEntityName()});
                        } else {
                            // TODO: find replacement for notifyAdmins
                            // notifyAdmins(icommandsender, 1, "Added " + xp + " to their own tool", new Object[]{});
                        }
                    } else throw new WrongUsageException("Players tool is already max level", new Object[0]);
                } else throw new WrongUsageException(
                        "Player must have a levelable Tinker's Construct tool in hand",
                        new Object[0]);
            } else throw new WrongUsageException("Player must have a Tinker's Construct tool in hand", new Object[0]);
        } else throw new WrongUsageException("toolxp [player] <value>", new Object[0]);
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
    public int compareTo(Object o) {
        // TODO Auto-generated method stub
        return 0;
    }

}
