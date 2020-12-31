package com.bankxpvalue;

import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.*;

public class BankXpValueTutorialOverlay extends OverlayPanel {

    private final Client client;
    private final BankXpValueConfig config;
    private final BankXpValuePlugin plugin;
    public static boolean nextTip = false;
    private Widget bank;

    @Inject
    private BankXpValueTutorialOverlay(Client client, BankXpValueConfig config, BankXpValuePlugin plugin){
        this.client = client;
        this.config = config;
        this.plugin = plugin;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
    }

    @Override
    public Dimension render(Graphics2D graphics){
        if (!config.showTutorial()){
            return null;
        }

        bank = client.getWidget(WidgetInfo.BANK_CONTAINER);

        if (bank == null || bank.isHidden()){
            return null;
        }

        Widget button = client.getWidget(WidgetInfo.BANK_SETTINGS_BUTTON);
        if (button == null || button.isSelfHidden() || button.getDynamicChildren()[0].getSpriteId() != 195){
            return null;
        }

        Rectangle bounds = button.getBounds();

        // If on tutorial step 1/2
        if (!nextTip){
            graphics.setColor(ColorScheme.BRAND_ORANGE);
            graphics.setStroke(new BasicStroke(2));
            graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

            FontMetrics font = graphics.getFontMetrics();
            int width = font.stringWidth("Right click this button");

            graphics.setColor(ColorScheme.DARKER_GRAY_COLOR);
            graphics.fillRect(bounds.x + bounds.width + 2, bounds.y - 15, width + 6, 30);

            graphics.setColor(ColorScheme.BRAND_ORANGE);
            graphics.drawString("Right click this button", bounds.x + bounds.width + 5, bounds.y);
            graphics.drawString("to see your Banked XP", bounds.x + bounds.width + 5, bounds.y + 12);
        }
        // If on tutorial step 2/2
        else{
            int x = bounds.x - 308;
            int y = bank.getHeight() / 2 + 81;

            graphics.setColor(ColorScheme.BRAND_ORANGE);
            graphics.setStroke(new BasicStroke(2));
            graphics.drawRect(x, y, 200, 21);

            FontMetrics font = graphics.getFontMetrics();
            int width = font.stringWidth("Hover over icons");

            graphics.setColor(ColorScheme.DARKER_GRAY_COLOR);
            graphics.fillRect(x + 205, y - 1, width + 2, 30);

            graphics.setColor(ColorScheme.BRAND_ORANGE);
            graphics.drawString("Hover over icons", x + 205, y + 14);
            graphics.drawString("to see items", x + 205, y + 26);

            final net.runelite.api.Point cursor = client.getMouseCanvasPosition();
            if (new Rectangle(x, y, 200, 23).contains(cursor.getX(), cursor.getY())){
                nextTip = false;
                plugin.hideTutorial();
            }
        }
        return super.render(graphics);
    }
}
