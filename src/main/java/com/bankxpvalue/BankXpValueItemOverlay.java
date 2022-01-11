package com.bankxpvalue;

import java.awt.*;

import net.runelite.api.*;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.SkillColor;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.util.ColorUtil;
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
        setPriority(OverlayPriority.HIGHEST);
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

        if (widgetId != WidgetInfo.BANK_ITEM_CONTAINER.getId()){
            return null;
        }

        final int index = menuEntry.getParam0();
        Item item;


        if (null == bank.getItem(index)){
            return null;
        }
        else{
            item = bank.getItem(index);
        }

        final StringBuilder xpValue = new StringBuilder();

        try{
            if (null != data.getItem(item.getId())){
                ItemDataCache.ItemData itemData = data.getItem(item.getId());

                xpValue.append(getColoredSkill(itemData.skill.substring(0, 1).toUpperCase() + itemData.skill.substring(1) + ": "));
                xpValue.append(QuantityFormatter.quantityToStackSize((long)(itemData.xp * item.getQuantity())));
                xpValue.append(" xp (" + QuantityFormatter.formatNumber(itemData.xp) + " ea)");
            }
        }
        catch (Exception e){
            // Hovered over "view tab" label
        }

        if (!xpValue.toString().equals("")) {
            tooltipManager.add(new Tooltip(xpValue.toString()));
        }
        return null;
    }

    // Colors the skill portion of the tooltip
    private String getColoredSkill(String skill){
        switch (skill){
            case "Construction: ":
                return ColorUtil.wrapWithColorTag("Construction: ",
                        SkillColor.CONSTRUCTION.getColor().brighter().brighter());
            case "Cooking: ":
                return ColorUtil.wrapWithColorTag("Cooking: ",
                        SkillColor.COOKING.getColor().brighter().brighter());
            case "Crafting: ":
                return ColorUtil.wrapWithColorTag("Crafting: ",
                        SkillColor.CRAFTING.getColor().brighter().brighter());
            case "Farming: ":
                return ColorUtil.wrapWithColorTag("Farming: ",
                        SkillColor.FARMING.getColor().brighter().brighter());
            case "Firemaking: ":
                return ColorUtil.wrapWithColorTag("Firemaking: ",
                        new Color(255, 119, 0));
            case "Fletching: ":
                return ColorUtil.wrapWithColorTag("Fletching: ",
                        SkillColor.FLETCHING.getColor().brighter().brighter());
            case "Herblore: ":
                return ColorUtil.wrapWithColorTag("Herblore: ",
                        SkillColor.HERBLORE.getColor().brighter().brighter());
            case "Prayer: ":
                return ColorUtil.wrapWithColorTag("Prayer: ",
                        SkillColor.PRAYER.getColor().brighter().brighter());
            case "Smithing: ":
                return ColorUtil.wrapWithColorTag("Smithing: ",
                        SkillColor.SMITHING.getColor().brighter().brighter());
        }
        return skill;
    }
}