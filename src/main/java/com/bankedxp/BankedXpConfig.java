package com.bankedxp;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("bankedxp")
public interface BankedXpConfig extends Config{
    @ConfigItem(
            position = 1,
            keyName = "bankedXp",
            name = "Banked XP",
            description = "Shows the total amount of potential XP available from items in your bank"
    )
    default boolean bankedXp(){
        return true;
    }
}
