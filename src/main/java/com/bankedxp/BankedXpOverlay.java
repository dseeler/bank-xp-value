package com.bankedxp;

import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.SkillColor;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.*;
import net.runelite.client.ui.overlay.components.ComponentOrientation;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import javax.inject.Inject;
import java.awt.*;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class BankedXpOverlay extends OverlayPanel {

    private final Client client;
    private final BankedXpPlugin plugin;
    private final BankedXpConfig config;
    private final ItemManager itemManager;
    private final TooltipManager tooltipManager;
    private final SkillIconManager iconManager;
    private final PanelComponent skillsBar;
    private final static String[] xpTotals = new String[10];
    private final ArrayList<PanelComponent> itemPanels = new ArrayList<>();

    @Inject
    private BankedXpOverlay(Client client, ItemManager itemManager, TooltipManager tooltipManager,
                            BankedXpConfig config, BankedXpPlugin plugin){

        this.client = client;
        this.plugin = plugin;
        this.itemManager = itemManager;
        this.config = config;
        this.tooltipManager = tooltipManager;

        setLayer(OverlayLayer.ALWAYS_ON_TOP);
        setPriority(OverlayPriority.HIGHEST);
        setPosition(OverlayPosition.TOP_CENTER);

        panelComponent.setBackgroundColor(new Color(51, 51, 51, 245));

        iconManager = new SkillIconManager();
        skillsBar = new PanelComponent();
        createSkillsBar();
    }

    @Override
    public Dimension render(Graphics2D graphics){
        Widget bank = client.getWidget(WidgetInfo.BANK_CONTAINER);

        panelComponent.getChildren().clear();

        if (bank == null || bank.isHidden()){
            plugin.hideOverlay();
            return null;
        }

        panelComponent.setPreferredLocation(new Point(0, bank.getHeight() / 5 + 9));

        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Potential XP Available")
                .build());

        panelComponent.setPreferredSize(new Dimension(
                graphics.getFontMetrics().stringWidth("Total Potential XP Available") + 50, 0));

        displayTotals();

        final net.runelite.api.Point cursor = client.getMouseCanvasPosition();
        setBounds(graphics, cursor, (int)(bank.getBounds().getCenterX() - 100),
                bank.getHeight() / 5 + 9 + 188);

        return super.render(graphics);
    }

    public void setXpTotals(ItemDataCache.SkillContents[] skillContents){
        for (int i = 0; i < skillContents.length; i++){
            if ((int)skillContents[i].total == 0){
                xpTotals[i] = "None";
            }
            else{
                xpTotals[i] = String.format("%,d", (int)Math.ceil(skillContents[i].total));
            }
        }
        createTooltips(skillContents);
    }

    private void displayTotals(){
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
                .leftColor(new Color(255, 119, 0))
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

        panelComponent.getChildren().add(skillsBar);
    }

    private void createSkillsBar(){
        skillsBar.setGap(new Point(6, 0));
        skillsBar.setBackgroundColor(Color.DARK_GRAY);
        skillsBar.setOrientation(ComponentOrientation.HORIZONTAL);
        skillsBar.getChildren().add(new ImageComponent(iconManager.getSkillImage(Skill.CONSTRUCTION, true)));
        skillsBar.getChildren().add(new ImageComponent(iconManager.getSkillImage(Skill.COOKING, true)));
        skillsBar.getChildren().add(new ImageComponent(iconManager.getSkillImage(Skill.CRAFTING, true)));
        skillsBar.getChildren().add(new ImageComponent(iconManager.getSkillImage(Skill.FARMING, true)));
        skillsBar.getChildren().add(new ImageComponent(iconManager.getSkillImage(Skill.FIREMAKING, true)));
        skillsBar.getChildren().add(new ImageComponent(iconManager.getSkillImage(Skill.FLETCHING, true)));
        skillsBar.getChildren().add(new ImageComponent(iconManager.getSkillImage(Skill.HERBLORE, true)));
        skillsBar.getChildren().add(new ImageComponent(iconManager.getSkillImage(Skill.PRAYER, true)));
        skillsBar.getChildren().add(new ImageComponent(iconManager.getSkillImage(Skill.SMITHING, true)));
    }

    private void createTooltips(ItemDataCache.SkillContents[] skillContents){
        if (itemPanels.size() != 0){
            itemPanels.clear();
        }

        for (int i = 0; i < skillContents.length; i++){
            PanelComponent panel = new PanelComponent();
            panel.setOrientation(ComponentOrientation.HORIZONTAL);
            panel.setPreferredSize(new Dimension(250, 0));
            panel.setWrap(true);
            for (int j = 0; j < skillContents[i].images.size(); j++){
                panel.getChildren().add(skillContents[i].images.get(j));
            }
            itemPanels.add(panel);
        }
    }

    private Rectangle2D[] createBounds(Graphics2D graphics, int x, int y){
        Rectangle2D bounds[] = new Rectangle2D[9];

        for (int i = 0; i < bounds.length; i++){
            bounds[i] = new Rectangle2D.Double(x, y, 22, 25);
            x+= 22;
        }
        return bounds;
    }

    private void setBounds(Graphics2D graphics, net.runelite.api.Point cursor, int x, int y){
        Rectangle2D bounds[] = createBounds(graphics, x, y);

        if (bounds[0].contains(cursor.getX(), cursor.getY())){
            if (itemPanels.get(0).getChildren().size() != 0){
                tooltipManager.add(new Tooltip("Construction: " + xpTotals[0] + "xp"));
                tooltipManager.add(new Tooltip(itemPanels.get(0)));
            }
            else{
                tooltipManager.add(new Tooltip("Construction: " + xpTotals[0]));
            }
        }
        else if (bounds[1].contains(cursor.getX(), cursor.getY())){
            if (itemPanels.get(1).getChildren().size() != 0){
                tooltipManager.add(new Tooltip("Cooking: " + xpTotals[1] + "xp"));
                tooltipManager.add(new Tooltip(itemPanels.get(1)));
            }
            else{
                tooltipManager.add(new Tooltip("Cooking: " + xpTotals[1]));
            }
        }
        else if (bounds[2].contains(cursor.getX(), cursor.getY())){
            if (itemPanels.get(2).getChildren().size() != 0){
                tooltipManager.add(new Tooltip("Crafting: " + xpTotals[2] + "xp"));
                tooltipManager.add(new Tooltip(itemPanels.get(2)));
            }
            else{
                tooltipManager.add(new Tooltip("Crafting: " + xpTotals[2]));
            }
        }
        else if (bounds[3].contains(cursor.getX(), cursor.getY())){
            if (itemPanels.get(3).getChildren().size() != 0){
                tooltipManager.add(new Tooltip("Farming: " + xpTotals[3] + "xp"));
                tooltipManager.add(new Tooltip(itemPanels.get(3)));
            }
            else{
                tooltipManager.add(new Tooltip("Farming: " + xpTotals[3]));
            }
        }
        else if (bounds[4].contains(cursor.getX(), cursor.getY())){
            if (itemPanels.get(4).getChildren().size() != 0){
                tooltipManager.add(new Tooltip("Firemaking: " + xpTotals[4] + "xp"));
                tooltipManager.add(new Tooltip(itemPanels.get(4)));
            }
            else{
                tooltipManager.add(new Tooltip("Firemaking: " + xpTotals[4]));
            }
        }
        else if (bounds[5].contains(cursor.getX(), cursor.getY())){
            if (itemPanels.get(5).getChildren().size() != 0){
                tooltipManager.add(new Tooltip("Fletching: " + xpTotals[5] + "xp"));
                tooltipManager.add(new Tooltip(itemPanels.get(5)));
            }
            else{
                tooltipManager.add(new Tooltip("Fletching: " + xpTotals[5]));
            }
        }
        else if (bounds[6].contains(cursor.getX(), cursor.getY())){
            if (itemPanels.get(6).getChildren().size() != 0){
                tooltipManager.add(new Tooltip("Herblore: " + xpTotals[6] + "xp"));
                tooltipManager.add(new Tooltip(itemPanels.get(6)));
            }
            else{
                tooltipManager.add(new Tooltip("Herblore: " + xpTotals[6]));
            }
        }
        else if (bounds[7].contains(cursor.getX(), cursor.getY())){
            if (itemPanels.get(7).getChildren().size() != 0){
                tooltipManager.add(new Tooltip("Prayer: " + xpTotals[7] + "xp"));
                tooltipManager.add(new Tooltip(itemPanels.get(7)));
            }
            else{
                tooltipManager.add(new Tooltip("Prayer: " + xpTotals[7]));
            }
        }
        else if (bounds[8].contains(cursor.getX(), cursor.getY())){
            if (itemPanels.get(8).getChildren().size() != 0){
                tooltipManager.add(new Tooltip("Smithing: " + xpTotals[8] + "xp"));
                tooltipManager.add(new Tooltip(itemPanels.get(8)));
            }
            else{
                tooltipManager.add(new Tooltip("Smithing: " + xpTotals[8]));
            }
        }
    }
}
