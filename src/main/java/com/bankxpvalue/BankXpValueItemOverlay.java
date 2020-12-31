package com.bankxpvalue;

import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.*;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.util.QuantityFormatter;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;

import javax.inject.Inject;

public class BankXpValueItemOverlay extends Overlay {

    private final Client client;
    private final BankXpValueConfig config;
    private final TooltipManager tooltipManager;

    @Inject
    ItemDataCache data;

    @Inject
    BankXpValueItemOverlay(Client client, BankXpValueConfig config, TooltipManager tooltipManager)
    {
        setPosition(OverlayPosition.DYNAMIC);
        this.client = client;
        this.config = config;
        this.tooltipManager = tooltipManager;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.showItemXpTooltips() || client.isMenuOpen()) {
            return null;
        }

        final MenuEntry[] menuEntries = client.getMenuEntries();

        if (menuEntries.length - 1 < 0) {
            return null;
        }

        final ItemContainer bank = client.getItemContainer(InventoryID.BANK);
        final MenuEntry menuEntry = menuEntries[menuEntries.length - 1];
        final int widgetId = menuEntry.getParam1();
        final int groupId = WidgetInfo.TO_GROUP(widgetId);

        if (groupId != WidgetID.BANK_GROUP_ID || widgetId != 786444){
            return null;
        }

        final int index = menuEntry.getParam0();
        final Item item = bank.getItem(index);
        final StringBuilder xpValue = new StringBuilder();

        if (null != data.getItem(item.getId())){
            ItemDataCache.ItemData itemData = data.getItem(item.getId());

            xpValue.append(itemData.skill.substring(0, 1).toUpperCase() + itemData.skill.substring(1));
            xpValue.append(": " + QuantityFormatter.quantityToStackSize((long)(itemData.xp * item.getQuantity())));
            xpValue.append(" xp (" + QuantityFormatter.formatNumber(itemData.xp) + " ea)");
        }

        if (!xpValue.toString().equals("")) {
            tooltipManager.add(new Tooltip(xpValue.toString()));
        }
        return null;
    }
}