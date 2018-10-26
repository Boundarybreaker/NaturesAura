package de.ellpeck.naturesaura.compat;

import de.ellpeck.naturesaura.compat.patchouli.PatchouliCompat;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;

public final class Compat {

    public static boolean baubles;

    public static void init() {
        baubles = Loader.isModLoaded("baubles");
        if (baubles) {
            MinecraftForge.EVENT_BUS.register(new BaublesCompat());
        }
    }

    public static void initClient(){
        PatchouliCompat.initClient();
    }
}