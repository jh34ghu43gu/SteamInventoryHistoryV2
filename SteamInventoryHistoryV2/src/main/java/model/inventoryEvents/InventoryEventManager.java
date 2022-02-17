package model.inventoryEvents;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Stack;

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
	
	public ArrayList<String> getMannUpStats(String option) {
		ArrayList<String> out = new ArrayList<String>();
		HashMap<String, Double> items = new HashMap<String, Double>();
		double weapon = 0;
		double roboHat = 0;
		double tool = 0;
		
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
		//Tally tours since map contains missions
		for(Entry<String, Double> e : items.entrySet()) {
			if(e.getKey().contains("Oil Spill")) {
				os = e.getValue() / 6.0;
			} else if(e.getKey().contains("Steel Trap")) {
				st = e.getValue() / 6.0;
			} else if(e.getKey().contains("Mecha Engine")) {
				me = e.getValue() / 3.0;
			} else if(e.getKey().contains("Two Cities")) {
				tc = e.getValue() / 4.0;
				pPercent = pristine/e.getValue();
				bwPercent = bwPart/e.getValue();
				rPercent = rPart/e.getValue();
			} else if(e.getKey().contains("Gear Grinder")) {
				gg = e.getValue() / 3.0;
			}
		}
		
		ausTours = Math.floor(st) + Math.floor(me) + Math.floor(tc) + Math.floor(gg);
		tours = Math.floor(os) + Math.floor(st) + Math.floor(me) + Math.floor(tc) + Math.floor(gg);
		
		DecimalFormat df = new DecimalFormat("#,###.##");
		switch (option) {
		case "Overall stats":
			out.add("Total tours: " + tours);
			out.add("Total australium dropping tours: " + ausTours);
			out.add("Total australiums: " + aussie + " (" + df.format((aussie/ausTours)*100.0) + "%)");
			out.add("Total golden frying pans: " + pan + " (" + df.format((pan/ausTours)*100.0) + "%)");
			//TODO weapons etc...
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
		case "Oil Spill":
			
		case "Steel Trap":
			
		case "Mecha Engine":
			
		case "Two Cities":
			
		case "Gear Grinder":
			
		}
		return out;
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

}
