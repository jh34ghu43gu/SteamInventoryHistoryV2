package model.inventoryEvents;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import model.Item;

public class MannUpEvent extends InventoryEvent{

	private String tour;
	
	public MannUpEvent(LocalDateTime date) {
		super(date, "Mann Up");
		tour = "Unknown";
	}
	
	public MannUpEvent(LocalDateTime date, String tour) {
		super(date, "Mann Up");
		this.tour = tour;
	}

	public MannUpEvent(InventoryEvent IE, String tour) {
		super(IE.getDate(), "Mann Up");
		this.tour = tour;
		this.itemsGained = IE.getItemsGained();
		this.itemsLost = IE.getItemsLost();
	}
	
	public String getTour() {
		return tour;
	}

	public void setTour(String tour) {
		this.tour = tour;
	}
	
	@Override
	public String toString(String tabs) {
		String out = tabs + "Type: " + type + " Tour: " + tour + "\n"
				+ tabs + "Date: " + date.format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mma")) + "\n"
				+ tabs + "Items Gained: " + itemsGained.size() + " Items Lost: " + itemsLost.size() + "\n";
		for(Item i : itemsGained) {
			out += tabs + "\tGained: " + i.toString("\t");
		}
		for(Item i : itemsLost) {
			out += tabs + "\tLost: " + i.toString("\t");
		}
		
		return out;
	}
	
	@Override
	public JsonObject toJson() {
		JsonObject event = new JsonObject();
		event.addProperty("date", date.format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mma")));
		event.addProperty("type", type);
		event.addProperty("tour", tour);
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
