package iguanaman.iguanatweakstconstruct.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.oredict.OreDictionary;

import iguanaman.iguanatweakstconstruct.util.Log;

public class CommandDumpOredict extends CommandBase {

    @Override
    public String getCommandName() {
        return "dumpOredict";
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
        File file = new File("dump_OreDict.txt");
        try {
            PrintWriter pw = new PrintWriter(file);
            for (String ore : OreDictionary.getOreNames()) pw.println(ore);

            pw.close();

            icommandsender.addChatMessage(new ChatComponentText("Dumped oredict names to " + file.getName()));
        } catch (FileNotFoundException e) {
            Log.error("Couldn't create file " + file.getName());
            icommandsender.addChatMessage(new ChatComponentText("Couldn't create file " + file.getName()));
        }
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender) {
        return "/dumpOredict";
    }

}
