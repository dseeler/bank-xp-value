package com.bankedxp;

import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.SkillColor;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.*;
import javax.inject.Inject;
import java.awt.*;
import java.awt.Point;

public class BankedXpOverlay extends OverlayPanel {

    private final Client client;
    private final ItemManager itemManager;
    private final BankedXpConfig config;
    private final static String[] xpTotals = new String[10];

    @Inject
    private BankedXpOverlay(Client client, ItemManager itemManager, BankedXpConfig config){

        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(OverlayPriority.HIGHEST);
        setPosition(OverlayPosition.TOP_CENTER);

        panelComponent.setPreferredLocation(new Point(0, 85));
        panelComponent.setBackgroundColor(new Color(51, 51, 51, 245));

        this.client = client;
        this.itemManager = itemManager;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics){
        Widget bank = client.getWidget(WidgetInfo.BANK_CONTAINER);

        panelComponent.getChildren().clear();

        if (bank == null){
            return null;
        }

        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Potential XP Available")
                .build());

        panelComponent.setPreferredSize(new Dimension(
                graphics.getFontMetrics().stringWidth("Total Potential XP Available") + 50, 0));

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Construction: ")
                .leftColor(SkillColor.CONSTRUCTION.getColor().brighter().brighter())
                .right("" + xpTotals[0]).build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Cooking: ")
                .leftColor(SkillColor.COOKING.getColor().brighter().brighter())
                .right("" + xpTotals[1]).build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Crafting: ")
                .leftColor(SkillColor.CRAFTING.getColor().brighter().brighter())
                .right("" + xpTotals[2]).build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Farming: ")
                .leftColor(SkillColor.FARMING.getColor().brighter().brighter())
                .right("" + xpTotals[3]).build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Firemaking: ")
                .leftColor(new Color(255, 102, 0))
                .right("" + xpTotals[4]).build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Fletching: ")
                .leftColor(SkillColor.FLETCHING.getColor().brighter().brighter())
                .right("" + xpTotals[5]).build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Herblore: ")
                .leftColor(SkillColor.HERBLORE.getColor().brighter().brighter())
                .right("" + xpTotals[6]).build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Prayer: ")
                .leftColor(SkillColor.PRAYER.getColor().brighter().brighter())
                .right("" + xpTotals[7]).build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Smithing: ")
                .leftColor(SkillColor.SMITHING.getColor().brighter().brighter())
                .right("" + xpTotals[8]).build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Total: ")
                .right("" + xpTotals[9]).build());

        return super.render(graphics);
    }

    public void setXpTotals(double[] totals){
        for (int i = 0; i < totals.length; i++){
            if ((int)totals[i] == 0){
                xpTotals[i] = "None";
            }
            else{
                xpTotals[i] = String.format("%,d", (int)Math.ceil(totals[i]));
            }
        }
    }
}
