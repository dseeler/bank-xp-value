package com.bankxpvalue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import java.awt.image.BufferedImage;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.components.ImageComponent;
import lombok.AllArgsConstructor;
import javax.inject.Inject;

@Slf4j
public class ItemDataCache {

    @AllArgsConstructor
    class ItemData{
        public int id;
        public double xp;
        public String skill;
    }

    @AllArgsConstructor
    class SkillContents{
        public double total;
        public List<ImageComponent> images;
    }

    @AllArgsConstructor
    class ItemDataContainer{
        List<ItemData> items;
    }

    private static HashMap<String, Integer> skills = new HashMap<>();
    private static HashMap<Integer, ItemData> cache = new HashMap<>();
    private final ItemManager itemManager;
    private final Gson gson;

    @Inject
    public ItemDataCache(ItemManager itemManager, Gson gson){
        mapSkills();
        populateCache();
        this.itemManager = itemManager;
        this.gson = gson;
    }

    // Set the skills
    private void mapSkills(){
        skills.put("construction", 0);
        skills.put("cooking", 1);
        skills.put("crafting", 2);
        skills.put("farming", 3);
        skills.put("firemaking", 4);
        skills.put("fletching", 5);
        skills.put("herblore", 6);
        skills.put("prayer", 7);
        skills.put("smithing", 8);
    }

    // Stores json data in hashmap
    private void populateCache(){
        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream("/item_xp_data.json"), StandardCharsets.UTF_8)) {
            ItemDataContainer data = gson.fromJson(reader, ItemDataContainer.class);
            for (ItemData item : data.items) {
                cache.put(item.id, item);
            }
        } catch (IOException e) {
            log.warn("Failed to read item xp data", e);
        }
    }

    // Computes the total xp for each skill
    public SkillContents[] getTotals(Item[] items){
        SkillContents[] skillContents = new SkillContents[10];

        for (int i = 0; i < skillContents.length; i++){
            skillContents[i] = new SkillContents(0.0, new ArrayList<>());
        }

        for (int i = 0; i < items.length; i++){
            if (cache.containsKey(items[i].getId())){
                ItemData data = cache.get(items[i].getId());

                // Add the XP to the skill's total
                skillContents[skills.get(data.skill)].total += data.xp * items[i].getQuantity();
                skillContents[9].total += data.xp * items[i].getQuantity();

                // Add the image to the skill's tooltip
                final BufferedImage image = itemManager.getImage(items[i].getId(), items[i].getQuantity(), true);
                skillContents[skills.get(data.skill)].images.add(new ImageComponent(image));
            }
        }
        return skillContents;
    }

    // Outside classes use to search hash table
    public ItemData getItem(int id){
        if (cache.containsKey(id)){
            return cache.get(id);
        }
        return null;
    }
}
