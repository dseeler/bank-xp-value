package com.bankxpvalue;

import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.plugins.Plugin;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.eventbus.Subscribe;
import com.google.inject.Provides;
import javax.inject.Inject;
import java.util.Arrays;

@PluginDescriptor(
        name = "Bank XP Value",
        description = "All-in-one banked xp viewer + item xp tooltips",
        tags = {"bank", "xp", "calc", "item", "skill", "overlay", "tooltip"},
        loadWhenOutdated = true,
        enabledByDefault = true
)
public class BankXpValuePlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private BankXpValueOverlay overlay;

    @Inject
    private BankXpValueTutorialOverlay tutorialOverlay;

    @Inject
    private BankXpValueItemOverlay itemOverlay;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private BankXpValueConfig config;

    @Inject
    private ItemDataCache data;

    private Widget bank;
    private ItemContainer bankContainer;
    private ItemContainer seedVaultContainer;
    private boolean pluginToggled = false;
    private static final String CONFIG_GROUP = "bank-xp-value";

    @Provides
    BankXpValueConfig provideConfig(ConfigManager configManager){
        return configManager.getConfig(BankXpValueConfig.class);
    }

    @Override
    protected void startUp() throws Exception{
        if (config.showTutorial()){
            overlayManager.add(tutorialOverlay);
        }
        if (config.showItemXpTooltips()){
            overlayManager.add(itemOverlay);
        }
    }

    @Override
    protected void shutDown() throws Exception{
        overlayManager.remove(overlay);
        overlayManager.remove(tutorialOverlay);
        overlayManager.remove(itemOverlay);
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event){
        if (event.getType() != MenuAction.CC_OP.getId() || !event.getOption().equals("Show menu")
                || (event.getActionParam1() >> 16) != WidgetID.BANK_GROUP_ID){
            return;
        }

        MenuEntry[] entries = client.getMenuEntries();

        if (config.showTutorial()){
            entries = Arrays.copyOf(entries, entries.length + 1);
            MenuEntry tutorial = new MenuEntry();
            tutorial.setOption("Disable tutorial");
            tutorial.setTarget("");
            tutorial.setType(MenuAction.WIDGET_FIFTH_OPTION.getId() + 2000);
            tutorial.setIdentifier(event.getIdentifier());
            tutorial.setParam0(event.getActionParam0());
            tutorial.setParam1(event.getActionParam1());
            entries[entries.length - 1] = tutorial;
        }

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
                (!event.getMenuOption().contains("Banked XP") && !event.getMenuOption().contains("tutorial"))){
            return;
        }

        if (event.getMenuOption().equals("Disable tutorial")){
            hideTutorial();
            return;
        }

        if (event.getMenuOption().equals("Toggle Banked XP")) {
            if (config.showTutorial()){
                tutorialOverlay.nextTip = true;
            }

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
        if (!configChanged.getGroup().equals("bankxpvalue")){
            return;
        }

        if (config.showTutorial()){
            hideOverlay();
            config.setTutorial(true);
            overlayManager.add(tutorialOverlay);
        }
        else{
            hideTutorial();
        }

        if (configChanged.getKey().equals("bankxpvalueplugin")){
            pluginToggled = false;
            hideTutorial();
            hideOverlay();
            overlayManager.remove(itemOverlay);
        }
        if (configChanged.getGroup().equals(CONFIG_GROUP) &&
                configChanged.getKey().equals("includeSeedVault")){
            calculate();
        }
    }

    // Includes seed vault items if config set
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

    // Sends bank data to calculate totals
    private void calculate(){
        bankContainer = client.getItemContainer(InventoryID.BANK);
        Item items[] = bankContainer.getItems();

        if (config.includeSeedVault() && items != null){
            items = includeSeedVault(items);
        }

        overlay.setXpTotals(data.getTotals(items));
    }

    // Hides overlay on hidden detection
    public void hideOverlay(){
        pluginToggled = false;
        overlayManager.remove(overlay);
    }

    // Hides tutorial overlay
    public void hideTutorial(){
        overlayManager.remove(tutorialOverlay);
        config.setTutorial(false);
        tutorialOverlay.nextTip = false;
    }
}