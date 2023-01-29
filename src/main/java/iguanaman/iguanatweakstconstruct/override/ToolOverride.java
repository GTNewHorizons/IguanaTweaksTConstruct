package iguanaman.iguanatweakstconstruct.override;

import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import iguanaman.iguanatweakstconstruct.harvestlevels.HarvestLevelTweaks;
import iguanaman.iguanatweakstconstruct.reference.Config;
import iguanaman.iguanatweakstconstruct.util.HarvestLevels;
import iguanaman.iguanatweakstconstruct.util.Log;

public class ToolOverride implements IOverride {

    @Override
    public void createDefault(Configuration config) {
        Log.debug("Creating Tool Default File");

        for (Object identifier : Item.itemRegistry.getKeys()) {
            Object o = Item.itemRegistry.getObject(identifier);
            if (!(o instanceof Item) || o instanceof ItemBlock) continue;

            Item item = (Item) o;
            ItemStack stack = new ItemStack(item); // let's assume there are no sick bastards who use metadata to group
                                                   // tools into a singular id
            // ._.

            String saneCategory = buildCategory(identifier.toString());

            for (String tool : item.getToolClasses(stack)) {
                int level = item.getHarvestLevel(stack, tool);
                config.get(saneCategory, tool, level).getInt();
            }
        }
    }

    @Override
    public void processConfig(Configuration config) {
        Log.debug("Loading Tool Overrides");

        StringBuilder comment = new StringBuilder();
        comment.append(
                "Copy the desired tools you want to change from the defaults file into this file and adapt the stats.\n\n");

        comment.append("Mining Levels:\n");
        for (int i = 0; i <= HarvestLevels.max; i++)
            comment.append(String.format("\t%d - %s\n", i, HarvestLevels.getHarvestLevelName(i)));

        config.setCategoryComment(" Info", comment.toString());
        Property prop = config.get(
                " Info",
                "fillWithDefault",
                false,
                "Fills this file with the default values, not altering changed values.");
        boolean fillWithDefault = prop.getBoolean();
        prop.set(false);
        prop = config.get(
                " Info",
                "removeDefaultValues",
                false,
                "Removes all entries that correspond to the default values.");
        boolean cleanup = prop.getBoolean();
        prop.set(false);

        for (Object identifier : Item.itemRegistry.getKeys()) {
            Object o = Item.itemRegistry.getObject(identifier);
            if (!(o instanceof Item) || o instanceof ItemBlock) continue;
            // only load if it has a value
            String saneCategory = buildCategory(identifier.toString());

            if (!config.hasCategory(saneCategory) && !fillWithDefault) continue;

            Item item = (Item) o;
            ItemStack stack = new ItemStack(item); // let's assume there are no sick bastards who use metadata to group
                                                   // tools into a singular id
            // ._.

            boolean changed = false;
            for (String tool : item.getToolClasses(stack)) {
                int level = item.getHarvestLevel(stack, tool);
                prop = config.get(saneCategory, tool, level);
                int newLevel = prop.getInt();

                // update tool
                if (level != newLevel) {
                    HarvestLevelTweaks.updateToolHarvestLevel(item, tool, newLevel);
                    if (Config.logOverrideChanges) Log.info(
                            String.format(
                                    "Tool Override: Changed harvest level of %s to %d",
                                    item.getUnlocalizedName(),
                                    newLevel));
                    changed = true;
                }
            }
            if (!changed && cleanup) config.removeCategory(config.getCategory(saneCategory));
        }
    }

    private String buildCategory(String identifier) {
        // make it sane
        String cat = identifier.replace(Configuration.CATEGORY_SPLITTER, "_"); // replace '.' in string.. blah. this
                                                                               // sucks
        // then split it into subcategory of mod-id
        return cat.replaceFirst(":", Configuration.CATEGORY_SPLITTER).toLowerCase();
    }
}
