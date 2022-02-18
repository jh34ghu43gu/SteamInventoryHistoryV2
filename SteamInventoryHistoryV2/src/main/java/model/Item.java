package model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ch.qos.logback.classic.Logger;

public class Item {
	
	private static final Logger log = (Logger) LoggerFactory.getLogger(Item.class);
	
	public static String[] qualities = {"Unique", "Strange", "Unusual", "Decorated Weapon",
			"Vintage", "Genuine", "Haunted", "Collector's", "Normal", "Community", "Self-Made", "Valve"
	};
	
	private String itemName;
	private String quality;
	private String secondaryQuality;
	private String special;
	
	/**
	 * @param itemName 
	 * @param color Color code including #
	 */
	public Item(String itemName, String color) {
		this.itemName = itemName;
		this.isSpecial();
		quality = colorToQuality(color);
		if(quality.equals("Unknown")) {
			log.warn("Item: " + itemName + " has unknown color code: " + color);
		}
		secondaryQuality = "";
		for(String s : qualities) { //If this takes too much time, possibly cut qualities down to the common combos
			if(quality.contains(s)) { continue; }
			if(itemName.contains(s)) {
				//Edge cases for unique vintages
				if(s.equals("Vintage")) {
					if(itemName.contains("Vintage Tyrolean") || itemName.contains("Vintage Merryweather")) {
						continue;
					}
				}
				//Edge case for some strange tools
				if(s.equals("Strange")) {
					if(itemName.contains("Strange Part") || itemName.contains("Strange Cosmetic Part") 
							|| itemName.equals("Strange Count Transfer Tool") || itemName.contains("Strange Filter")) {
						continue;
					}
				}
				//Edge case for strange part
				if(s.equals("Unusual")) {
					if(itemName.equals("Strange Part: Unusual-Wearing Player Kills")) {
						continue;
					}
				}
				//Edge case for community update names and filters
				if(s.equals("Community")) {
					if(itemName.contains("Community Update") || itemName.contains("Community Crate") 
							|| itemName.contains("Strange Filter")) {
						continue;
					}
				}
				//Edge case for war paint name and hms/haunted hat
				if(s.equals("Haunted")) {
					if(itemName.contains("Haunted Ghosts") || itemName.contains("Haunted Hat") || itemName.contains("Haunted Metal Scrap")) {
						continue;
					}
				}
				//Edge case for collector chems
				if(s.equals("Collector's")) {
					if(itemName.contains("Chemistry Set")) {
						continue;
					}
				}
				secondaryQuality = s;
				break;
			}
		}
	}
	
	public Item(String itemName, String quality, String secondaryQuality, String special) {
		super();
		this.itemName = itemName;
		this.special = special;
		this.quality = quality;
		this.secondaryQuality = secondaryQuality;
	}

	
	public static String colorToQuality(String color) {
		//Order of presumed most likely
		if(color.equalsIgnoreCase("#7D6D00")) {
			return "Unique";
		} else if(color.equalsIgnoreCase("#CF6A32")) {
			return "Strange";
		} else if(color.equalsIgnoreCase("#8650AC")) {
			return "Unusual";
		} else if(color.equalsIgnoreCase("#FAFAFA")) {
			return "Decorated Weapon";
		} else if(color.equalsIgnoreCase("#476291")) {
			return "Vintage";
		} else if(color.equalsIgnoreCase("#4D7455")) {
			return "Genuine";
		} else if(color.equalsIgnoreCase("#38F3AB")) {
			return "Haunted";
		} else if(color.equalsIgnoreCase("#AA0000")) {
			return "Collector's";
		} else if(color.equalsIgnoreCase("#B2B2B2")) {
			return "Normal";
		} else if(color.equalsIgnoreCase("#70B04A")) {
			return "Community/Self-Made";
		} else if(color.equalsIgnoreCase("#A50F79")) {
			return "VALVE";
		} else if(color.equalsIgnoreCase("Undefined")) {
			return "Undefined";
		} else if(color.equalsIgnoreCase("#D2D2D2")) {
			return "CS:GO";
		} else if(color.equalsIgnoreCase("#f15840") || color.equalsIgnoreCase("#35a3f1")) {
			return "RUST";
		}
		log.warn("Unknown color code: " + color);
		return "Unknown";
	}

	/**
	 * Compare properties of a second item to this item. Returns true if all properties are the same.
	 * @param i2
	 * @return
	 */
	public boolean compare(Item i2) {
		boolean same = true;
		if(!itemName.equals(i2.getItemName())) {
			same = false;
		} else if(!quality.equals(i2.getQuality())) {
			same = false;
		} else if(!secondaryQuality.equals(i2.getSecondaryQuality())) {
			same = false;
		}
		return same;
	}
	
	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
		this.isSpecial();
	}

	public String getQuality() {
		return quality;
	}

	public void setQuality(String quality) {
		this.quality = quality;
	}

	public String getSecondaryQuality() {
		return secondaryQuality;
	}

	public void setSecondaryQuality(String secondaryQuality) {
		this.secondaryQuality = secondaryQuality;
	}

	public String toString(String tabs) {
		String secondQ = secondaryQuality.isEmpty() ? "" : " " + secondaryQuality;
		return tabs + quality + secondQ + " " + itemName;
	}
	
	public JsonObject toJson() {
		JsonObject item = new JsonObject();
		item.addProperty("name", itemName);
		item.addProperty("quality", quality);
		//Save file space
		if(!secondaryQuality.isEmpty()) {
			item.addProperty("secondaryQuality", secondaryQuality);
		}
		if(!special.isEmpty()) {
			item.addProperty("special", special);
		}
		return item;
	}
	
	public String getSpecial() {
		return special;
	}
	
	public void setSpecial(String special) {
		this.special = special;
	}

	/**
	 * Internal method run alongside setting itemName to determine if item is a
	 * Weapon
	 * Paint
	 * Hat
	 * RoboHat
	 * Tool
	 */
	private void isSpecial() {
		special = "";
		Long startTime = System.currentTimeMillis();
		Long time = startTime;
		InputStream is = Item.class.getClassLoader().getResourceAsStream("specials.json");
		Gson gson = new Gson();
		try {
			JsonObject object = gson.fromJson(new InputStreamReader(is, "UTF-8"), JsonObject.class);
			
			//log.debug("Time to load json: " + (System.currentTimeMillis()-time));
			//time = System.currentTimeMillis();
			
			//Weapon check, all weapons start with The
			if(itemName.startsWith("The")) {
				JsonArray weaponArray = object.get("Weapons").getAsJsonArray();
				for(JsonElement el : weaponArray) {
					if(itemName.equals("The " + el.getAsJsonObject().get("Name").getAsString())) {
						special = "Weapon";
						return;
					}
				}
			}
			
			//log.debug("Time to loop weapons: " + (System.currentTimeMillis()-time));
			//time = System.currentTimeMillis();
			
			//RoboHat
			JsonArray roboHatArray = object.get("RoboHats").getAsJsonArray();
			for(JsonElement el : roboHatArray) {
				if(itemName.equals(el.getAsString())) {
					special = "RoboHat";
					return;
				}
			}
			//Tool
			JsonArray toolArray = object.get("Tools").getAsJsonArray();
			for(JsonElement el : toolArray) {
				if(itemName.equals(el.getAsString())) {
					special = "Tool";
					return;
				}
			}
			//Paint
			JsonArray paintArray = object.get("Paints").getAsJsonArray();
			for(JsonElement el : paintArray) {
				if(itemName.equals(el.getAsJsonObject().get("Name").getAsString())) {
					special = "Paint";
					return;
				}
			}
			
			//log.debug("Time to loop paint/robo/tools: " + (System.currentTimeMillis()-time));
			//time = System.currentTimeMillis();
			
			//Hat check is the most expensive do it last
			JsonArray hatArray = object.get("Hats").getAsJsonArray();
			for(JsonElement el : hatArray) {
				if(itemName.equals(el.getAsString())) {
					special = "Hat";
					return;
				}
			}
			
			//log.debug("Time to loop hats: " + (System.currentTimeMillis()-time));
			//time = System.currentTimeMillis();
		} catch(IOException e) {
			e.printStackTrace();
		}
		//log.debug("Total time: " + (System.currentTimeMillis()-startTime));
	}
}
