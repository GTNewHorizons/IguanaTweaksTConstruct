package iguanaman.iguanatweakstconstruct.restriction;

import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.common.config.Configuration;

import iguanaman.iguanatweakstconstruct.reference.Reference;
import iguanaman.iguanatweakstconstruct.util.Log;
import mantle.utils.ItemMetaWrapper;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.tools.ToolMaterial;

public class RestrictionConfig {

    private Configuration configfile;
    // public static Map<String, Set<Integer>> restrictedParts = new HashMap<String, Set<Integer>>();

    public void init(String fileName) {
        configfile = new Configuration(Reference.configFile(fileName));
        configfile.load();

        Log.debug("Applying Tool Part restrictions");
        configfile.setCategoryComment(
                "ToolParts",
                "Restriction Module: Allows to blacklist certain ToolParts from being created with specific Materials!\nThe allowed and restricted entries are (=should be) equal. They're just both there for visibility.\nAny material not listed in any category will stay untouched.");
        loadRestrictedParts();
        loadAllowedParts();

        RestrictionHelper.sortEntries();
        configfile.save();
    }

    public void loadRestrictedParts() {
        // construct the comment containing all the info needed :I
        StringBuilder comment = new StringBuilder();
        comment.append("Prevents the creation of listed Material-Tool combinations.\n");
        comment.append("The format is <materialname>:<partname>\n");
        // material names
        comment.append("materialnames are: ");
        ToolMaterial[] mats = TConstructRegistry.toolMaterials.values()
                .toArray(new ToolMaterial[TConstructRegistry.toolMaterials.size()]);
        for (int i = 0; i < mats.length; i++) {
            comment.append(mats[i].materialName);
            if (i < mats.length - 1) comment.append(", ");
        }
        comment.append('\n');
        // part names
        comment.append("partnames are: ");
        // patterns
        for (String name : RestrictionHelper.configNameToPattern.keySet()) {
            // exclude fletching and bowstring
            if ("bowstring".equals(name) || "fletching".equals(name)) continue;

            comment.append(name);
            comment.append(", ");
        }
        comment.append("\n");

        // load the actual config after creating this long information comment ._.
        String[] input = configfile
                .getStringList("restricted", "ToolParts", RestrictionHelper.defaultRestrictions, comment.toString());

        // work through the entries
        for (String str : input) {
            if (str.isEmpty()) continue;
            String[] restriction = str.split(":");
            ToolMaterial material = null;

            if (restriction.length != 2) {
                Log.error(String.format("Found invalid restriction entry: %s", str));
                continue;
            }

            // check if valid material
            material = TConstructRegistry.getMaterial(restriction[0]);
            if (material == null) {
                Log.error(String.format("Found invalid material %s in restriction entry: %s", restriction[0], str));
                continue;
            }

            // check if we have to add all
            if ("all".equals(restriction[1])) {
                for (ItemMetaWrapper key : RestrictionHelper.configNameToPattern.values())
                    RestrictionHelper.addRestriction(key, material);
                for (ItemMetaWrapper key : RestrictionHelper.configNameToCast.values())
                    RestrictionHelper.addRestriction(key, material);

                continue;
            }

            // check if valid part
            boolean validPattern = RestrictionHelper.configNameToPattern.keySet().contains(restriction[1]);
            boolean validCast = RestrictionHelper.configNameToCast.keySet().contains(restriction[1]);

            if (!validPattern && !validCast) {
                Log.error(String.format("Found invalid part %s in restriction entry: %s", restriction[1], str));
                continue;
            }

            // add restriction :)
            if (validPattern)
                RestrictionHelper.addRestriction(RestrictionHelper.configNameToPattern.get(restriction[1]), material);
            if (validCast)
                RestrictionHelper.addRestriction(RestrictionHelper.configNameToCast.get(restriction[1]), material);
        }
    }

    public void loadAllowedParts() {
        StringBuilder comment = new StringBuilder();
        comment.append(
                "This section is a negative of the above restricted section, and will be applied AFTER restricted parts.\n");
        comment.append(
                "That means only the parts listed here will be craftable, none of the other parts with this material.\n");
        comment.append(
                "If a Material does not show up here, it will be unmodified. Otherwise all other recipes for this material will be deleted.\n");
        comment.append(
                "ATTENTION: THIS DOES NOT ALLOW YOU TO ADD NEW RECIPES. ONLY EXISTING ONES WORK. This exists purely for convenience.");
        comment.append("(materialnames and partnames are the same as restricted parts)");
        String[] input = configfile
                .getStringList("allowed", "ToolParts", RestrictionHelper.defaultAllowed, comment.toString());

        // set of already encountered materials
        Set<ToolMaterial> memory = new HashSet<ToolMaterial>();

        // work through the entries
        for (String str : input) {
            if (str.isEmpty()) continue;
            String[] restriction = str.split(":");
            ToolMaterial material = null;

            if (restriction.length != 2) {
                Log.error(String.format("Found invalid allow-entry: %s", str));
                continue;
            }

            // check if valid material
            // check if valid material
            material = TConstructRegistry.getMaterial(restriction[0]);
            if (material == null) {
                Log.error(String.format("Found invalid material %s in allow-entry: %s", restriction[0], str));
                continue;
            }

            // check if valid part
            boolean validPattern = RestrictionHelper.configNameToPattern.keySet().contains(restriction[1]);
            boolean validCast = RestrictionHelper.configNameToCast.keySet().contains(restriction[1]);

            if (!validPattern && !validCast) {
                Log.error(String.format("Found invalid part %s in allow-entry: %s", restriction[1], str));
                continue;
            }

            // check if we have to clean up first
            if (!memory.contains(material)) {
                RestrictionHelper.clearRecipes(material);
                memory.add(material);
            }

            boolean success = false;
            // then allow it
            if (validPattern) success |= RestrictionHelper
                    .addAllowed(RestrictionHelper.configNameToPattern.get(restriction[1]), material);
            if (validCast) success |= RestrictionHelper
                    .addAllowed(RestrictionHelper.configNameToCast.get(restriction[1]), material);

            if (!success) Log.error(String.format("You're trying to allow an invalid recipe: %s", str));
        }
    }
}
