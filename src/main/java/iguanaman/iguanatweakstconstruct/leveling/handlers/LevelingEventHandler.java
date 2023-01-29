package iguanaman.iguanatweakstconstruct.leveling.handlers;

import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.UseHoeEvent;

import tconstruct.items.tools.Chisel;
import tconstruct.items.tools.Hammer;
import tconstruct.items.tools.Pickaxe;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.event.ToolCraftEvent;
import tconstruct.library.tools.ToolCore;
import tconstruct.library.weaponry.ProjectileWeapon;
import tconstruct.weaponry.entity.ShurikenEntity;
import tconstruct.weaponry.weapons.Shuriken;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import iguanaman.iguanatweakstconstruct.leveling.LevelingLogic;
import iguanaman.iguanatweakstconstruct.leveling.LevelingTooltips;
import iguanaman.iguanatweakstconstruct.leveling.RandomBonuses;
import iguanaman.iguanatweakstconstruct.reference.Config;

public class LevelingEventHandler {

    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        // only player caused damage
        if (!(event.source.damageType.equals("player") || event.source.damageType.equals("arrow"))) return;

        // only players
        if (!(event.source.getEntity() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.source.getEntity();
        // but no fake players
        if (player instanceof FakePlayer) return;

        ItemStack stack = player.getCurrentEquippedItem();
        if (event.source.getSourceOfDamage() instanceof ShurikenEntity) {
            if (stack == null || !(stack.getItem() instanceof Shuriken)) {
                if (player.inventory.currentItem == 0) stack = player.inventory.getStackInSlot(8);
                else stack = player.inventory.getStackInSlot(player.inventory.currentItem + 1);
            }

            if (stack != null && !(stack.getItem() instanceof Shuriken)) stack = null;
        }

        if (stack == null || !stack.hasTagCompound()) return;

        if (stack.getItem() == null || !(stack.getItem() instanceof ToolCore)) return;

        int xp = 0;
        // is a weapon?
        if (Arrays.asList(((ToolCore) stack.getItem()).getTraits()).contains("weapon")) xp = Math.round(event.ammount);
        else xp = Math.round((event.ammount - 0.1f) / 2);

        // reduce xp for hitting poor animals
        if (event.entityLiving instanceof EntityAnimal) xp = Math.max(1, xp / 2);

        // dead stuff gives little xp
        boolean cheatyXP = false;
        if (!event.entityLiving.isEntityAlive()) {
            xp = Math.max(1, Math.round(xp / 4f));
            cheatyXP = true;
        }

        ItemStack ammo = null;
        // projectile weapons also get xp on their ammo!
        if (stack.getItem() instanceof ProjectileWeapon && event.source.damageType.equals("arrow")) {
            ammo = ((ProjectileWeapon) stack.getItem()).searchForAmmo(player, stack);
            if (ammo != null && !(ammo.getItem() instanceof ToolCore)) ammo = null;
        }

        // projectile weapons and ammo only get xp when they're shot
        if (!event.source.damageType.equals("arrow")) {
            if (stack.getItem() instanceof ProjectileWeapon) return;
            if (Arrays.asList(((ToolCore) stack.getItem()).getTraits()).contains("ammo")) return;
        }

        if (xp > 0) {
            for (ItemStack itemstack : new ItemStack[] { stack, ammo }) {
                if (itemstack == null) continue;

                LevelingLogic.addXP(itemstack, player, xp);

                NBTTagCompound tags = itemstack.getTagCompound().getCompoundTag("InfiTool");
                if (cheatyXP) {
                    int cxp = tags.getInteger("CheatyXP");
                    tags.setInteger("CheatyXP", cxp + xp);
                }

                // bonus chance for luck if hitting passive mob
                if (event.entityLiving instanceof EntityAnimal)
                    RandomBonuses.addModifierExtraWeight(RandomBonuses.Modifier.LAPIS, xp + 5, tags);
                // otherwise damage chance
                else RandomBonuses.addModifierExtraWeight(RandomBonuses.Modifier.ATTACK, xp, tags);

                // spiders also increase bane chance
                if (event.entityLiving instanceof EntitySpider)
                    RandomBonuses.addModifierExtraWeight(RandomBonuses.Modifier.BANE, Math.max(1, xp / 2), tags);
                // blazes give fiery chance (yes, blizz gives fiery :P)
                else if (event.entityLiving instanceof EntityBlaze)
                    RandomBonuses.addModifierExtraWeight(RandomBonuses.Modifier.BLAZE, Math.max(1, xp / 2), tags);
                // zombie pigman gives lifesteal
                else if (event.entityLiving instanceof EntityPigZombie)
                    RandomBonuses.addModifierExtraWeight(RandomBonuses.Modifier.LIFESTEAL, Math.max(1, xp / 2), tags);
                // zombie gives smite
                else if (event.entityLiving instanceof EntityZombie)
                    RandomBonuses.addModifierExtraWeight(RandomBonuses.Modifier.SMITE, Math.max(1, xp / 2), tags);
                // wither skeleton gives lifesteal
                else if (event.entityLiving instanceof EntitySkeleton) {
                    if (((EntitySkeleton) event.entityLiving).getSkeletonType() != 0) RandomBonuses
                            .addModifierExtraWeight(RandomBonuses.Modifier.LIFESTEAL, Math.max(1, xp / 2) + 2, tags);
                }
                // enderman gives beheading
                else if (event.entityLiving instanceof EntityEnderman) RandomBonuses
                        .addModifierExtraWeight(RandomBonuses.Modifier.BEHEADING, Math.max(1, xp / 2) + 3, tags);

                // knocking back enemies with spriting gives knockback chance
                if (player.isSprinting())
                    RandomBonuses.addModifierExtraWeight(RandomBonuses.Modifier.KNOCKBACK, xp + 2, tags);
            }
        }

    }

    @SubscribeEvent
    public void onUseHoe(UseHoeEvent event) {
        EntityPlayer player = event.entityPlayer;
        // no fake players
        if (player instanceof FakePlayer) return;

        // can hoe?
        Block block = event.world.getBlock(event.x, event.y, event.z);
        Block blockAbove = event.world.getBlock(event.x, event.y + 1, event.z);
        if (blockAbove.isAir(event.world, event.x, event.y + 1, event.z)
                && (block == Blocks.grass || block == Blocks.dirt)) {
            ItemStack stack = event.current;
            if (stack != null && stack.hasTagCompound() && stack.getItem() instanceof ToolCore)
                LevelingLogic.addXP(stack, player, 1L);
        }
    }

    @SubscribeEvent
    public void onCrafting(PlayerEvent.ItemCraftedEvent event) {
        if (event.crafting == null) return;

        // was a chisel involved?
        ItemStack chisel = null;
        for (int i = 0; i < event.craftMatrix.getSizeInventory(); i++) if (event.craftMatrix.getStackInSlot(i) != null
                && event.craftMatrix.getStackInSlot(i).getItem() instanceof Chisel)
            chisel = event.craftMatrix.getStackInSlot(i);

        // no chisel found
        if (chisel == null) return;

        // we don't check for chisel recipes specifically, since there is no other recipe that requires a chisel
        // and it's very likely that there never will be

        LevelingLogic.addXP(chisel, event.player, 1);
    }

    @SubscribeEvent
    public void onCraftTool(ToolCraftEvent.NormalTool event) {
        // add tags for tool leveling
        NBTTagCompound toolTag = event.toolTag.getCompoundTag("InfiTool");
        LevelingLogic.addLevelingTags(toolTag, event.tool);

        // remove modifiers
        toolTag.setInteger(
                "Modifiers",
                Math.max(toolTag.getInteger("Modifiers") - (3 - Config.toolLevelingExtraModifiers), 0));
    }

    // Display XP of held tool in debug (F3) if config is set
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Text event) {
        if (Config.toolLeveling && Config.showDebugXP) {
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayer player = mc.thePlayer;
            if (!player.isDead && mc.gameSettings.showDebugInfo) {
                ItemStack equipped = player.getCurrentEquippedItem();

                if (equipped != null && equipped.getItem() != null && equipped.getItem() instanceof ToolCore) {
                    NBTTagCompound tags = equipped.getTagCompound().getCompoundTag("InfiTool");

                    int level = tags.getInteger("ToolLevel");
                    int hLevel = tags.hasKey("HarvestLevel") ? hLevel = tags.getInteger("HarvestLevel") : -1;

                    event.left.add("");

                    if (Config.showTooltipXP) {
                        if (level <= 5) event.left.add(LevelingTooltips.getXpToolTip(equipped, null));

                        if (Config.levelingPickaxeBoost)
                            if (hLevel >= TConstructRegistry.getMaterial("Copper").harvestLevel()
                                    && hLevel < TConstructRegistry.getMaterial("Manyullyn").harvestLevel()
                                    && !tags.hasKey("HarvestLevelModified")
                                    && (equipped.getItem() instanceof Pickaxe || equipped.getItem() instanceof Hammer))
                                event.left.add(LevelingTooltips.getBoostXpToolTip(equipped, null));
                    }
                }
            }
        }
    }
}
