package model.inventoryEvents;

import java.util.ArrayList;
import java.util.Stack;

import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ch.qos.logback.classic.Logger;

public class InventoryEventManager {
	
	private static final Logger log = (Logger) LoggerFactory.getLogger(InventoryEventManager.class);
	
	private ArrayList<InventoryEvent> events;
	
	public InventoryEventManager() {
		events = new ArrayList<InventoryEvent>();
	}

	/**
	 * Go through given list and remove any duplicates found in our existing list.
	 * @param events
	 */
	public void addEvents(ArrayList<InventoryEvent> events) {
		if(this.events.isEmpty()) {
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
		for(InventoryEvent event: events) {
			this.events.add(event);
		}
	}
	
	public ArrayList<InventoryEvent> getEvents() {
		return events;
	}

	public void setEvents(ArrayList<InventoryEvent> events) {
		this.events = events;
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
