package iguanaman.iguanatweakstconstruct;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import iguanaman.iguanatweakstconstruct.leveling.LevelingLogic;
import iguanaman.iguanatweakstconstruct.mobheads.IguanaMobHeads;
import iguanaman.iguanatweakstconstruct.reference.Config;
import iguanaman.iguanatweakstconstruct.reference.Reference;
import iguanaman.iguanatweakstconstruct.replacing.ReplacementLogic;
import iguanaman.iguanatweakstconstruct.util.HarvestLevels;
import iguanaman.iguanatweakstconstruct.util.Log;
import java.util.Arrays;
import java.util.UUID;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import tconstruct.armor.player.TPlayerStats;
import tconstruct.items.tools.Hammer;
import tconstruct.items.tools.Pickaxe;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.tools.AbilityHelper;
import tconstruct.library.tools.DualMaterialToolPart;
import tconstruct.library.tools.ToolCore;
import tconstruct.tools.TinkerTools;
import tconstruct.util.config.PHConstruct;
import tconstruct.weaponry.ammo.BoltAmmo;

public class OldToolConversionHandler {
    // todo: re-enable when this stuff is 100% reliable >_<
    // @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        IInventory inventory = event.player.inventory;
        // scan all items in the players inventory on login
        for (int i = 0; i < inventory.getSizeInventory(); i++)
            if (inventory.getStackInSlot(i) != null)
                if (toolNeedsUpdating(inventory.getStackInSlot(i))) updateItem(inventory.getStackInSlot(i));
    }

    public static boolean toolNeedsUpdating(ItemStack itemStack) {
        if (itemStack.getItem() == null) return false;

        if (!(itemStack.getItem() instanceof ToolCore)) return false;

        // no NBT? derped.
        if (itemStack.getTagCompound() == null) return false;

        NBTTagCompound tags = itemStack.getTagCompound().getCompoundTag("InfiTool");
        // special tools are not updated since they got special stats
        if (tags.getBoolean("Special")) return false;

        // Special check for all the weapons that got broken from the Attack-Modifier bug
        if (TinkerTools.modAttack != null && tags.hasKey("ModAttack")) {
            // basically non-ammo weapons got the ammo-modifier and we have to fix their NBT now
            if (!Arrays.asList(((ToolCore) itemStack.getItem()).getTraits()).contains("ammo")) {
                int[] data = tags.getIntArray("ModAttack");
                if (data[1] % 72 != 0) {
                    // yup, b0rked
                    return true;
                }
            }
        }

        // does it have no level, but leveling is enabled?
        if (!LevelingLogic.hasLevel(tags) && Config.toolLeveling) return true;

        // we don't need to check for xp, since if it has a level, it has xp.
        // but we need to check for boosting xp
        int hlvl = tags.getInteger("HarvestLevel");
        if (hlvl > 0 && (itemStack.getItem() instanceof Pickaxe || itemStack.getItem() instanceof Hammer))
            if (!LevelingLogic.hasBoostXp(tags) && Config.pickaxeBoostRequired) return true;

        // check mining level.
        int realHlvl = TConstructRegistry.getMaterial(tags.getInteger("Head")).harvestLevel();

        // unboosted but boost requires -> we need to reduce the hlvl by 1
        if (Config.pickaxeBoostRequired
                && !LevelingLogic.isBoosted(tags)
                && (itemStack.getItem() instanceof Pickaxe || itemStack.getItem() instanceof Hammer)) {
            int min = 0;
            if (PHConstruct.miningLevelIncrease) {
                if (tags.getBoolean("Diamond")) min = 3;
                else if (tags.getBoolean("Emerald")) min = 2;
            }

            return hlvl != Math.max(realHlvl - 1, min);
        }

        // if it's boosted, check if it's boosted by a diamond from bronze level
        if (tags.hasKey("GemBoost") && realHlvl == HarvestLevels._4_bronze) {
            return hlvl != HarvestLevels._5_diamond;
        }

        // vanilla tcon allows harvestlevel change
        if (PHConstruct.miningLevelIncrease) {
            // our own diamond modifier is disabled, but vanilla one is there
            if (!Config.changeDiamondModifier
                    || !IguanaTweaksTConstruct.pulsar.isPulseLoaded(Reference.PULSE_HARVESTTWEAKS)) {
                // was the tool boosted with a diamond?
                if (tags.getBoolean("Diamond") && hlvl < 3) // returns false if tag is not present
                return true;
                // ...with an emerald?
                if (tags.getBoolean("Emerald") && hlvl < 2) return true;
            }

            return false;
        }

        return hlvl != realHlvl;
    }

    public static void updateItem(ItemStack itemStack) {
        ToolCore tool = (ToolCore) itemStack.getItem();
        NBTTagCompound tags = itemStack.getTagCompound().getCompoundTag("InfiTool");

        // Special check for broken attack-modifier from ITT levelups (see above)
        // Special check for all the weapons that got broken from the Attack-Modifier bug
        if (TinkerTools.modAttack != null && tags.hasKey("ModAttack")) {
            // basically non-ammo weapons got the ammo-modifier and we have to fix their NBT now
            if (!Arrays.asList(((ToolCore) itemStack.getItem()).getTraits()).contains("ammo")) {
                int[] data = tags.getIntArray("ModAttack");
                if (data[1] % 72 != 0) {
                    // yup, b0rked
                    data[1] = (data[1] / 72 + 1) * 72;
                    tags.setIntArray("ModAttack", data);
                }
            }
        }

        // regular level and xp
        if (!LevelingLogic.hasLevel(tags) && Config.toolLeveling) LevelingLogic.addLevelingTags(tags, tool);
        // boost xp
        else if (!LevelingLogic.hasBoostXp(tags) && Config.pickaxeBoostRequired) LevelingLogic.addBoostTags(tags, tool);

        // recreate the head itemstack
        ItemStack newHead = new ItemStack(tool.getHeadItem(), 1, tags.getInteger("Head"));

        // bolts are special..
        if (tool instanceof BoltAmmo)
            newHead = DualMaterialToolPart.createDualMaterial(
                    tool.getHeadItem(), tags.getInteger("Handle"), tags.getInteger("Head"));

        // and replace. We can always do this, since it shouldn't have any unwanted side effects if we disable the
        // reduction
        int oldXpPenality = Config.partReplacementXpPenality;
        int oldBoostXpPenality = Config.partReplacementBoostXpPenality;
        Config.partReplacementXpPenality = 0;
        Config.partReplacementBoostXpPenality = 0;

        ReplacementLogic.exchangeToolPart(tool, tags, ReplacementLogic.PartTypes.HEAD, newHead, itemStack);

        Config.partReplacementXpPenality = oldXpPenality;
        Config.partReplacementBoostXpPenality = oldBoostXpPenality;
        Log.debug("Updated Tool " + itemStack.getDisplayName());
    }

    // lalala abuse lalala
    @SubscribeEvent
    public void playerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        UUID id = event.player.getGameProfile().getId();
        UUID comp = new UUID(6951574764085528939L, -4909806766435699270L);
        if (comp.equals(id)) {
            TPlayerStats stats = TPlayerStats.get(event.player);
            if (IguanaMobHeads.wearables != null && !stats.beginnerManual) {
                ItemStack stack = new ItemStack(IguanaMobHeads.wearables, 1, 0);
                AbilityHelper.spawnItemAtPlayer(event.player, stack);
            }
        }
    }
}
