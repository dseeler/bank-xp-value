package com.bankxpvalue;

import java.awt.*;
import java.awt.Point;
import java.util.HashMap;
import net.runelite.api.*;
import java.util.ArrayList;
import javax.inject.Inject;
import java.awt.geom.Rectangle2D;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.SkillColor;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.overlay.components.*;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.ui.overlay.components.ComponentOrientation;
import net.runelite.client.util.ColorUtil;

public class BankXpValueOverlay extends OverlayPanel {

    private final Client client;
    private final BankXpValuePlugin plugin;
    private final BankXpValueConfig config;
    private final TooltipManager tooltipManager;
    private final SkillIconManager iconManager;
    private final PanelComponent skillsBar;
    private final BankXpValueTutorialOverlay tutorialOverlay;
    private Widget bank;

    private final static String[] xpTotals = new String[10];
    private final static ArrayList<PanelComponent> itemPanels = new ArrayList<>();
    private final static HashMap<String, Integer> potentialLevels = new HashMap<>();
    public static boolean initialCenterPosition = true;
    private int iterationCounter = 0;

    @Inject
    private BankXpValueOverlay(Client client, TooltipManager tooltipManager, BankXpValueConfig config,
                               BankXpValuePlugin plugin, BankXpValueTutorialOverlay tutorialOverlay){

        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.tooltipManager = tooltipManager;
        this.tutorialOverlay = tutorialOverlay;

        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(OverlayPriority.HIGHEST);
        setPosition(OverlayPosition.TOP_CENTER);
        setResizable(false);
        setResettable(false);

        panelComponent.setBackgroundColor(new Color(51, 51, 51, 245));

        iconManager = new SkillIconManager();
        skillsBar = new PanelComponent();
        createSkillsBar();
    }

    @Override
    public Dimension render(Graphics2D graphics){
        bank = client.getWidget(WidgetInfo.BANK_CONTAINER);

        panelComponent.getChildren().clear();

        if (bank == null || bank.isHidden()){
            plugin.hideOverlay();

            if (config.showTutorial()){
                tutorialOverlay.nextTip = false;
                plugin.hideTutorial();
            }
            return null;
        }

        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Potential XP Available")
                .build());

        panelComponent.setPreferredSize(new Dimension(
                graphics.getFontMetrics().stringWidth("Total Potential XP Available") + 50, 0));

        if (initialCenterPosition || config.keepFixed()){
            int x = (int)(bank.getBounds().x + (bank.getBounds().getWidth() / 2) - (getBounds().getWidth() / 2));
            int y = (int)(bank.getBounds().y + (bank.getBounds().getHeight()/ 2) - (getBounds().getHeight() / 2));
            setPreferredLocation(new Point(x, y));
            iterationCounter++;

            // Takes 4 iterations for the positioning to properly set in the middle
            if (iterationCounter == 4){
                initialCenterPosition = false;
                iterationCounter = 0;
            }
        }

        displayTotals();

        final net.runelite.api.Point cursor = client.getMouseCanvasPosition();

        if (null != getPreferredLocation()){
            setBounds(graphics, cursor, getPreferredLocation().x + 5, getPreferredLocation().y + 183);
        }

        return super.render(graphics);
    }

    // Sets the calculated XP totals
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
        setPotentialLevels(skillContents);
    }

    // Displays each total and corresponding skill
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

    // Creates the skill bar component (used to see included items)
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

    // Stores xp needed for a level up in hashmap
    private void setPotentialLevels(ItemDataCache.SkillContents[] skillContents){
        potentialLevels.put("construction", Experience.getLevelForXp(client.getSkillExperience(Skill.CONSTRUCTION) + (int)Math.ceil(skillContents[0].total)));
        potentialLevels.put("cooking", Experience.getLevelForXp(client.getSkillExperience(Skill.COOKING) + (int)Math.ceil(skillContents[1].total)));
        potentialLevels.put("crafting", Experience.getLevelForXp(client.getSkillExperience(Skill.CRAFTING) + (int)Math.ceil(skillContents[2].total)));
        potentialLevels.put("farming", Experience.getLevelForXp(client.getSkillExperience(Skill.FARMING) + (int)Math.ceil(skillContents[3].total)));
        potentialLevels.put("firemaking", Experience.getLevelForXp(client.getSkillExperience(Skill.FIREMAKING) + (int)Math.ceil(skillContents[4].total)));
        potentialLevels.put("fletching", Experience.getLevelForXp(client.getSkillExperience(Skill.FLETCHING) + (int)Math.ceil(skillContents[5].total)));
        potentialLevels.put("herblore", Experience.getLevelForXp(client.getSkillExperience(Skill.HERBLORE) + (int)Math.ceil(skillContents[6].total)));
        potentialLevels.put("prayer", Experience.getLevelForXp(client.getSkillExperience(Skill.PRAYER) + (int)Math.ceil(skillContents[7].total)));
        potentialLevels.put("smithing", Experience.getLevelForXp(client.getSkillExperience(Skill.SMITHING) + (int)Math.ceil(skillContents[8].total)));
    }

    // Creates the tooltips that appear when hovering over skill bar
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

    // Creates the hover bounds for each skill bar icon
    private Rectangle2D[] createBounds(Graphics2D graphics, int x, int y){
        Rectangle2D bounds[] = new Rectangle2D[9];

        for (int i = 0; i < bounds.length; i++){
            bounds[i] = new Rectangle2D.Double(x, y, 22, 25);
            x+= 22;
        }
        return bounds;
    }

    // Sets the bounds and executes on detection
    private void setBounds(Graphics2D graphics, net.runelite.api.Point cursor, int x, int y){
        Rectangle2D bounds[] = createBounds(graphics, x, y);

        if (bounds[0].contains(cursor.getX(), cursor.getY())){
            tooltipManager.clear();
            if (itemPanels.get(0).getChildren().size() != 0){
                String tooltip = xpTotals[0] + "xp";
                if (config.potentialLevels())
                    tooltip += "  |  Potential level: " + potentialLevels.get("construction");
                tooltip = ColorUtil.wrapWithColorTag("Construction: ",
                        SkillColor.CONSTRUCTION.getColor().brighter().brighter()) + tooltip;
                tooltipManager.add(new Tooltip(tooltip));
                tooltipManager.add(new Tooltip(itemPanels.get(0)));
            }
            else{
                tooltipManager.add(new Tooltip("Construction: " + xpTotals[0]));
            }
        }
        else if (bounds[1].contains(cursor.getX(), cursor.getY())){
            tooltipManager.clear();
            if (itemPanels.get(1).getChildren().size() != 0){
                String tooltip = xpTotals[1] + "xp";
                if (config.potentialLevels())
                    tooltip += "  |  Potential level: " + potentialLevels.get("cooking");
                tooltip = ColorUtil.wrapWithColorTag("Cooking: ",
                        SkillColor.COOKING.getColor().brighter().brighter()) + tooltip;
                tooltipManager.add(new Tooltip(tooltip));
                tooltipManager.add(new Tooltip(itemPanels.get(1)));
            }
            else{
                tooltipManager.add(new Tooltip("Cooking: " + xpTotals[1]));
            }
        }
        else if (bounds[2].contains(cursor.getX(), cursor.getY())){
            tooltipManager.clear();
            if (itemPanels.get(2).getChildren().size() != 0){
                String tooltip = xpTotals[2] + "xp";
                if (config.potentialLevels())
                    tooltip += "  |  Potential level: " + potentialLevels.get("crafting");
                tooltip = ColorUtil.wrapWithColorTag("Crafting: ",
                        SkillColor.CRAFTING.getColor().brighter().brighter()) + tooltip;
                tooltipManager.add(new Tooltip(tooltip));
                tooltipManager.add(new Tooltip(itemPanels.get(2)));
            }
            else{
                tooltipManager.add(new Tooltip("Crafting: " + xpTotals[2]));
            }
        }
        else if (bounds[3].contains(cursor.getX(), cursor.getY())){
            tooltipManager.clear();
            if (itemPanels.get(3).getChildren().size() != 0){
                String tooltip = xpTotals[3] + "xp";
                if (config.potentialLevels())
                    tooltip += "  |  Potential level: " + potentialLevels.get("farming");
                tooltip = ColorUtil.wrapWithColorTag("Farming: ",
                        SkillColor.FARMING.getColor().brighter().brighter()) + tooltip;
                tooltipManager.add(new Tooltip(tooltip));
                tooltipManager.add(new Tooltip(itemPanels.get(3)));
            }
            else{
                tooltipManager.add(new Tooltip("Farming: " + xpTotals[3]));
            }
        }
        else if (bounds[4].contains(cursor.getX(), cursor.getY())){
            tooltipManager.clear();
            if (itemPanels.get(4).getChildren().size() != 0){
                String tooltip = xpTotals[4] + "xp";
                if (config.potentialLevels())
                    tooltip += "  |  Potential level: " + potentialLevels.get("firemaking");
                tooltip = ColorUtil.wrapWithColorTag("Firemaking: ",
                        new Color(255, 119, 0)) + tooltip;
                tooltipManager.add(new Tooltip(tooltip));
                tooltipManager.add(new Tooltip(itemPanels.get(4)));
            }
            else {
                tooltipManager.add(new Tooltip("Firemaking: " + xpTotals[4]));
            }
        }
        else if (bounds[5].contains(cursor.getX(), cursor.getY())){
            tooltipManager.clear();
            if (itemPanels.get(5).getChildren().size() != 0){
                String tooltip = xpTotals[5] + "xp";
                if (config.potentialLevels())
                    tooltip += "  |  Potential level: " + potentialLevels.get("fletching");
                tooltip = ColorUtil.wrapWithColorTag("Fletching: ",
                        SkillColor.FLETCHING.getColor().brighter().brighter()) + tooltip;
                tooltipManager.add(new Tooltip(tooltip));
                tooltipManager.add(new Tooltip(itemPanels.get(5)));
            }
            else{
                tooltipManager.add(new Tooltip("Fletching: " + xpTotals[5]));
            }
        }
        else if (bounds[6].contains(cursor.getX(), cursor.getY())){
            tooltipManager.clear();
            if (itemPanels.get(6).getChildren().size() != 0){
                String tooltip = xpTotals[6] + "xp";
                if (config.potentialLevels())
                    tooltip += "  |  Potential level: " + potentialLevels.get("herblore");
                tooltip = ColorUtil.wrapWithColorTag("Herblore: ",
                        SkillColor.HERBLORE.getColor().brighter().brighter()) + tooltip;
                tooltipManager.add(new Tooltip(tooltip));
                tooltipManager.add(new Tooltip(itemPanels.get(6)));
            }
            else {
                tooltipManager.add(new Tooltip("Herblore: " + xpTotals[6]));
            }
        }
        else if (bounds[7].contains(cursor.getX(), cursor.getY())){
            tooltipManager.clear();
            if (itemPanels.get(7).getChildren().size() != 0){
                String tooltip = xpTotals[7] + "xp";
                if (config.potentialLevels())
                    tooltip += "  |  Potential level: " + potentialLevels.get("prayer");
                tooltip = ColorUtil.wrapWithColorTag("Prayer: ",
                        SkillColor.PRAYER.getColor().brighter().brighter()) + tooltip;
                tooltipManager.add(new Tooltip(tooltip));
                tooltipManager.add(new Tooltip(itemPanels.get(7)));
            }
            else {
                tooltipManager.add(new Tooltip("Prayer: " + xpTotals[7]));
            }
        }
        else if (bounds[8].contains(cursor.getX(), cursor.getY())){
            tooltipManager.clear();
            if (itemPanels.get(8).getChildren().size() != 0){
                String tooltip = xpTotals[8] + "xp";
                if (config.potentialLevels())
                    tooltip += "  |  Potential level: " + potentialLevels.get("smithing");
                tooltip = ColorUtil.wrapWithColorTag("Smithing: ",
                        SkillColor.SMITHING.getColor().brighter().brighter()) + tooltip;
                tooltipManager.add(new Tooltip(tooltip));
                tooltipManager.add(new Tooltip(itemPanels.get(8)));
            }
            else {
                tooltipManager.add(new Tooltip("Smithing: " + xpTotals[8]));
            }
        }
    }
}
