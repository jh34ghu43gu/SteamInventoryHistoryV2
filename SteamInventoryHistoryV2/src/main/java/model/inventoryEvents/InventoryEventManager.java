package model.inventoryEvents;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Stack;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ch.qos.logback.classic.Logger;
import model.Item;

public class InventoryEventManager {
	
	private static final Logger log = (Logger) LoggerFactory.getLogger(InventoryEventManager.class);
	
	private ArrayList<InventoryEvent> events;
	private ArrayList<String> types;
	private ArrayList<String> tours;
	private ArrayList<String> crates;
	
	public InventoryEventManager() {
		events = new ArrayList<InventoryEvent>();
		types = new ArrayList<String>();
		tours = new ArrayList<String>();
		crates = new ArrayList<String>();
	}

	public int getSize() {
		return events.size();
	}
	
	/**
	 * Go through given list and remove any duplicates found in our existing list.
	 * @param events
	 */
	public void addEvents(ArrayList<InventoryEvent> events) {
		if(this.events.isEmpty()) {
			for(InventoryEvent e : events) {
				if(!types.contains(e.getType())) {
					types.add(e.getType());
				}
				if(tours.size() < 5 && e.getClass().equals(MannUpEvent.class) && !tours.contains(((MannUpEvent) e).getTour())) {
					tours.add(((MannUpEvent) e).getTour());
				}
				if(e.getType().equals("Unbox")) {
					if(e.getItemsLost().get(0).getItemName().toLowerCase().contains("key") 
							&& !e.getItemsLost().get(0).getItemName().toLowerCase().contains("keyless")
							&& !crates.contains(e.getItemsLost().get(1).getItemName())) {
						crates.add(e.getItemsLost().get(1).getItemName());
					} else if(!crates.contains(e.getItemsLost().get(0).getItemName())) {
						crates.add(e.getItemsLost().get(0).getItemName());
					}
				}
			}
			this.events = events;
			return;
		}
		//Get duplicates into a stack
		Stack<Integer> remove = new Stack<Integer>();
		int i = 0;
		for(InventoryEvent event : events) {
			for(InventoryEvent event2 : this.events) {
				if(event.compare(event2)) {
					log.debug("Duplicate InventoryEvent found at location " + i);
					//log.debug(event.toString());
					//log.debug(event.toString("\t"));
					remove.push(i);
					break;
				}
			}
			i++;
		}
		//Remove duplicates from positions high to low
		while(!remove.isEmpty()) {
			i = remove.pop();
			//log.debug("Removing InventoryEvent: " + events.get(i).toString()); //Mirror the previous log messages
			events.remove(i);
		}
		//Merge
		for(InventoryEvent e: events) {
			if(!types.contains(e.getType())) {
				types.add(e.getType());
			}
			if(tours.size() < 5 && e.getClass().equals(MannUpEvent.class) && !tours.contains(((MannUpEvent) e).getTour())) {
				tours.add(((MannUpEvent) e).getTour());
			}
			if(e.getType().equals("Unbox")) {
				if(e.getItemsLost().get(0).getItemName().toLowerCase().contains("key") 
						&& !e.getItemsLost().get(0).getItemName().toLowerCase().contains("keyless")
						&& !crates.contains(e.getItemsLost().get(1).getItemName())) {
					crates.add(e.getItemsLost().get(1).getItemName());
				} else if(!crates.contains(e.getItemsLost().get(0).getItemName())) {
					crates.add(e.getItemsLost().get(0).getItemName());
				}
			}
			this.events.add(e);
		}
	}
	
	public ArrayList<String> getOptionsForType(String type) {
		ArrayList<String> options = new ArrayList<String>();
		options.add("View All (VERY SPAMMY!)");
		//options.add("");
		switch (type) {
		case "Mann Up":
			options.add("Overall stats");
			for(String tour : tours) {
				options.add(tour + " stats");
			}
			break;
		case "Surplus":
			options.add("Overall stats");
			break;
		case "Unbox":
			options.add("Overall stats");
			for(String crate : crates) {
				options.add(crate + " stats");
			}
			break;
		case "Found":
			
		case "Deleted":
			
		case "Used":
			
		case "Store Purchase":
			
		case "Trade Up":
			
		case "Promo":
			
		case "Earned":
			
		case "Transmute":
			
		case "Daily Hat":
			
		case "Open Store Package":
			
		case "Halloween Event":
			
		case "Transmogrified":
			
		default:
			log.debug("Type: " + type + " does not have custom options.");
		}
		return options;
	}
	
	public ArrayList<String> getStats(String type, String option) {
		ArrayList<String> out = new ArrayList<String>();
		switch (type) {
		case "Mann Up":
			return this.getMannUpStats(option);
		case "Surplus":
			return this.getSurplusStats(option);
		case "Unbox":
			return this.getUnboxStats(option);
		case "Found":
			
		case "Deleted":
			
		case "Used":
			
		case "Store Purchase":
			
		case "Trade Up":
			
		case "Promo":
			
		case "Earned":
			
		case "Transmute":
			
		case "Daily Hat":
			
		case "Open Store Package":
			
		case "Halloween Event":
			
		case "Transmogrified":
			
		default:
			log.debug("Type: " + type + " does not have a stats return method!");
		}
		return out;
	}
	
	public ArrayList<String> getMannUpStats(String option) {
		ArrayList<String> out = new ArrayList<String>();
		HashMap<String, Double> items = new HashMap<String, Double>();
		double weapon = 0;
		double roboHat = 0;
		double tool = 0;
		double hat = 0;
		double paint = 0;
		double robro = 0;
		
		double tours = 0;
		double ausTours = 0;
		double os = 0;
		double st = 0;
		double me = 0;
		double tc = 0;
		double gg = 0;
		double rust = 0;
		double blood = 0;
		double silver1 = 0;
		double gold1 = 0;
		double silver2 = 0;
		double gold2 = 0;
		double carb = 0;
		double diamond = 0;
		double ks = 0;
		double spec = 0;
		double prof = 0;
		double aussie = 0;
		double pan = 0;
		
		double pristine = 0;
		double bwPart = 0;
		double rPart = 0;
		//Get tour items and put into map
		for(InventoryEvent event : events) {
			if(event.getClass().equals(MannUpEvent.class)) {
				if(!option.equals("Overall stats") && ((MannUpEvent) event).getTour().equals(option)) {
					continue;
				}
				for(Item item : event.getItemsGained()) {
					if(item.getSpecial().equals("Weapon")) {
						weapon++;
					} else if(item.getSpecial().equals("RoboHat")) {
						roboHat++;
					} else if(item.getSpecial().equals("Tool")) {
						tool++;
					} else if(item.getSpecial().equals("Paint")) {
						paint++;
					} else if(item.getSpecial().equals("Hat")) {
						hat++;
					} else if(item.getSpecial().equals("Robro")) {
						robro++;
					}
					
					String name = item.getItemName();
					if(items.containsKey(name)) {
						items.put(name, items.get(name)+1.0);
					} else {
						items.put(name, 1.0);
					}
					name = name.toLowerCase();
					if(name.contains("rust botkiller")) {
						rust++;
					} else if(name.contains("blood botkiller")) {
						blood++;
					} else if(name.contains("silver botkiller")) {
						if(name.contains("mk.ii")) {
							silver2++;
						} else {
							silver1++;
						}
					} else if(name.contains("gold botkiller")) {
						if(name.contains("mk.ii")) {
							gold2++;
						} else {
							gold1++;
						}
					} else if(name.contains("carbonado botkiller")) {
						carb++;
					} else if(name.contains("diamond botkiller")) {
						diamond++;
					} else if(name.contains("golden")) { //Pan first
						pan++;
					} else if(name.contains("professional")) { //Then prof
						prof++;
					} else if(name.contains("specialized")) { //Spec
						spec++;
					} else if(name.contains("killstreak")) { //Any killstreak left is normal
						ks++;
					} else if(name.contains("reinforced robot")) { 
						rPart++;
					} else if(name.contains("battle-worn robot")) {
						bwPart++;
					} else if(name.contains("pristine robot")) {
						pristine++;
					} else if(name.contains("australium") && !name.contains("gold")) { //Don't count paints
						aussie++;
					}
				}
			}
		}
		double pPercent = 0;
		double bwPercent = 0;
		double rPercent = 0;
		int missions = 0;
		//Tally tours since map contains missions
		for(Entry<String, Double> e : items.entrySet()) {
			if(e.getKey().contains("Oil Spill")) {
				os = e.getValue() / 6.0;
				missions += e.getValue();
			} else if(e.getKey().contains("Steel Trap")) {
				st = e.getValue() / 6.0;
				missions += e.getValue();
			} else if(e.getKey().contains("Mecha Engine")) {
				me = e.getValue() / 3.0;
				missions += e.getValue();
			} else if(e.getKey().contains("Two Cities")) {
				tc = e.getValue() / 4.0;
				missions += e.getValue();
				pPercent = pristine/e.getValue();
				bwPercent = bwPart/e.getValue();
				rPercent = rPart/e.getValue();
			} else if(e.getKey().contains("Gear Grinder")) {
				gg = e.getValue() / 3.0;
				missions += e.getValue();
			}
		}
		
		ausTours = Math.floor(st) + Math.floor(me) + Math.floor(tc) + Math.floor(gg);
		tours = Math.floor(os) + Math.floor(st) + Math.floor(me) + Math.floor(tc) + Math.floor(gg);
		
		DecimalFormat df = new DecimalFormat("#,###.##");
		switch (option) {
		case "Overall stats":
			out.add("Total tours: " + tours + " (Missions: " + missions + ")");
			out.add("Total australium dropping tours: " + ausTours);
			out.add("Total australiums: " + aussie + " (" + df.format((aussie/ausTours)*100.0) + "%)");
			out.add("Total golden frying pans: " + pan + " (" + df.format((pan/ausTours)*100.0) + "%)");
			out.add("Total weapons: " + weapon + " (" + df.format((weapon/missions)*100.0) + "%)");
			out.add("Total robot hats: " + roboHat + " (" + df.format((roboHat/missions)*100.0) + "%)");
			out.add("\tTotal Robro 3000s: " + robro + " (" + df.format((robro/missions)*100.0) + "%)");
			out.add("Total paints: " + paint + " (" + df.format((paint/missions)*100.0) + "%)");
			out.add("Total tools: " + tool + " (" + df.format((tool/missions)*100.0) + "%)");
			out.add("Total regular hats: " + hat + " (" + df.format((hat/missions)*100.0) + "%)");
			out.add("");
			if(os >= 1) {
				out.add("\tTotal Oil Spill Tours: " + df.format(os) + " (" + df.format((Math.floor(os)/tours)*100.0) + "% of total tours)");
				out.add("\t\tTotal rust botkillers: " + rust + " (" + df.format((rust/Math.floor(os))*100.0) + "%)");
				out.add("\t\tTotal blood botkillers: " + blood + " (" + df.format((blood/Math.floor(os))*100.0) + "%)");
			}
			if(st >= 1) {
				out.add("\tTotal Steel Trap Tours: " + df.format(st) + " (" + df.format((Math.floor(st)/tours)*100.0) + "% of total tours)");
				out.add("\t\tTotal silver mk.i botkillers: " + silver1 + " (" + df.format((silver1/Math.floor(st))*100.0) + "%)");
				out.add("\t\tTotal gold mk.i botkillers: " + gold1 + " (" + df.format((gold1/Math.floor(st))*100.0) + "%)");
			}
			if(me >= 1) {
				out.add("\tTotal Mecha Engine Tours: " + df.format(me) + " (" + df.format((Math.floor(me)/tours)*100.0) + "% of total tours)");
				out.add("\t\tTotal silver mk.ii botkillers: " + silver2 + " (" + df.format((silver2/Math.floor(me))*100.0) + "%)");
				out.add("\t\tTotal gold mk.ii botkillers: " + gold2 + " (" + df.format((gold2/Math.floor(me))*100.0) + "%)");
			}
			if(tc >= 1) {
				out.add("\tTotal Two Cities Tours: " + df.format(tc) + " (" + df.format((Math.floor(tc)/tours)*100.0) + "% of total tours)");
				out.add("\t\tTotal killstreak kits: " + ks + " (" + df.format((ks/Math.floor(tc))*100.0) + "%)");
				out.add("\t\tTotal specialized killstreak kit fabricators: " + spec + " (" + df.format((spec/Math.floor(tc))*100.0) + "%)");
				out.add("\t\tTotal professional killstreak kit fabricators: " + prof + " (" + df.format((prof/Math.floor(tc))*100.0) + "%)");
				out.add("\t\tTotal pristine robot parts: " + pristine + " (" + df.format(pPercent*100.0) + "% per mission)");
				out.add("\t\tTotal battle-worn robot parts: " + bwPart + " (" + df.format(bwPercent*100.0) + "% per mission)");
				out.add("\t\tTotal reinforced robot parts: " + rPart + " (" + df.format(rPercent*100.0) + "% per mission)");
			}
			if(gg >= 1) {
				out.add("\tTotal Gear Grinder Tours: " + df.format(gg) + " (" + df.format((Math.floor(gg)/tours)*100.0) + "% of total tours)");
				out.add("\t\tTotal carbonado botkillers: " + carb + " (" + df.format((carb/Math.floor(gg))*100.0) + "%)");
				out.add("\t\tTotal diamond botkillers: " + diamond + " (" + df.format((diamond/Math.floor(gg))*100.0) + "%)");
			}
			return out;
		case "Oil Spill":
			
		case "Steel Trap":
			
		case "Mecha Engine":
			
		case "Two Cities":
			
		case "Gear Grinder":
			
		case "View All (VERY SPAMMY!)":
			for(Entry<String, Double> e : items.entrySet()) {
				out.add("\t" + e.getKey() + ": " +  e.getValue());
			}
			return out;
		}
		return out;
	}
	
	public ArrayList<String> getSurplusStats(String option) {
		ArrayList<String> out = new ArrayList<String>();
		HashMap<String, Double> items = new HashMap<String, Double>();
		double weapon = 0;
		double roboHat = 0;
		double tool = 0;
		double hat = 0;
		double paint = 0;
		double surplusEvents = 0;
		double surplusItems = 0;
		
		for(InventoryEvent event : events) {
			if(event.getType().equals("Surplus")) {
				surplusEvents++;
				for(Item item : event.getItemsGained()) {
					surplusItems++;
					if(item.getSpecial().equals("Weapon")) {
						weapon++;
					} else if(item.getSpecial().equals("RoboHat")) {
						roboHat++;
					} else if(item.getSpecial().equals("Tool")) {
						tool++;
					} else if(item.getSpecial().equals("Paint")) {
						paint++;
					} else if(item.getSpecial().equals("Hat")) {
						hat++;
					}
					
					String name = item.getItemName();
					if(items.containsKey(name)) {
						items.put(name, items.get(name)+1.0);
					} else {
						items.put(name, 1.0);
					}
					name = name.toLowerCase();
					
				}
			}
		}
		
		DecimalFormat df = new DecimalFormat("#,###.##");
		switch (option) {
		case "Overall stats":
			out.add("Total missions with >=1 surplus ticket: " + surplusEvents);
			out.add("Total surplus rewards: " + surplusItems);
			out.add("Total weapons: " + weapon);
			out.add("Total robot hats: " + roboHat);
			out.add("Total paints: " + paint);
			out.add("Total tools: " + tool);
			out.add("Total regular hats: " + hat);
			return out;
		case "View All (VERY SPAMMY!)":
			for(Entry<String, Double> e : items.entrySet()) {
				out.add("\t" + e.getKey() + ": " +  e.getValue());
			}
			return out;
		}
		
		return out;
	}
	
	public ArrayList<String> getUnboxStats(String option) {
		option = option.replace(" stats", "");
		System.out.println(option);
		ArrayList<String> out = new ArrayList<String>();
		LinkedHashMap<String, Double> items = new LinkedHashMap<String, Double>();
		double unboxEvents = 0.0;
		double unboxItems = 0.0;
		double uniqueItems = 0.0;
		double decoratedItems = 0.0;
		double hauntedItems = 0.0;
		double strangeItems = 0.0;
		double unusualItems = 0.0;
		double unusualifiers = 0.0;
		double strangeUnusualItems = 0.0;
		double strangeHauntedItems = 0.0;
		
		ArrayList<Item> sortList = new ArrayList<Item>();
		
		for(InventoryEvent event : events) {
			if(event.getType().equals("Unbox")) {
				if(!option.equals("Overall")) {
					boolean matchedCrate = false;
					for(Item item : event.getItemsLost()) {
						if(item.getItemName().equals(option)) {
							matchedCrate = true;
							break;
						}
					}
					if(!matchedCrate) {
						continue;
					}
				}
				//Correct crate
				unboxEvents++;
				for(Item item: event.getItemsGained()) {
					unboxItems++;
					//Count qualities
					switch(item.getQuality()) {
					case "Unique":
						uniqueItems++;
						break;
					case "Strange":
						strangeItems++;
						break;
					case "Decorated Weapon":
						decoratedItems++;
						break;
					case "Haunted":
						hauntedItems++;
						break;
					case "Unusual":
						unusualItems++;
						break;
					}
					if(item.getItemName().contains("Unusualifier")) {
						unusualifiers++;
					}
					//Count secondaries
					if(item.getSecondaryQuality().equals("Strange")) {
						if(item.getQuality().equals("Unusual")) {
							strangeUnusualItems++;
						} else if(item.getQuality().equals("Haunted")) {
							strangeHauntedItems++;
						}
					}
					sortList.add(item);
				}
			}
		}
		
		Item itemSorter = new Item("", "#7D6D00"); //need color to stop a warning.
		sortList.sort(itemSorter);
		for(Item item : sortList) {
			String name = item.getItemName();
			if(items.containsKey(name)) {
				items.put(name, items.get(name)+1.0);
			} else {
				items.put(name, 1.0);
			}
		}
		
		DecimalFormat df = new DecimalFormat("#,###.##");
		for(Entry<String, Double> e : items.entrySet()) {
			out.add("\t" + e.getKey() + ": " +  e.getValue() + " (" + df.format(e.getValue()/unboxEvents*100.0) + "%)");
		}
		out.add("");
		out.add("Total crates unboxed: " + unboxEvents);
		out.add("Total items unboxed: " + unboxItems + " (" + df.format(unboxItems/unboxEvents*100.0) + "%)");
		if(decoratedItems > 0) {
			out.add("Total decorated items unboxed: " + decoratedItems + " (" + df.format(decoratedItems/unboxEvents*100.0) + "%)");
		}
		if(uniqueItems > 0) {
			out.add("Total unique items unboxed: " + uniqueItems + " (" + df.format(uniqueItems/unboxEvents*100.0) + "%)");
		}
		if(strangeItems > 0) {
			out.add("Total strange items unboxed: " + strangeItems + " (" + df.format(strangeItems/unboxEvents*100.0) + "%)");
		}
		if(hauntedItems > 0) {
			out.add("Total haunted items unboxed: " + hauntedItems + " (" + df.format(hauntedItems/unboxEvents*100.0) + "%)");
		}
		if(strangeHauntedItems > 0) {
			out.add("\tTotal strange haunted items unboxed: " + strangeHauntedItems + " (" + df.format(strangeHauntedItems/unboxEvents*100.0) + "%)");
		}
		if(unusualItems > 0) {
			out.add("Total unusual items unboxed: " + unusualItems + " (" + df.format(unusualItems/unboxEvents*100.0) + "%)");
		}
		if(unusualifiers > 0) {
			out.add("\tTotal non-unusualifier unusual items unboxed: " + (unusualItems-unusualifiers) + " (" + df.format((unusualItems-unusualifiers)/unboxEvents*100.0) + "%)");
			out.add("\tTotal unusualifiers unboxed: " + unusualifiers + " (" + df.format(unusualifiers/unboxEvents*100.0) + "%)");
		}
		if(strangeUnusualItems > 0) {
			out.add("\tTotal strange unusual items unboxed: " + strangeUnusualItems + " (" + df.format(strangeUnusualItems/unboxEvents*100.0) + "%)");
		}
		
		return out;
	}
	
	public void sortEvents() {
		long startTime = System.currentTimeMillis();
		System.out.println("Begin sorting events.");
		System.out.println("Sorting items...");
		double progress = 0.0;
		int lastProgress = 0;
		for(InventoryEvent event : events) {
			progress++;
			double percent = (progress/(double)events.size())*100.0;
			if(Math.floor(percent) > lastProgress) { 
				lastProgress = (int) Math.floor(percent);
				if(lastProgress % 5 == 0) {
					System.out.print(lastProgress + "%");
					if(lastProgress == 100) {
						System.out.println();
					}
				} else {
					System.out.print(".");
				}
			}
			event.sortItems();
		}
		
		System.out.println("Finished item sorting, sorting events...");
		InventoryEvent eventSorter = new InventoryEvent(null, null);
		events.sort(eventSorter);
		System.out.println("Sorting finished.");
		DecimalFormat df = new DecimalFormat("#,###");
		log.debug("Sorting InventoryEvents took: " + df.format((System.currentTimeMillis()-startTime)) + "ms");
	}
	
	public ArrayList<InventoryEvent> getEvents() {
		return events;
	}

	public void setEvents(ArrayList<InventoryEvent> events) {
		this.events = events;
	}
	
	public ArrayList<String> getTypes() {
		return types;
	}

	public void setTypes(ArrayList<String> types) {
		this.types = types;
	}

	public JsonObject toJson() {
		JsonObject events = new JsonObject();
		JsonArray eventArr = new JsonArray();
		for(InventoryEvent e : this.events) {
			eventArr.add(e.toJson());
		}
		events.add("InventoryEvents", eventArr);
		return events;
	}

	public String exportToSpreadsheet() {
		String fileName = "events.csv";
		this.sortEvents();
		try {
			BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName));

            CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
            printer.printRecord("Event Type", "Date", "Items Gained", "Items Lost");
            for(InventoryEvent event : events) {
            	printer.printRecord(event.getType(), event.getDate().toString(), event.getItemsGainedString(), event.getItemsLostString());
            }
            printer.close();
		} catch(Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			return "An error occured creating csv.";
		}
		
		
		return "Events written to: " + fileName;
	}
}
