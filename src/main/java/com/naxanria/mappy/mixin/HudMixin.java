package com.naxanria.mappy.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.naxanria.mappy.map.MapGUI;
import com.naxanria.mappy.map.waypoint.WayPointRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class HudMixin
{
  @Inject(at = @At("RETURN"), method = "draw(F)V")
  public void draw(float delta, CallbackInfo info)
  {
    MapGUI map = MapGUI.instance;
    if (map != null)
    {
      map.draw();
    }
//
//    GlStateManager.disableDepthTest();
//    WayPointRenderer.render(delta);
//    GlStateManager.enableDepthTest();
  }
}
