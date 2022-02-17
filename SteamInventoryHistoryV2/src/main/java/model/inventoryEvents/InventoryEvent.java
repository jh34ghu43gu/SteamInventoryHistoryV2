package model.inventoryEvents;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import model.Item;

public class InventoryEvent {
	
	protected LocalDateTime date;
	protected String type;
	protected ArrayList<Item> itemsGained;
	protected ArrayList<Item> itemsLost;
	
	public InventoryEvent(LocalDateTime date, String type) {
		this.date = date;
		this.type = type;
		itemsGained = new ArrayList<Item>();
		itemsLost = new ArrayList<Item>();
	}
	
	/**
	 * Compares all attributes to another InventoryEvent. 
	 * Returns true if everything is the same including the order of items in gained/lost
	 * @param e2
	 * @return
	 */
	public boolean compare(InventoryEvent e2) {
		boolean same = true;
		if(!date.equals(e2.getDate())) {
			same = false;
		} else if(!type.equals(e2.getType())) {
			same = false;
		} else if(itemsGained.size() != e2.getItemsGained().size()) {
			same = false;
		} else if(itemsLost.size() != e2.getItemsLost().size()) {
			same = false;
		} else {
			//Quick checks passed, cycle item lists
			int i = 0;
			for(Item item : itemsGained) {
				if(!item.compare(e2.getItemsGained().get(i))) { //Items should be in the same order
					same = false;
					break;
				}
				i++;
			}
			if(same) { //Don't loop if gained already triggered a false
				i = 0;
				for(Item item : itemsLost) {
					if(!item.compare(e2.getItemsLost().get(i))) { //Items should be in the same order
						same = false;
						break;
					}
					i++;
				}
			}
		}
		return same;
	}
	
	public LocalDateTime getDate() {
		return date;
	}
	
	public void setDate(LocalDateTime date) {
		this.date = date;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ArrayList<Item> getItemsGained() {
		return itemsGained;
	}
	
	public void setItemsGained(ArrayList<Item> itemsGained) {
		this.itemsGained = itemsGained;
	}
	
	public void addItemGained(Item item) {
		itemsGained.add(item);
	}
	
	public ArrayList<Item> getItemsLost() {
		return itemsLost;
	}
	
	public void setItemsLost(ArrayList<Item> itemsLost) {
		this.itemsLost = itemsLost;
	}
	
	public void addItemLost(Item item) {
		itemsLost.add(item);
	}
	
	public String toString(String tabs) {
		String out = tabs + "Type: " + type + "\n"
				+ tabs + "Date: " + date.format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mma")) + "\n"
				+ tabs + "Items Gained: " + itemsGained.size() + " Items Lost: " + itemsLost.size() + "\n";
		for(Item i : itemsGained) {
			out += tabs + "\tGained: " + i.toString("\t") + "\n";
		}
		for(Item i : itemsLost) {
			out += tabs + "\tLost: " + i.toString("\t") + "\n";
		}
		
		return out;
	}
	
	public JsonObject toJson() {
		JsonObject event = new JsonObject();
		event.addProperty("date", date.format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mma")));
		event.addProperty("type", type);
		JsonArray gain = new JsonArray();
		for(Item i : itemsGained) {
			gain.add(i.toJson());
		}
		JsonArray loss = new JsonArray();
		for(Item i : itemsLost) {
			loss.add(i.toJson());
		}
		event.add("itemsGained", gain);
		event.add("itemsLost", loss);
		return event;
	}
}

