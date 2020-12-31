package com.bankxpvalue;

import java.util.List;
import java.util.HashMap;
import java.io.FileReader;
import java.util.ArrayList;
import com.google.gson.Gson;
import net.runelite.api.Item;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import java.awt.image.BufferedImage;
import net.runelite.api.ItemComposition;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.components.ImageComponent;
import lombok.AllArgsConstructor;
import javax.inject.Inject;

public class ItemDataCache {

    @AllArgsConstructor
    class ItemData{
        public int id;
        public double xp;
        public String skill;
    }

    @AllArgsConstructor
    public class SkillContents{
        public double total;
        public List<ImageComponent> images;
    }

    private final static HashMap<String, Integer> skills = new HashMap<>();
    private final static HashMap<Integer, ItemData> cache = new HashMap<>();
    private final ItemManager itemManager;

    @Inject
    public ItemDataCache(ItemManager itemManager){
        mapSkills();
        populateCache();
        this.itemManager = itemManager;
    }

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
        try{
            Gson gson = new Gson();
            JsonElement json = gson.fromJson(new FileReader("src/main/java/com/" +
                    "bankxpvalue/item_xp_data.json"), JsonElement.class);
            JsonObject root = json.getAsJsonObject();
            JsonArray items = root.getAsJsonArray("items");

            for (int i = 0; i < items.size(); i++){
                JsonObject element = items.get(i).getAsJsonObject();
                ItemData data = new ItemData(element.get("id").getAsInt(), element.get("xp").getAsDouble(),
                        element.get("skill").getAsString());

                cache.put(data.id, data);
            }
        }
        catch (Exception e){
            e.printStackTrace();
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
                final BufferedImage image = getImage(items[i]);
                skillContents[skills.get(data.skill)].images.add(new ImageComponent(image));
                if (skillContents[skills.get(data.skill)].equals("herblore")){
                    System.out.println(items[i].getId());
                }
            }
        }
        return skillContents;
    }

    private BufferedImage getImage(Item item){
        ItemComposition composition = itemManager.getItemComposition(item.getId());
        return itemManager.getImage(item.getId(), item.getQuantity(), true);
    }

    public ItemData getItem(int id){
        if (cache.containsKey(id)){
            return cache.get(id);
        }
        return null;
    }
}
