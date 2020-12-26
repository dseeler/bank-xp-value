package com.bankedxp;

import net.runelite.api.*;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.*;
import javax.inject.Inject;
import java.awt.*;
import java.awt.Point;

public class BankedXpOverlay extends OverlayPanel {

    private final Client client;
    private final ItemManager itemManager;
    private final BankedXpConfig config;
    private final static String[] xpTotals = new String[8];

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
        panelComponent.getChildren().clear();

        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Potential XP Available")
                .color(Color.GREEN)
                .build());

        panelComponent.setPreferredSize(new Dimension(
                graphics.getFontMetrics().stringWidth("Total Potential XP Available") + 50,
                0));

        panelComponent.getChildren().add(LineComponent.builder().left("Prayer: ").right(xpTotals[0]).build());
        panelComponent.getChildren().add(LineComponent.builder().left("Construction: ").right(xpTotals[1]).build());
        panelComponent.getChildren().add(LineComponent.builder().left("Crafting: ").right(xpTotals[2]).build());
        panelComponent.getChildren().add(LineComponent.builder().left("Fletching: ").right(xpTotals[3]).build());
        panelComponent.getChildren().add(LineComponent.builder().left("Smithing: ").right(xpTotals[4]).build());
        panelComponent.getChildren().add(LineComponent.builder().left("Cooking: ").right(xpTotals[5]).build());
        panelComponent.getChildren().add(LineComponent.builder().left("Farming: ").right(xpTotals[6]).build());
        panelComponent.getChildren().add(LineComponent.builder().left("Total: ").right(xpTotals[7]).build());



        return super.render(graphics);
    }

    public void setXpTotals(double[] totals){
        for (int i = 0; i < totals.length; i++){
            if ((int)totals[i] == 0){
                xpTotals[i] = "None";
            }
            else{
                xpTotals[i] = String.format("%,d", (int)totals[i]) + "xp";
            }
        }
    }
}
