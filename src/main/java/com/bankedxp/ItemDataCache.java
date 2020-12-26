package com.bankedxp;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.runelite.api.Item;
import net.runelite.api.ItemID;
import javax.inject.Inject;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import javax.*;
import com.google.gson.Gson;

public class ItemDataCache {

    class ItemData{
        public int id;
        public double xp;
        public String skill;

        public ItemData(int id, double xp, String skill){
            this.id = id;
            this.xp = xp;
            this.skill = skill;
        }
    }

    private final static HashMap<String, Integer> skills = new HashMap<>();
    private final static HashMap<Integer, ItemData> cache = new HashMap<>();

    public ItemDataCache(){
        mapSkills();
        populateCache();
    }

    private void mapSkills(){
        skills.put("prayer", 0);
        skills.put("construction", 1);
        skills.put("crafting", 2);
        skills.put("fletching", 3);
        skills.put("smithing", 4);
        skills.put("cooking", 5);
        skills.put("farming", 6);
    }

    private void populateCache(){
        try{
            Gson gson = new Gson();
            JsonElement json = gson.fromJson(new FileReader("src/main/java/com/" +
                    "bankedxp/item_xp_data.json"), JsonElement.class);
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

    public double[] getTotals(Item[] items){
        double[] totals = new double[8];

        // Initialize array
        for (int i = 0; i < totals.length; i++){
            totals[i] = 0.0;
        }

        for (int i = 0; i < items.length; i++){
            if (cache.containsKey(items[i].getId())){
                ItemData data = cache.get(items[i].getId());
                totals[skills.get(data.skill)] += data.xp * items[i].getQuantity();
                totals[7] += data.xp * items[i].getQuantity();
            }
        }
        return totals;
    }

}
