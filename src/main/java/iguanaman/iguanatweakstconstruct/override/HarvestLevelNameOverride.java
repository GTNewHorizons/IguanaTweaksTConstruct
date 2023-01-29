package iguanaman.iguanatweakstconstruct.override;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import tconstruct.library.TConstructRegistry;
import tconstruct.library.tools.ToolMaterial;
import iguanaman.iguanatweakstconstruct.reference.Config;
import iguanaman.iguanatweakstconstruct.util.HarvestLevels;
import iguanaman.iguanatweakstconstruct.util.Log;

public class HarvestLevelNameOverride implements IOverride {

    @Override
    public void createDefault(Configuration config) {
        Log.debug("Creating Harvest Level Name Default File");

        config.get("HarvestLevelNames", "Level0", "Stone");
        config.get("HarvestLevelNames", "Level1", "Copper");
        config.get("HarvestLevelNames", "Level2", "Iron");
        config.get("HarvestLevelNames", "Level3", "Bronze");
        config.get("HarvestLevelNames", "Level4", "Steel");
        config.get("HarvestLevelNames", "Level5", "Obsidian");
        config.get("HarvestLevelNames", "Level6", "Ardite");
        config.get("HarvestLevelNames", "Level7", "Cobalt");
        config.get("HarvestLevelNames", "Level8", "Manyullyn");
    }

    @Override
    public void processConfig(Configuration config) {
        Log.debug("Loading Harvest Level Name Overrides");

        ConfigCategory cat = config.getCategory("HarvestLevelNames");
        cat.setComment(
                "Use materialnames to set the name of a harvest level. The entrties have to either be identifiers for Tinker-Materials OR the registered names of items.\nFor Example: 'Level0=wood' would change the first harvest level to wood from stone. Likewise 'Level5=minecraft:emerald' would change that level to Emerald.");

        Map<Integer, ToolMaterial> mats = new HashMap<Integer, ToolMaterial>();

        for (Property prop : cat.values()) {
            if (!prop.getName().startsWith("Level")) {
                Log.error("Invalid entry: " + prop.getName());
                continue;
            }
            Integer lvl;
            try {
                lvl = Integer.valueOf(prop.getName().substring(5));
            } catch (NumberFormatException e) {
                Log.error("Invalid entry: " + prop.getName());
                continue;
            }

            // find material. we have to loop through all materials, because configs don't like case sensitivity.
            String matName = prop.getString().toLowerCase();
            boolean found = false;
            for (ToolMaterial mat : TConstructRegistry.toolMaterials.values())
                if (matName.equals(mat.name().toLowerCase())) {
                    mats.put(lvl, mat);
                    if (Config.logOverrideChanges) Log.info(
                            String.format(
                                    "Harvest Level Name Override: Changed Level %s to %s",
                                    lvl,
                                    mat.materialName));
                    found = true;
                    break;
                }

            // if it's not a material, we try items
            if (!found) {
                matName = prop.getString();
                Item item = (Item) Item.itemRegistry.getObject(matName);
                if (item != null) {
                    String name = (new ItemStack(item)).getDisplayName();
                    tconstruct.library.util.HarvestLevels.harvestLevelNames.put(lvl, name);

                    if (Config.logOverrideChanges)
                        Log.info(String.format("Harvest Level Name Override: Changed Level %s to %s", lvl, name));
                    found = true;
                }
            }

            if (!found) {
                Log.error("No Tinkers Construct material found: " + prop.getString());
                Log.error("Entries have to be a registered material for Tinker Tools!");
            }
        }

        HarvestLevels.setCustomHarvestLevelNames(mats);
    }
}
