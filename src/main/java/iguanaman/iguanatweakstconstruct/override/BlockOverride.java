package iguanaman.iguanatweakstconstruct.override;

import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.oredict.OreDictionary;

import iguanaman.iguanatweakstconstruct.harvestlevels.HarvestLevelTweaks;
import iguanaman.iguanatweakstconstruct.reference.Config;
import iguanaman.iguanatweakstconstruct.util.HarvestLevels;
import iguanaman.iguanatweakstconstruct.util.Log;

public class BlockOverride implements IOverride {

    @Override
    public void createDefault(Configuration config) {
        Log.debug("Creating Block Default File");

        // oredict entries
        for (int i = 0; i < HarvestLevelTweaks.allOreDicLevels.length; i++)
            for (String entry : HarvestLevelTweaks.allOreDicLevels[i])
                if (entry != null) config.get("generaloredict", entry, i);

        // blocks can not really be added automatically because derp
        for (Object key : Block.blockRegistry.getKeys()) {
            Block block = (Block) Block.blockRegistry.getObject(key);

            int meta = -1;
            LinkedList<Integer> metas = new LinkedList<Integer>();
            while (++meta < 16) {
                if (block.getHarvestLevel(meta) == -1) continue;
                try {
                    String s = new ItemStack(block, 1, meta).getDisplayName();
                    if (s == null || s.isEmpty()) continue;
                } catch (Exception e) // bad practice to catch exception, but it ensures that mc doesn't crash if
                                      // modders do weird stuff
                {
                    continue;
                }

                metas.add(meta);
            }

            if (metas.isEmpty()) continue;

            // write it down
            for (Integer m : metas)
                config.get("blocks_" + block.getHarvestTool(m), key.toString() + ":" + m, block.getHarvestLevel(m));
        }
    }

    @Override
    public void processConfig(Configuration config) {
        Log.debug("Loading Block Overrides");

        StringBuilder comment = new StringBuilder();
        comment.append(
                "To change the harvestability, create a 'blocks_<toolclass>' category. See BlockDefaults for examples.\n");
        comment.append("Format of the block entries: <mod-id>:<name>:<metadata>=<harvestlevel>\n");
        comment.append("A metadata of -1 will apply the setting for all metadatas.\n");
        comment.append("A harvestlevel of -1 removes the tools effectiveness for this block.\n");
        comment.append("You can also add new properties. An Example:\n");
        comment.append("\tblocks_pickaxe { I:\"minecraft:chest:-1\"=0 }\n");
        comment.append("\tblocks_axe { I:\"minecraft:chest:-1\"=-1 }\n");
        comment.append(
                "This changes the chest to be harvestable by pickaxes instead of axes. Note that this does not make any sense, since chests don't require a tool to break it.\n\n");

        comment.append("Mining Levels:\n");
        for (int i = 0; i <= HarvestLevels.max; i++)
            comment.append(String.format("\t%d - %s\n", i, HarvestLevels.getHarvestLevelName(i)));

        config.setCategoryComment(" Info", comment.toString());

        config.setCategoryComment(
                "generaloredict",
                "Adapt harvestability of all oredicted blocks here. The name is just the postfix, so instead of 'oreIron' you use 'Iron' and it changes all the iron oredcits it can find. like oreIron, denseoreIron, blockIron,...");
        config.setCategoryComment(
                "oredict",
                "Adapt harvestability of specific oredicted blocks here. The name must be exact, so use 'oreIron' instead of 'Iron' here. Overwrites harvestlevels set by generaloredict.");

        // general oredict entries first
        ConfigCategory cat = config.getCategory("generaloredict");
        for (Property prop : cat.values()) {
            HarvestLevelTweaks.modifyOredictBlock(prop.getName(), prop.getInt());
        }

        // specific oredict entries next
        cat = config.getCategory("oredict");
        for (Property prop : cat.values()) {
            for (ItemStack stack : OreDictionary.getOres(prop.getName()))
                HarvestLevelTweaks.modifyBlock(stack, prop.getInt());
        }

        // block specific harvest levels.. urgh
        for (String catname : config.getCategoryNames()) {
            if (!catname.startsWith("blocks_")) continue;

            cat = config.getCategory(catname);
            String tool = catname.substring(7); // 'b' 'l' 'o' 'c' 'k' 's' '_'. blocks_. 7 letters.

            for (Property prop : cat.values()) {
                // dissect the name.
                String[] foo = prop.getName().split(":");
                String metaStr = foo[foo.length - 1];
                String blockStr = prop.getName().substring(0, prop.getName().lastIndexOf(':'));
                int meta;
                // check if the meta-str actually is a number
                try {
                    meta = Integer.valueOf(metaStr);
                } catch (NumberFormatException e) {
                    // no metadata string present. Is required.
                    continue;
                }

                // find the block
                if (!Block.blockRegistry.containsKey(blockStr)) continue;

                Block block = (Block) Block.blockRegistry.getObject(blockStr);
                int lvl = prop.getInt();
                if (meta == -1) {
                    block.setHarvestLevel(tool, prop.getInt());
                    if (Config.logOverrideChanges) Log.info(
                            String.format(
                                    "Block Override: Changed Harvest Level of %s:%d to %d",
                                    block.getUnlocalizedName(),
                                    meta,
                                    lvl));
                } else {
                    block.setHarvestLevel(tool, prop.getInt(), meta);
                    if (Config.logOverrideChanges) Log.info(
                            String.format(
                                    "Block Override: Changed Harvest Level of %s:%d to %d",
                                    block.getUnlocalizedName(),
                                    meta,
                                    lvl));
                }
            }
        }
    }
}
