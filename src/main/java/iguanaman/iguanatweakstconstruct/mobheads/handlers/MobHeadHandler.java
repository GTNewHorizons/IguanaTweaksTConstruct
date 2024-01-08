package iguanaman.iguanatweakstconstruct.mobheads.handlers;

import java.util.Iterator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDropsEvent;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import iguanaman.iguanatweakstconstruct.IguanaTweaksTConstruct;
import iguanaman.iguanatweakstconstruct.mobheads.IguanaMobHeads;
import iguanaman.iguanatweakstconstruct.mobheads.items.IguanaSkull;
import iguanaman.iguanatweakstconstruct.reference.Config;
import tconstruct.library.tools.ToolCore;
import tconstruct.tools.TinkerTools;

public class MobHeadHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void LivingDrops(LivingDropsEvent event) {
        // remove regular skull drops (because we modify its drop chance)
        Iterator<EntityItem> i = event.drops.iterator();
        while (i.hasNext()) {
            EntityItem eitem = i.next();

            if (eitem != null) if (eitem.getEntityItem() != null) {
                ItemStack item = eitem.getEntityItem();
                if (item.getItem() == Items.skull && item.getItemDamage() != 3) i.remove();
            }
        }

        // add our own drops if the damage source was a player (and no fake player)
        Entity entity = event.source.getEntity();
        if (entity == null) return;
        if (!(entity instanceof EntityPlayer) || entity instanceof FakePlayer) return;

        // how much beheading chance do we have?
        EntityPlayer player = (EntityPlayer) event.source.getEntity();
        ItemStack stack = player.getCurrentEquippedItem();
        int beheading = 0;
        if (stack != null && stack.hasTagCompound() && stack.getItem() instanceof ToolCore) {
            beheading = stack.getTagCompound().getCompoundTag("InfiTool").getInteger("Beheading");
            if (stack.getItem() == TinkerTools.cleaver) beheading += 2;
        }
        // roll the dice
        if (IguanaTweaksTConstruct.random.nextInt(100)
                > beheading * Config.beheadingHeadDropChance + Config.baseHeadDropChance)
            return;

        Item skullItem = null;
        int skullId = -1;

        Entity mob = event.entityLiving;
        // skelly/witherskelly
        if (mob instanceof EntitySkeleton) {
            skullItem = Items.skull;
            skullId = ((EntitySkeleton) event.entityLiving).getSkeletonType();
        } else if (mob instanceof EntityPigZombie) {
            skullItem = IguanaMobHeads.skullItem;
            skullId = IguanaSkull.META_PIGZOMBIE;
        } else if (mob instanceof EntityZombie) {
            skullItem = Items.skull;
            skullId = 2;
        } else if (mob instanceof EntityCreeper) {
            skullItem = Items.skull;
            skullId = 4;
        } else if (mob instanceof EntityEnderman) {
            skullItem = IguanaMobHeads.skullItem;
            skullId = IguanaSkull.META_ENDERMAN;

            // sometimes, very very rarely, you'll only get the jaw :D
            if (IguanaTweaksTConstruct.random.nextInt(1000) == 0) {
                skullItem = IguanaMobHeads.wearables;
                skullId = 2;
            }
        } else if (mob instanceof EntityBlaze) {
            skullItem = IguanaMobHeads.skullItem;
            skullId = IguanaSkull.META_BLAZE;
        }
        // mod support
        else {
            String mobName = EntityList.getEntityString(mob);
            // thermal expansion
            if ("Blizz".equals(mobName)) {
                skullItem = IguanaMobHeads.skullItem;
                skullId = IguanaSkull.META_BLIZZ;
            }
        }

        // no skull found?
        if (skullItem == null) return;

        // drop it like it's hot
        EntityItem entityitem = new EntityItem(
                mob.worldObj,
                mob.posX,
                mob.posY,
                mob.posZ,
                new ItemStack(skullItem, 1, skullId));
        entityitem.delayBeforeCanPickup = 10;
        event.drops.add(entityitem);
    }
}
