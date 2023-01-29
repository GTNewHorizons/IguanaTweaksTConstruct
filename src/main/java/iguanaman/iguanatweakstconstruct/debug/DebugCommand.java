package iguanaman.iguanatweakstconstruct.debug;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;

import tconstruct.library.crafting.ToolBuilder;
import tconstruct.library.tools.ToolCore;
import tconstruct.tools.TinkerTools;
import iguanaman.iguanatweakstconstruct.leveling.RandomBonuses;

public class DebugCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "igdebug";
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
            // Config.randomBonusesAreUseful = true;
            // Config.randomBonusesAreRandom = false;
            HashMap<RandomBonuses.Modifier, Integer> foo = new HashMap<RandomBonuses.Modifier, Integer>();
            for (int i = 0; i < 1000; i++) {
                {
                    // pick
                    // ItemStack head = new ItemStack(TinkerTools.pickaxeHead, 0, 2);
                    // ItemStack handle = new ItemStack(TinkerTools.toolRod, 0, 3);
                    // ItemStack accessory = new ItemStack(TinkerTools.binding, 0, 4);
                    // sword
                    // ItemStack head = new ItemStack(TinkerTools.swordBlade, 0, 2);
                    // ItemStack handle = new ItemStack(TinkerTools.toolRod, 0, 3);
                    // ItemStack accessory = new ItemStack(TinkerTools.wideGuard, 0, 4);
                    // bow
                    ItemStack head = new ItemStack(TinkerTools.toolRod, 0, 2);
                    ItemStack handle = new ItemStack(TinkerTools.bowstring, 0, 0);
                    ItemStack accessory = new ItemStack(TinkerTools.toolRod, 0, 4);
                    ItemStack tool = ToolBuilder.instance.buildTool(head, handle, accessory, "testtool");
                    for (int j = 0; j < 5; j++) {
                        RandomBonuses.Modifier mod = RandomBonuses.tryModifying(entityplayermp, tool);
                        if (!foo.containsKey(mod)) foo.put(mod, 1);
                        else foo.put(mod, foo.get(mod) + 1);
                    }
                }
            }

            for (Map.Entry<RandomBonuses.Modifier, Integer> bar : foo.entrySet())
                icommandsender.addChatMessage(new ChatComponentText(bar.getKey().toString() + ": " + bar.getValue()));
            /*
             * NBTTagCompound tags = equipped.getTagCompound().getCompoundTag("InfiTool"); int tipNum = 0; while (true)
             * { tipNum++; String tip = "Tooltip" + tipNum; String modTip = "ModifierTip" + tipNum; boolean found =
             * false; if (tags.hasKey(tip)) { Log.info(tip + ": " + tags.getString(tip)); found = true; } if
             * (tags.hasKey(modTip)) { Log.info(modTip + ": " + tags.getString(tip)); found = true; } if (!found) break;
             * }
             */
        }
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender) {
        return null;
    }

}
