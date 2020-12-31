package com.bankxpvalue;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("bankxpvalue")
public interface BankXpValueConfig extends Config{
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
            keyName = "itemXpTooltips",
            name = "Show item xp tooltips",
            description = "Hovering over items in your bank will display a tooltip containing the total xp of the stack"
    )
    default boolean showItemXpTooltips(){
        return true;
    }

    @ConfigItem(
            position = 3,
            keyName = "includeSeedVault",
            name = "Include seed vault",
            description = "Includes items in your seed vault"
    )
    default boolean includeSeedVault(){
        return true;
    }
}
