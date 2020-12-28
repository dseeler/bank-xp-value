package com.bankedxp;

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import javax.inject.Inject;
import java.util.Arrays;

@PluginDescriptor(
        name = "Banked XP",
        description = "Shows your banked XP in an overlay",
        tags = {"bank", "xp"},
        loadWhenOutdated = true,
        enabledByDefault = true
)
public class BankedXpPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private BankedXpOverlay overlay;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private BankedXpConfig config;

    @Inject
    private ItemDataCache data;

    private static final String CONFIG_GROUP = "bankedxp";
    private Widget bank;
    private ItemContainer bankContainer;
    private ItemContainer seedVaultContainer;
    private static boolean pluginToggled = false;

    @Provides
    BankedXpConfig provideConfig(ConfigManager configManager){
        return configManager.getConfig(BankedXpConfig.class);
    }

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
        bank = client.getWidget(WidgetInfo.BANK_CONTAINER);
        if (bank == null){
            overlayManager.remove(overlay);
            pluginToggled = false;
            return;
        }

        if (event.getWidgetId() != WidgetInfo.BANK_SETTINGS_BUTTON.getId() ||
                !event.getMenuOption().contains("XP")){
            return;
        }

        if (event.getMenuOption().equals("Toggle Banked XP")) {
            if (!pluginToggled){
                calculate();
                overlayManager.add(overlay);
            }
            else{
                overlayManager.remove(overlay);
            }
            pluginToggled = !pluginToggled;

            MenuEntry[] entries = client.getMenuEntries();
            client.setMenuEntries(entries);
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event){
        if (event.getContainerId() == InventoryID.SEED_VAULT.getId() && config.includeSeedVault()){
            seedVaultContainer = client.getItemContainer(InventoryID.SEED_VAULT);
        }
        else if (bank != null && event.getContainerId() == InventoryID.BANK.getId()){
            calculate();
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged configChanged){
        if (configChanged.getKey().equals("bankedxpplugin")){
            pluginToggled = false;
        }
        if (configChanged.getGroup().equals(CONFIG_GROUP) &&
                configChanged.getKey().equals("includeSeedVault")){
            calculate();
        }
    }

    private void calculate(){
        bankContainer = client.getItemContainer(InventoryID.BANK);
        Item items[] = bankContainer.getItems();

        if (config.includeSeedVault() && items != null){
            items = includeSeedVault(items);
        }

        overlay.setXpTotals(data.getTotals(items));
    }

    private Item[] includeSeedVault(Item[] items){
        ItemContainer container = seedVaultContainer;

        if (container == null || container.size() == 0){
            return items;
        }

        Item[] moreItems = Arrays.copyOf(items, items.length + container.size());

        int count = 0;
        for (int i = items.length; i < moreItems.length; i++){
            moreItems[i] = container.getItem(count);
            count++;
        }
        return moreItems;
    }

    public void hideOverlay(){
        pluginToggled = false;
        overlayManager.remove(overlay);
    }
}