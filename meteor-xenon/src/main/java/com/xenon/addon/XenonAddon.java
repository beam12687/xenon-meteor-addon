package com.xenon.addon;
import com.mojang.logging.LogUtils;
import com.xenon.addon.modules.AmethystESP;
import com.xenon.addon.modules.ActivityDebug;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;
public class XenonAddon extends MeteorAddon {
    public static final Category CATEGORY = new Category("Xenon");
    public static final Logger LOG = LogUtils.getLogger();
    @Override
    public void onInitialize() {
        Modules.get().add(new AmethystESP());
        Modules.get().add(new ActivityDebug());
    }
    @Override
    public String getPackage() { return "com.xenon.addon"; }
}
