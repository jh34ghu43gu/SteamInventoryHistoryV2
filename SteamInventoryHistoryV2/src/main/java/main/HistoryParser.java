package main;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ch.qos.logback.classic.Logger;
import model.Item;
import model.inventoryEvents.InventoryEvent;
import model.inventoryEvents.MannUpEvent;

public class HistoryParser {
	
	private static final Logger log = (Logger) LoggerFactory.getLogger(HistoryParser.class);
	//private static String[] special_used_items = {"" };
	
	public static ArrayList<InventoryEvent> readHtmlFile(File file) {
		ArrayList<InventoryEvent> events = new ArrayList<InventoryEvent>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mma");

		Element nullHandleRow = null;
		try {
			Document doc = Jsoup.parse(file, "UTF-8", "https://steamcommunity.com/");
			
			Elements rows = doc.select("div.tradehistoryrow");
			//Combine certain events that are connected ie opening an unlocked multiclass crate
			boolean lastEventUsed = false;
			Element lastEventUsedRow = null;
			LocalDateTime lastEventUsedDate = null;
			Elements giftEventCache = new Elements();
			
			for(Element row : rows) {
				boolean errorEvent = false;
				nullHandleRow = row;
				String dateString = row.select("div.tradehistory_date").first().text();
				if(dateString.endsWith("am")) {
					dateString = dateString.substring(0,dateString.length()-2) + "AM";
				}
				if(dateString.endsWith("pm")) {
					dateString = dateString.substring(0,dateString.length()-2) + "PM";
				}
				LocalDateTime date = LocalDateTime.parse(dateString, formatter);
				
				String type = row.select("div.tradehistory_event_description").first().text();
				//log.debug(type);
				if(type.equals("You purchased an item on the Community Market.") || type.equals("Received from the Steam Community Market")) {
					type = "SCM Purchase";
				} else if(type.equals("You listed an item on the Community Market.") || type.equals("Listed on the Steam Community Market")) {
					type = "SCM Listing";
				} else if(type.equals("You canceled a listing on the Community Market. The item was returned to you.")) {
					type = "SCM Cancel";
				} else if(type.startsWith("You traded with") && type.contains("but the trade was placed on hold. The items will be delivered later on")) {
					continue; //Trade-hold throws an error when trying to find +/- on items gained since it does not exist
					//Must be before "You traded with"
				} else if(type.startsWith("You traded with") || type.equals("Traded")
						|| (type.startsWith("Your trade with ") && type.endsWith("was on hold, and the trade has now completed."))) {
					type = "Trade";
				} else if(type.equals("Played MvM Mann Up Mode")) {
					type = "Mann Up";
				} else if(type.equals("MvM Squad Surplus bonus")) {
					type = "Surplus";
				} else if(type.equals("Unlocked a crate")) {
					type = "Unbox";
				} else if(type.equals("Purchased from the store")) {
					type = "Store Purchase";
				} else if(type.equals("Traded up")) {
					type = "Trade Up";
				} else if(type.equals("Gift wrapped")) {
					type = "Wrapped";
				} else if(type.startsWith("Received a gift from")) {
					type = "Player Gift";
				} else if(type.startsWith("Your trade with ") && type.endsWith("failed. The items have been returned to you.")) {
					type = "Failed Trade";
				} else if(type.equals("You deleted")) {
					type = "Deleted";
				} else if(type.equals("Used as input to recipe")) {
					type = "Recipe Input";
				} else if(type.equals("Recipe completed")) {
					type = "Recipe Finish";
				} else if(type.equals("Name changed")) {
					type = "Name Change";
				} else if(type.equals("Strangified an item")) {
					type = "Kit/Strangifier/Festivizer Applied";
				} else if(type.equals("Earned a promotional item")) {
					type = "Promo";
				} else if(type.equals("Started testing an item from the store")) {
					type = "Rent Start";
				} else if(type.equals("Preview period ended")) {
					type = "Rent End";
				} else if(type.equals("Received a gift")) {
					type = "Gift";
				} else if(type.equals("Used")) {
					lastEventUsed = true;
					lastEventUsedDate = date;
					lastEventUsedRow = row;
					continue;
				} else if(type.equals("Completed a Contract")) {
					type = "Contract";
				} else if(type.equals("Halloween transmute performed")) {
					type = "Transmute";
				} else if(type.equals("Earned from unlocking an achievement")) {
					type = "Achievement";
				} else if(type.equals("Earned from unlocking an achievement in a different game")) {
					type = "Promo Achievement";
				} else if(type.equals("Antique Halloween Goodie Cauldron")) {
					type = "Halloween Cauldron";
				} else if(type.equals("Earned by participating in the Halloween event")) {
					type = "Halloween Event";
				} else if(type.equals("Level upgraded by defeating Merasmus")) {
					type = "Merasmus";
				} else if(type.startsWith("Gift sent to")) {
					type = "Sent Gift";
				} else if(type.equals("Took the Strange scores from one item and added them on to another")) {
					type = "Strange Transfer";
				} else if(type.equals("Borrowed for a Contract")) {
					type = "Borrowed";
				} else if(type.equals("Generated by Competitive Beta Pass")) {
					type = "Comp Beta";
				} else if(type.startsWith("Your held trade with") && type.endsWith("was canceled. The items have been returned to you.")) {
					type = "Trade Canceled";
				} else if(type.equals("Texture customized")) {
					type = "Used Decal";
				} else if(type.equals("Received by entering product code")) {
					continue; //This appears to be a duplicate for "Unpacked"
				} else if(type.equals("Periodic score system reward")) {
					type = "Daily Hat";
				} else if(type.equals("Periodic score system reward was removed")) {
					type = "Daily Hat Removed";
				} else if(type.equals("Strange score reset")) {
					type = "Reset Strange";
				} else if(type.equals("Card Upgrade Removed")) {
					type = "Removed Spell";
				} else if(type.equals("Card Upgraded")) {
					type = "Applied Spell";
				} else if(!type.equals("Found") && !type.equals("Removed gifter's name") && !type.equals("Removed Killstreak effects")
						&& !type.equals("Unwrapped") && !type.equals("Crafted") && !type.equals("Earned")
						&& !type.equals("Removed a Strange Part") && !type.equals("Applied a Strange Part")
						&& !type.equals("Added a Spell Page") && !type.equals("Custom name removed") 
						&& !type.equals("Removed or modified") && !type.equals("Added") //These 2 are for redeeming war paints, should try to combine later TODO
						&& !type.equals("Expired") && !type.equals("Transmogrified") && !type.equals("Item painted")
						&& !type.equals("Removed crafter's name") && !type.equals("Refunded") && !type.equals("Unpacked")
						&& !type.equals("Purchased with Blood Money") && !type.equals("Unusual effects adjusted")
						&& !type.equals("Applied a Strange Filter")) {
					log.warn("Unchecked InventoryEvent Type: " + type);
				}
				
				//Special for gift items
				if(type.equals("Gift") && lastEventUsed) {
					String itemUsed = lastEventUsedRow.select("span.history_item_name").first().text();
					if(itemUsed.startsWith("Unlocked Cosmetic Crate") || itemUsed.startsWith("Gift-Stuffed Stocking")
							|| itemUsed.startsWith("Unlocked Winter 2016") || itemUsed.startsWith("Unlocked Creepy")
							|| itemUsed.contains("Mann Co. Store Package")) {
						giftEventCache.add(row);
						continue; //We will finalize the event after all gifted items are added to the cache. This is not 100% accurate for edge cases
					} else if(!itemUsed.equals("Halloween Package") && !itemUsed.equals("Enchantment: Eternaween")
							&& !itemUsed.equals("Secret Saxton") && !itemUsed.equals("Halloween Gift Cauldron")
							&& !itemUsed.equals("Backpack Expander") && !itemUsed.equals("Smissmas 2015 Festive Gift")
							&& !itemUsed.equals("Antique Halloween Goodie Cauldron") && !itemUsed.equals("Tough Break Campaign Pass")
							&& !itemUsed.equals("Gun Mettle Campaign Pass") && !itemUsed.equals("Dueling Mini-Game")
							&& !itemUsed.equals("Invasion Community Update Pass") && !itemUsed.equals("Jungle Inferno Campaign Pass")) {
						log.warn("Unchecked 'Used' Item: " + itemUsed);
					}
				}
				
				//Need to finalize an event that was put on hold
				if(lastEventUsed) { //&& !giftEventCache.isEmpty()
					String oldEventType = "Used";
					String itemUsed = lastEventUsedRow.select("span.history_item_name").first().text();
					if(itemUsed.startsWith("Unlocked Cosmetic Crate") || itemUsed.startsWith("Unlocked Winter 2016")
							|| itemUsed.startsWith("Unlocked Creepy")) {
						oldEventType = "Unbox";
					} else if(itemUsed.startsWith("Gift-Stuffed Stocking")) {
						oldEventType = "Open Stocking";
					} else if(itemUsed.equals("Mann Co. Store Package")) {
						oldEventType = "Open Store Package";
					} else if(!itemUsed.equals("Halloween Package") && !itemUsed.equals("Enchantment: Eternaween")
							&& !itemUsed.equals("Secret Saxton") && !itemUsed.equals("Halloween Gift Cauldron")
							&& !itemUsed.equals("Backpack Expander") && !itemUsed.equals("Smissmas 2015 Festive Gift")
							&& !itemUsed.equals("Antique Halloween Goodie Cauldron") && !itemUsed.equals("Tough Break Campaign Pass")
							&& !itemUsed.equals("Gun Mettle Campaign Pass") && !itemUsed.equals("Dueling Mini-Game")
							&& !itemUsed.equals("Invasion Community Update Pass") && !itemUsed.equals("Jungle Inferno Campaign Pass")) {
						log.error("Unchecked 'Used' Item: " + itemUsed + " while finalizing an event. Faulty event creation probable.");
						errorEvent = true;
					}
					InventoryEvent event = new InventoryEvent(lastEventUsedDate, oldEventType);
					giftEventCache.add(lastEventUsedRow);
					for(Element miniEvent : giftEventCache) {
						if(miniEvent.select("div.tradehistory_items_plusminus").size() > 1) { //Shouldn't happen for these cases
							log.warn("More than one plusminus element found in gift/used event.");
							log.debug(miniEvent.toString());
						}
						if(miniEvent.select("div.tradehistory_items_plusminus").first().text().equals("+")) {
							for(Element itemElement : miniEvent.select("span.history_item_name")) {
								String itemName = itemElement.text();
								String[] styles = itemElement.attr("style").split(":");
								String color = "Undefined";
								for(int i = 0; i < styles.length; i++) {
									if(styles[i].equalsIgnoreCase("color")) {
										color = styles[i+1].trim();
										break;
									}
								}
								event.addItemGained(new Item(itemName, color));
							}
						} else if(miniEvent.select("div.tradehistory_items_plusminus").first().text().equals("-")) {
							for(Element itemElement : miniEvent.select("span.history_item_name")) {
								String itemName = itemElement.text();
								String[] styles = itemElement.attr("style").split(":");
								String color = "Undefined";
								for(int i = 0; i < styles.length; i++) {
									if(styles[i].equalsIgnoreCase("color")) {
										color = styles[i+1].trim();
										break;
									}
								}
								event.addItemLost(new Item(itemName, color));
							}
						} else {
							log.warn("Could not locate a plus/minus for a miniEvent");
							log.debug("MiniEvent: " + miniEvent.toString());
						}
					}
					events.add(event);
					if(errorEvent) {
						log.debug("Possible error event created: \n" + event.toString("\t"));
					}
					//Cleanup
					giftEventCache.clear();
					if(type.equals("Used")) {
						lastEventUsedDate = date;
						lastEventUsedRow = row;
						continue; //Skip creating an event.
					} else {
						lastEventUsed = false;
					}
					
				}
				
				//Create events for others
				InventoryEvent event = new InventoryEvent(date, type);
				String tour = "";
				for(Element miniEvent : row.select("div.tradehistory_items")) {
					if(miniEvent.select("div.tradehistory_items_plusminus").first().text().equals("+")) {
						for(Element itemElement : miniEvent.select("span.history_item_name")) {
							String itemName = itemElement.text();
							String[] styles = itemElement.attr("style").split(":");
							String color = "Undefined";
							for(int i = 0; i < styles.length; i++) {
								if(styles[i].equalsIgnoreCase("color")) {
									color = styles[i+1].trim();
									break;
								}
							}
							event.addItemGained(new Item(itemName, color));
							//Tours MUST be detected in gain since 1st mission doesn't remove a badge
							if(type.equals("Mann Up") && itemName.startsWith("Operation") && itemName.endsWith("Badge")) {
								tour = itemName.substring("Operation".length(), itemName.indexOf("Badge")).trim();
							}
						}
					}
					if(miniEvent.select("div.tradehistory_items_plusminus").first().text().equals("-")) {
						for(Element itemElement : miniEvent.select("span.history_item_name")) {
							String itemName = itemElement.text();
							String[] styles = itemElement.attr("style").split(":");
							String color = "Undefined";
							for(int i = 0; i < styles.length; i++) {
								if(styles[i].equalsIgnoreCase("color")) {
									color = styles[i+1].trim();
									break;
								}
							}
							event.addItemLost(new Item(itemName, color));
							if(itemName.equals("Mann Co. Store Package") && type.equals("Used")) {
								log.debug(event.toString("\t"));
							}
						}
					}
				}
				if(tour.isEmpty()) {
					events.add(event);
				} else {
					events.add(new MannUpEvent(event, tour));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
			log.error("Null pointer at row: " + nullHandleRow.toString());
		}
		return events;
	}
	
	public static ArrayList<InventoryEvent> readJsonFile(File file) {
		ArrayList<InventoryEvent> events = new ArrayList<InventoryEvent>();
		Gson gson = new Gson();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mma");
		try {
			FileReader reader = new FileReader(file);
			JsonArray inventoryEvents = gson.fromJson(reader, JsonObject.class).getAsJsonArray("InventoryEvents");
			for(JsonElement eventElement : inventoryEvents) {
				InventoryEvent event = new InventoryEvent(
						LocalDateTime.parse(eventElement.getAsJsonObject().get("date").getAsString(), formatter),
						eventElement.getAsJsonObject().get("type").getAsString());
				for(JsonElement itemElement : eventElement.getAsJsonObject().get("itemsGained").getAsJsonArray()) {
					Item item = new Item(
							itemElement.getAsJsonObject().get("name").getAsString(),
							itemElement.getAsJsonObject().get("quality").getAsString(),
							"");
					if(itemElement.getAsJsonObject().has("secondaryQuality")) {
						item.setSecondaryQuality(itemElement.getAsJsonObject().get("secondaryQuality").getAsString());
					}
					event.addItemGained(item);
				}
				for(JsonElement itemElement : eventElement.getAsJsonObject().get("itemsLost").getAsJsonArray()) {
					Item item = new Item(
							itemElement.getAsJsonObject().get("name").getAsString(),
							itemElement.getAsJsonObject().get("quality").getAsString(),
							"");
					if(itemElement.getAsJsonObject().has("secondaryQuality")) {
						item.setSecondaryQuality(itemElement.getAsJsonObject().get("secondaryQuality").getAsString());
					}
					event.addItemLost(item);
				}
				if(eventElement.getAsJsonObject().has("tour")) {
					events.add(new MannUpEvent(event, eventElement.getAsJsonObject().get("tour").getAsString()));
				} else {
					events.add(event);
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return events;
	}
}
