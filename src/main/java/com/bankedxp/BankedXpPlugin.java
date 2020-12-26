package com.bankedxp;

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import javax.inject.Inject;
import java.util.Arrays;

@PluginDescriptor(
        name = "Banked XP",
        description = "Shows the total amount of potential XP available from items in your bank",
        tags = {"bank", "xp"},
        loadWhenOutdated = true,
        enabledByDefault = true
)
public class BankedXpPlugin extends Plugin {

    private boolean pluginToggled = false;

    private final ItemDataCache data = new ItemDataCache();

    @Inject
    private Client client;

    @Inject
    private BankedXpOverlay overlay;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private BankedXpConfig config;

    @Provides
    BankedXpConfig provideConfig(ConfigManager configManager){
        return configManager.getConfig(BankedXpConfig.class);
    }

    private ItemContainer itemContainer;

    @Override
    protected void startUp() throws Exception{

    }

    @Override
    protected void shutDown() throws Exception{
        overlayManager.remove(overlay);
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event){
        if (event.getType() != MenuAction.CC_OP.getId() || !event.getOption().equals("Show menu")
                || (event.getActionParam1() >> 16) != WidgetID.BANK_GROUP_ID){
            return;
        }

        MenuEntry[] entries = client.getMenuEntries();
        entries = Arrays.copyOf(entries, entries.length + 1);

        MenuEntry bankedXp = new MenuEntry();
        bankedXp.setOption("Toggle Banked XP");
        bankedXp.setTarget("");
        bankedXp.setType(MenuAction.WIDGET_FOURTH_OPTION.getId() + 2000);
        bankedXp.setIdentifier(event.getIdentifier());
        bankedXp.setParam0(event.getActionParam0());
        bankedXp.setParam1(event.getActionParam1());

        entries[entries.length - 1] = bankedXp;

        client.setMenuEntries(entries);
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event){
        if (event.getWidgetId() != WidgetInfo.BANK_SETTINGS_BUTTON.getId() ||
                !event.getMenuOption().contains("XP")){
            return;
        }

        if (event.getMenuOption().equals("Toggle Banked XP")) {
            if (!pluginToggled){

                itemContainer = client.getItemContainer(InventoryID.BANK);
                if (itemContainer != null){
                    overlay.setXpTotals(data.getTotals(itemContainer.getItems()));
                }

                overlayManager.add(overlay);
            }
            else{
                overlayManager.remove(overlay);
            }
            pluginToggled = !pluginToggled;

            MenuEntry[] entries = client.getMenuEntries();
            MenuEntry entry = new MenuEntry();
            entry.setOption("Hide Potential XP");

            client.setMenuEntries(entries);
        }
    }
}