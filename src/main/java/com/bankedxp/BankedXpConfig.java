package com.bankedxp;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("bankedxp")
public interface BankedXpConfig extends Config{
    @ConfigItem(
            position = 1,
            keyName = "includeSeedVault",
            name = "Include seed vault",
            description = "Includes items in your seed vault"
    )
    default boolean includeSeedVault(){
        return true;
    }

    @ConfigItem(
            position = 3,
            keyName = "dynamicWindow",
            name = "Make window movable",
            description = "Skill Tooltips show text instead of images"
    )
    default boolean dynamicWindow(){
        return false;
    }
}
