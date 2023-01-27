package iguanaman.iguanatweakstconstruct.restriction;

import iguanaman.iguanatweakstconstruct.util.Log;
import java.util.*;
import mantle.utils.ItemMetaWrapper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.crafting.CastingRecipe;
import tconstruct.library.tools.BowstringMaterial;
import tconstruct.library.tools.CustomMaterial;
import tconstruct.library.tools.FletchingMaterial;
import tconstruct.library.tools.ToolMaterial;
import tconstruct.library.util.IToolPart;
import tconstruct.smeltery.TinkerSmeltery;
import tconstruct.smeltery.items.MetalPattern;
import tconstruct.tools.TinkerTools;
import tconstruct.tools.items.Pattern;
import tconstruct.weaponry.TinkerWeaponry;

public final class RestrictionHelper {
    private RestrictionHelper() {} // non-instantiable

    public static Map<String, ItemMetaWrapper>
            configNameToPattern; // holds the names that can be used in the config and maps them to item-meta
    // combinations to retrieve the materials
    public static Map<String, ItemMetaWrapper> configNameToCast; // same as above but for metal casts
    // this list contains all ALLOWED pattern - material combinations
    public static Map<ItemMetaWrapper, List<ToolMaterial>>
            patternMaterialLookup; // item+metadata -> List of applicable materials
    public static Map<ItemMetaWrapper, List<CustomMaterial>>
            patternCustomMaterialLookup; // item+metadata -> List of applicable custom materials

    static {
        configNameToPattern = new HashMap<String, ItemMetaWrapper>();
        configNameToCast = new HashMap<String, ItemMetaWrapper>();
        patternMaterialLookup = new HashMap<ItemMetaWrapper, List<ToolMaterial>>();
        patternCustomMaterialLookup = new HashMap<ItemMetaWrapper, List<CustomMaterial>>();
    }

    public static boolean isRestricted(ItemStack pattern, ToolMaterial material) {
        boolean restricted = true;
        List<ToolMaterial> matIDs = patternMaterialLookup.get(new ItemMetaWrapper(pattern));
        if (matIDs != null) {
            for (ToolMaterial mat : matIDs)
                if (mat.materialName.equals(material.materialName)) {
                    restricted = false;
                    break;
                }
        }

        return restricted;
    }

    public static int getPatternIndexFromName(String name) {
        for (int i = 0; i < patternNames.length; i++) if (patternNames[i].equals(name)) return i;

        return -1;
    }

    public static List<ToolMaterial> getPatternMaterials(ItemStack pattern) {
        return patternMaterialLookup.get(new ItemMetaWrapper(pattern));
    }

    public static List<CustomMaterial> getPatternCustomMaterials(ItemStack pattern) {
        return patternCustomMaterialLookup.get(new ItemMetaWrapper(pattern));
    }

    public static void addRestriction(ItemMetaWrapper key, ToolMaterial material) {
        // fetch the material list
        List<ToolMaterial> materials = patternMaterialLookup.get(key);
        if (materials == null) {
            Log.debug(String.format("Couldn't find lookup entry for %s:%d", key.item.getUnlocalizedName(), key.meta));
            return;
        }

        // find the entry and remove it
        ListIterator<ToolMaterial> iter = materials.listIterator();
        while (iter.hasNext()) {
            ToolMaterial mat = iter.next();
            if (mat.materialName.equals(material.materialName)) {
                iter.remove();
            }
        }
    }

    // removes all recipes that use a certain tool from the list
    public static void clearRecipes(ToolMaterial material) {
        for (List<ToolMaterial> mats : patternMaterialLookup.values()) {
            ListIterator<ToolMaterial> iter = mats.listIterator();
            while (iter.hasNext()) {
                ToolMaterial mat = iter.next();
                if (mat.materialName.equals(material.materialName)) {
                    iter.remove();
                }
            }
        }
    }

    // Don't judge me. This function is an absolute terror because I realized that it'd allow invalid combinations if I
    // don't check everything again too late. Should probbaly simply create a set of originally allowed parts, and than
    // build the overlapping set.
    public static boolean addAllowed(ItemMetaWrapper key, ToolMaterial material) {
        // check if material was allowed for casting
        if (key.item instanceof MetalPattern) {
            boolean allowed = false;
            for (CastingRecipe recipe : TConstructRegistry.getTableCasting().getCastingRecipes()) {
                // only recipes with cast
                if (recipe.cast == null || recipe.output == null) continue;
                // we're only interested in toolparts, not making the casts themselves etc. or buckets.
                if (!(recipe.output.getItem() instanceof IToolPart)) continue;

                int matID = ((IToolPart) recipe.output.getItem()).getMaterialID(recipe.output);

                if (key.item == recipe.cast.getItem()
                        && key.meta == recipe.cast.getItemDamage()
                        && TConstructRegistry.getMaterial(matID).materialName.equals(material.materialName)) {
                    allowed = true;
                    break;
                }
            }
            // nope. we don't want wood as valid casting material!
            if (!allowed) return false;
        }
        // wooden pattern?
        else if (key.item instanceof Pattern) {
            boolean allowed = false;
            for (List entry : TConstructRegistry.patternPartMapping.keySet()) {
                Item pattern = (Item) entry.get(0); // the pattern
                int meta = (Integer) entry.get(1); // metadata of the pattern
                int matID = (Integer) entry.get(2); // Material-ID of the material needed to craft

                if (key.item == pattern
                        && key.meta == meta
                        && TConstructRegistry.getMaterial(matID).materialName.equals(material.materialName)) {
                    allowed = true;
                    break;
                }
            }
            if (!allowed) return false;
        }
        List<ToolMaterial> materials = patternMaterialLookup.get(key);
        if (materials == null) {
            Log.debug(String.format("Couldn't find lookup entry for %s:%d", key.item.getUnlocalizedName(), key.meta));
            return false;
        }

        // find the entry so we don't have a double entry
        for (ToolMaterial mat : materials) {
            if (mat.materialName.equals(material.materialName)) {
                // duplicate
                return true;
            }
        }

        // item is not in list. add it
        materials.add(material);

        return true;
    }

    public static void init() {
        initPatternParts();
        initCastParts();
    }

    private static void initPatternParts() {
        Log.debug("Loading tool pattern combinations");
        // cycle through ALL combinations
        for (Map.Entry<List, ItemStack> entry : TConstructRegistry.patternPartMapping.entrySet()) {
            Item pattern = (Item) entry.getKey().get(0); // the pattern
            Integer meta = (Integer) entry.getKey().get(1); // metadata of the pattern
            Integer matID = (Integer) entry.getKey().get(2); // Material-ID of the material needed to craft

            String name;
            if (pattern == TinkerTools.woodPattern && meta <= patternNames.length) name = patternNames[meta];
            else if (pattern == TinkerWeaponry.woodPattern && meta <= patternNamesWeaponry.length)
                name = patternNamesWeaponry[meta];
            else name = (new ItemStack(pattern, 1, meta)).getUnlocalizedName();

            if (name == null || name.isEmpty()) continue;

            ItemMetaWrapper im = configNameToPattern.get(name);
            // not registered in the mapping yet?
            if (im == null) {
                im = new ItemMetaWrapper(pattern, meta);
                configNameToPattern.put(name, im);
            }

            // add material
            if (!patternMaterialLookup.containsKey(im)) patternMaterialLookup.put(im, new LinkedList<ToolMaterial>());

            patternMaterialLookup.get(im).add(TConstructRegistry.getMaterial(matID));
        }

        // bowstring and fletchling are treated differently
        for (CustomMaterial mat : TConstructRegistry.customMaterials) {
            // bowstring
            if (mat instanceof BowstringMaterial) {
                ItemMetaWrapper im = configNameToPattern.get("bowstring");
                // not registered in the mapping yet?
                if (im == null) {
                    im = new ItemMetaWrapper(TinkerTools.woodPattern, 23);
                    configNameToPattern.put("bowstring", im);
                }

                // add material
                if (!patternCustomMaterialLookup.containsKey(im))
                    patternCustomMaterialLookup.put(im, new LinkedList<CustomMaterial>());

                patternCustomMaterialLookup.get(im).add(mat);
            }

            // fletchling
            if (mat instanceof FletchingMaterial) {
                ItemMetaWrapper im = configNameToPattern.get("fletching");
                // not registered in the mapping yet?
                if (im == null) {
                    im = new ItemMetaWrapper(TinkerTools.woodPattern, 24);
                    configNameToPattern.put("fletching", im);
                }

                // add material
                if (!patternCustomMaterialLookup.containsKey(im))
                    patternCustomMaterialLookup.put(im, new LinkedList<CustomMaterial>());

                patternCustomMaterialLookup.get(im).add(mat);
            }
        }
    }

    private static void initCastParts() {
        Log.debug("Loading tool casting combinations");
        // cycle through ALL combinations
        for (CastingRecipe recipe : TConstructRegistry.getTableCasting().getCastingRecipes()) {
            // only recipes with cast
            if (recipe.cast == null || recipe.output == null) continue;
            // we're only interested in toolparts, not making the casts themselves etc. or buckets.
            if (!(recipe.output.getItem() instanceof IToolPart)) continue;

            Item pattern = recipe.cast.getItem();
            int meta = recipe.cast.getItemDamage();
            int matID = ((IToolPart) recipe.output.getItem()).getMaterialID(recipe.output);

            String name;
            if (pattern == TinkerSmeltery.metalPattern && meta <= patternNames.length) name = patternNames[meta];
            else if (pattern == TinkerWeaponry.metalPattern && meta <= patternNamesWeaponry.length)
                name = patternNamesWeaponry[meta];
            else name = (new ItemStack(pattern, 1, meta)).getUnlocalizedName();

            if (name == null || name.isEmpty()) continue;

            ItemMetaWrapper im = configNameToCast.get(name);
            // not registered in the mapping yet?
            if (im == null) {
                im = new ItemMetaWrapper(pattern, meta);
                configNameToCast.put(name, im);
            }

            // add material
            if (!patternMaterialLookup.containsKey(im)) patternMaterialLookup.put(im, new LinkedList<ToolMaterial>());

            patternMaterialLookup.get(im).add(TConstructRegistry.getMaterial(matID));
        }
    }

    // the whole purpose of this is so that each tooltip has the same order >_<
    public static void sortEntries() {
        Comparator<ToolMaterial> comparator = new Comparator<ToolMaterial>() {
            @Override
            public int compare(ToolMaterial m1, ToolMaterial m2) {
                Integer id1 = null;
                Integer id2 = null;
                for (Map.Entry<Integer, ToolMaterial> entry : TConstructRegistry.toolMaterials.entrySet()) {
                    if (entry.getValue() == m1) id1 = entry.getKey();
                    if (entry.getValue() == m2) id2 = entry.getKey();
                }

                if (id1 == null || id2 == null) return 0;

                return id1 - id2;
            }
        };

        for (List<ToolMaterial> mats : patternMaterialLookup.values()) Collections.sort(mats, comparator);
    }

    // the tool parts
    public static final String[] patternNames = new String[] {
        "ingot", //  0
        "rod", //  1
        "pickaxe", //  2
        "shovel", //  3
        "axe", //  4
        "swordblade", //  5
        "largeguard", //  6
        "mediumguard", //  7
        "crossbar", //  8
        "binding", //  9
        "frypan", // 10
        "sign", // 11
        "knifeblade", // 12
        "chisel", // 13
        "largerod", // 14
        "toughbinding", // 15
        "largeplate", // 16
        "broadaxe", // 17
        "scythe", // 18
        "excavator", // 19
        "largeblade", // 20
        "hammerhead", // 21
        "fullguard", // 22
        "bowstring", // 23
        "fletching", // 24
        "arrowhead" // 25
    };

    public static final String[] patternNamesWeaponry = new String[] {
        "shuriken", //  0
        "crossbowlimb", //  1
        "crossbowbody", //  2
        "bowlimb" //  3
    };

    public static final String[] defaultRestrictions = new String[] {
                // removed because it confused people.
            };

    public static final String[] defaultAllowed = new String[] {
        // Wood:
        "Wood:rod",
        "Wood:crossbar",
        "Wood:binding",
        "Wood:sign",
        "Wood:bowlimb",
        "Wood:crossbowlimb",
        "Wood:crossbowbody",

        // Flint:
        "Flint:pickaxe",
        "Flint:shovel",
        "Flint:axe",
        "Flint:knifeblade",
        "Flint:arrowhead",
        "Flint:shuriken",

        // Bone
        "Bone:rod",
        "Bone:shovel",
        "Bone:axe",
        "Bone:crossbar",
        "Bone:knifeblade",
        "Bone:arrowhead",
        "Bone:bowlimb",
        "Bone:crossbowlimb",
        "Bone:crossbowbody",

        // Cactus
        "Cactus:rod",
        "Cactus:binding",
        "Cactus:knifeblade",
        "Cactus:shuriken",

        // Paper:
        "Paper:rod",
        "Paper:binding",

        // Slime:
        "Slime:rod",
        "Slime:sign",
        "Slime:binding",
        "Slime:bowlimb",
        "Slime:crossbowlimb",
        "Slime:crossbowbody",

        // BlueSlime
        "BlueSlime:rod",
        "BlueSlime:binding",
        "BlueSlime:bowlimb",
        "BlueSlime:crossbowlimb",
        "BlueSlime:crossbowbody",

        // Netherrack
        "Netherrack:rod",
        "Netherrack:pickaxe",
        "Netherrack:shovel",
        "Netherrack:axe",
        "Netherrack:largeguard",
        "Netherrack:mediumguard",
        "Netherrack:crossbar",
        "Netherrack:binding",
        "Netherrack:sign",
        "Netherrack:largerod",
        "Netherrack:toughbinding",
        "Netherrack:largeplate",
        "Netherrack:broadaxe",
        "Netherrack:scythe",
        "Netherrack:excavator",
        "Netherrack:fullguard",
        "Netherrack:arrowhead",
        "Netherrack:crossbowbody",

        // Obsidian
        "Obsidian:rod",
        "Obsidian:pickaxe",
        "Obsidian:shovel",
        "Obsidian:axe",
        "Obsidian:largeguard",
        "Obsidian:mediumguard",
        "Obsidian:crossbar",
        "Obsidian:binding",
        "Obsidian:sign",
        "Obsidian:largerod",
        "Obsidian:toughbinding",
        "Obsidian:largeplate",
        "Obsidian:broadaxe",
        "Obsidian:scythe",
        "Obsidian:excavator",
        "Obsidian:hammerhead",
        "Obsidian:fullguard",
        "Obsidian:arrowhead",
        "Obsidian:crossbowbody",
        "Obsidian:shuriken"
    };
}
