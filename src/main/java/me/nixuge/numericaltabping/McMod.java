package me.nixuge.numericaltabping;

import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.fml.common.Mod;

@Mod(
        modid = McMod.MOD_ID,
        name = McMod.NAME,
        version = McMod.VERSION,
        clientSideOnly = true
)

@Setter
public class McMod {
    public static final String MOD_ID = "numericaltabping";
    public static final String NAME = "Numerical Tab Ping";
    public static final String VERSION = "1.0.0 (1.8.9)";
    @Getter
    @Mod.Instance(value = McMod.MOD_ID)
    private static McMod instance;
}
