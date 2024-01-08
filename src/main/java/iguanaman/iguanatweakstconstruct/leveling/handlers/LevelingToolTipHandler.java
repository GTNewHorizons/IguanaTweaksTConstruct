package iguanaman.iguanatweakstconstruct.leveling.handlers;

import java.util.List;
import java.util.ListIterator;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import iguanaman.iguanatweakstconstruct.OldToolConversionHandler;
import iguanaman.iguanatweakstconstruct.leveling.IguanaToolLeveling;
import iguanaman.iguanatweakstconstruct.leveling.LevelingLogic;
import iguanaman.iguanatweakstconstruct.leveling.LevelingTooltips;
import iguanaman.iguanatweakstconstruct.reference.Config;
import iguanaman.iguanatweakstconstruct.util.ModSupportHelper;
import iguanaman.iguanatweakstconstruct.util.TooltipHelper;
import tconstruct.items.tools.Hammer;
import tconstruct.items.tools.Pickaxe;
import tconstruct.library.tools.ToolCore;

public class LevelingToolTipHandler {

    // the prefix used for "+ X attack damage". Thanks Tic Tooltips ;)
    public static String plusPrefix = "\u00A79+";

    @SubscribeEvent(priority = EventPriority.HIGH) // insert before tic-tooltips
    public void onItemToolTip(ItemTooltipEvent event) {
        if (event.entityPlayer == null) return;

        ItemStack stack = event.itemStack;

        if (stack.getItem() == IguanaToolLeveling.rubberChicken && stack.hasTagCompound()
                && stack.getTagCompound().getCompoundTag("InfiTool").hasKey("Original")) {
            event.toolTip.add(
                    1,
                    EnumChatFormatting.DARK_RED.toString() + EnumChatFormatting.ITALIC
                            + StatCollector.translateToLocal("tooltip.chicken1"));
            event.toolTip.add(2, EnumChatFormatting.DARK_RED + StatCollector.translateToLocal("tooltip.chicken2"));
            return;
        }

        // we're only interested in tinker tools, obviously
        if (!(stack.getItem() instanceof ToolCore) || !stack.hasTagCompound()) return;

        // don't display tooltip when CTRL is held (also tic tooltips compatibility)
        if (TooltipHelper.ctrlHeld()) return;

        // first off, let's check if we have to display a warning.
        if (OldToolConversionHandler.toolNeedsUpdating(stack)) {
            event.toolTip
                    .add(1, EnumChatFormatting.DARK_RED + StatCollector.translateToLocal("tooltip.oldToolWarning1"));
            event.toolTip
                    .add(2, EnumChatFormatting.DARK_RED + StatCollector.translateToLocal("tooltip.oldToolWarning2"));
        }

        ToolCore tool = (ToolCore) event.itemStack.getItem();
        NBTTagCompound tags = stack.getTagCompound().getCompoundTag(tool.getBaseTagName()); // tinker tags

        // we only need a tooltip if we have a level
        if (!LevelingLogic.hasLevel(tags)) return;

        // find spot to insert our tooltip data
        ListIterator<String> inserter = findInsertSpot(event.toolTip);
        // does the user hold shift?
        boolean advanced = TooltipHelper.shiftHeld();
        // only allow advanced (xp) tooltip if config option is set
        advanced &= Config.showTooltipXP;

        boolean hasMiningLevel = tool.getHarvestLevel(event.itemStack, "pickaxe") >= 0 || tool instanceof Pickaxe
                || tool instanceof Hammer;

        // add mining level if applicable
        if (hasMiningLevel) {
            int hLevel = tags.getInteger("HarvestLevel");
            String mLvl = LevelingTooltips.getMiningLevelTooltip(hLevel);
            // display minimal tooltip?
            if (Config.showMinimalTooltipXP && !advanced && Config.levelingPickaxeBoost) {
                if (LevelingLogic.hasBoostXp(tags) && LevelingLogic.canBoostMiningLevel(tags))
                    mLvl += " (" + LevelingTooltips.getBoostXpString(stack, tags, false) + ")";
            }

            inserter.add(mLvl);

            // display extended tooltip?
            if (Config.showTooltipXP && advanced && LevelingLogic.hasBoostXp(tags) && Config.pickaxeBoostRequired) {
                // xp if not boosted
                if (LevelingLogic.canBoostMiningLevel(tags))
                    inserter.add(LevelingTooltips.getBoostXpToolTip(stack, tags));
                // otherwise boosted message
                else if (LevelingLogic.isBoosted(tags)) inserter.add(LevelingTooltips.getBoostedTooltip());
            }
        }

        // add skill level
        int level = LevelingLogic.getLevel(tags);
        if (level > 0 && Config.toolLeveling) {
            String lvl = LevelingTooltips.getLevelTooltip(level);
            if (!advanced && Config.showMinimalTooltipXP && !LevelingLogic.isMaxLevel(tags))
                lvl += " (" + LevelingTooltips.getXpString(stack, tags, false) + ")";
            inserter.add(lvl);

            // skill level progress
            if (advanced && Config.showTooltipXP && !LevelingLogic.isMaxLevel(tags))
                inserter.add(LevelingTooltips.getXpToolTip(stack, tags));
        }

        // since we added at least one line we'll add an empty spacing line at the end
        inserter.add("");

        // add info that you can hold shift for more details
        if (!advanced && Config.showTooltipXP && !ModSupportHelper.tiCTooltips && Config.toolLeveling) // don't display
                                                                                                       // if TicToolTips
                                                                                                       // is installed
            inserter.add(
                    StatCollector.translateToLocalFormatted(
                            "tooltip.level.advanced",
                            EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.ITALIC
                                    + "Shift"
                                    + EnumChatFormatting.RESET
                                    + EnumChatFormatting.GRAY));

        // remove the trailing empty line we used as insert reference n stuff
        String empty = inserter.next();
        if (empty.isEmpty()) inserter.remove();
    }

    private ListIterator<String> findInsertSpot(List<String> tooltip) {
        ListIterator<String> iterator = tooltip.listIterator();

        // progress to the end, check if there's a "+ damage" stuff
        while (iterator.hasNext()) {
            String str = iterator.next();
            if (str.isEmpty() || str.startsWith(plusPrefix)) {
                iterator.previous();
                break;
            }
            /*
             * else if(Loader.isModLoaded("TiCTooltips") && str.contains("Shift")) { iterator.previous(); break; }
             */
        }

        // iterator.previous();

        // we're either directly before the "+ damage" or at the end now

        return iterator;
    }
}
