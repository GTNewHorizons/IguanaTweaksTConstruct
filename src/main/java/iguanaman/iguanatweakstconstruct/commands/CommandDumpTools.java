package iguanaman.iguanatweakstconstruct.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.ChatComponentText;

import iguanaman.iguanatweakstconstruct.util.Log;

public class CommandDumpTools extends CommandBase {

    @Override
    public String getCommandName() {
        return "dumpTools";
    }

    @Override
    public String getCommandUsage(ICommandSender iCommandSender) {
        return "/dumpTools";
    }

    @Override
    public void processCommand(ICommandSender icommandsender, String[] input) {
        File file = new File("dump_Tools.txt");
        try {
            PrintWriter pw = new PrintWriter(file);
            // we sort the names, otherwise it's quite hard to find something in the list
            List<String> itemnames = new LinkedList<String>(Item.itemRegistry.getKeys());
            Collections.sort(itemnames);
            // iterate through all registered item keys (their names)
            for (Object key : itemnames) {
                // get the object for the key
                Object item = Item.itemRegistry.getObject(key);
                // if the object is a tool, we dump it
                if (item instanceof ItemTool || item instanceof ItemHoe
                        || item instanceof ItemSword
                        || item instanceof ItemBow)
                    pw.println(key.toString());
                // if it's not one of these, but still has a toolclass, we consider it tool.
                // else if(item instanceof Item)
                // if(!((Item) item).getToolClasses(null).isEmpty())
                // pw.println(key.toString());
            }

            pw.close();

            icommandsender.addChatMessage(new ChatComponentText("Dumped tool names to " + file.getName()));
        } catch (FileNotFoundException e) {
            Log.error("Couldn't create file " + file.getName());
            icommandsender.addChatMessage(new ChatComponentText("Couldn't create file " + file.getName()));
        }
    }
}
