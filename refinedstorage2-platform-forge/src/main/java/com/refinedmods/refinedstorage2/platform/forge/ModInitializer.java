package com.refinedmods.refinedstorage2.platform.forge;

import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponent;
import com.refinedmods.refinedstorage2.platform.abstractions.PlatformAbstractions;
import com.refinedmods.refinedstorage2.platform.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil;

import net.minecraftforge.fml.common.Mod;

@Mod(IdentifierUtil.MOD_ID)
public class ModInitializer {
    public ModInitializer() {
        System.out.println("Hello Forge!!!");
        System.out.println(GridInsertMode.class.getName());
        System.out.println(NetworkComponent.class.getName());
        System.out.println(PlatformAbstractions.class.getName());
        System.out.println(Rs2PlatformApiFacade.class.getName());
    }
}
