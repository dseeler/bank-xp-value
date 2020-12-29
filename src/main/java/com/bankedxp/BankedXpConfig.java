package com.bankedxp;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("bankedxp")
public interface BankedXpConfig extends Config{
    @ConfigItem(
            position = 1,
            keyName = "tutorial",
            name = "Show tutorial",
            description = "Shows a tutorial overlay that explains how to use the plugin"
    )
    default boolean showTutorial(){
        return true;
    }

    @ConfigItem(
            keyName = "tutorial",
            name = "",
            description = ""
    )
    void setTutorial(boolean tutorial);

    @ConfigItem(
            position = 2,
            keyName = "includeSeedVault",
            name = "Include seed vault",
            description = "Includes items in your seed vault"
    )
    default boolean includeSeedVault(){
        return true;
    }
}
