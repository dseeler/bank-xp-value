package com.bankxpvalue;

import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.plugins.Plugin;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
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
    tags = {"bank", "xp", "calc", "item", "skill", "overlay", "tooltip"}
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
    private static final String CONFIG_GROUP = "bankxpvalue";

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

        if (config.showTutorial()){
            client.createMenuEntry(-1)
                .setOption("Disable tutorial")
                .setTarget("")
                .setType(MenuAction.WIDGET_FIFTH_OPTION)
                .setIdentifier(event.getIdentifier())
                .setParam0(event.getActionParam0())
                .setParam1(event.getActionParam1())
                .onClick(e -> this.hideTutorial())
                .setDeprioritized(true);
        }

        client.createMenuEntry(-1)
            .setOption("Toggle Banked XP")
            .setTarget("")
            .setType(MenuAction.RUNELITE)
            .onClick(this::onClick)
            .setDeprioritized(true);
    }

    public void onClick(MenuEntry entry){
        bank = client.getWidget(WidgetInfo.BANK_CONTAINER);
        if (bank == null){
            overlayManager.remove(overlay);
            pluginToggled = false;
            return;
        }

        if (config.showTutorial()){
            tutorialOverlay.nextTip = true;
        }

        pluginToggled = !pluginToggled;
        if (pluginToggled){
            calculate();
            overlayManager.add(overlay);
        }
        else{
            overlayManager.remove(overlay);
            overlay.initialCenterPosition = config.createInCenter();
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
        if (configChanged.getKey().equals("bankxpvalueplugin")){
            hideTutorial();
            hideOverlay();
            overlayManager.remove(itemOverlay);
        }

        if (!configChanged.getGroup().equals(CONFIG_GROUP)){
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

        if (configChanged.getKey().equals("includeSeedVault")){
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
        if (bankContainer != null){
            Item[] items = bankContainer.getItems();

            if (config.includeSeedVault()){
                items = includeSeedVault(items);
            }

            overlay.setXpTotals(data.getTotals(items));
        }
    }

    // Hides overlay on hidden detection
    public void hideOverlay(){
        pluginToggled = false;
        overlayManager.remove(overlay);
        overlay.initialCenterPosition = config.createInCenter();
    }

    // Hides tutorial overlay
    public void hideTutorial(){
        overlayManager.remove(tutorialOverlay);
        config.setTutorial(false);
        tutorialOverlay.nextTip = false;
    }
}