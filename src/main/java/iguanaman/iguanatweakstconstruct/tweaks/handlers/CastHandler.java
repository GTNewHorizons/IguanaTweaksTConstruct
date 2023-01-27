package iguanaman.iguanatweakstconstruct.tweaks.handlers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import tconstruct.library.event.SmelteryCastedEvent;
import tconstruct.library.util.IPattern;

public class CastHandler {
    @SubscribeEvent
    public void onCasted(SmelteryCastedEvent.CastingTable event) {
        if (event.output.getItem() instanceof IPattern) event.consumeCast = true;
    }
}
