package main;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ch.qos.logback.classic.Logger;
import model.inventoryEvents.InventoryEventManager;

public class Main {

	private static final Logger log = (Logger) LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args) {
		try {
			Scanner in = new Scanner(System.in);
			
			Path htmlPath = Paths.get("html/");
			Files.createDirectories(htmlPath);
			File htmlFolder = new File("html/");
			File[] htmlFiles = htmlFolder.listFiles();
			
			//Wait for valid files to appear in html folder
			while(htmlFiles.length == 0 || !hasFiles(htmlFiles, ".html")) {
				System.out.println("No html files detected in the 'html/' folder. "
						+ "Please copy your html files and press enter when ready to continue."
						+ "\nEnter 'Q' to quit.");
				if(in.nextLine().equals("Q")) {
					in.close();
					return;
				}
				htmlFiles = htmlFolder.listFiles();
			}
			//Set default option to refresh, if there are json files existing then user can change to view stats if they want
			String option = "refresh";
			if(hasFiles(htmlFiles, ".json")) {
				System.out.println("You appear to be a returning user. You can either:"
						+ "\n[1] Refresh your json files."
						+ "\n[2] View your stats."
						+ "\n[3] Quit.");
				String inStr = in.nextLine();
				while(!inStr.equals("1") && !inStr.equals("2") && !inStr.equals("3")) {
					inStr = in.nextLine();
				}
				if(inStr.equals("2")) {
					option = "view";
				} else if(inStr.equals("3")) {
					in.close();
					return;
				}
			}
			

			InventoryEventManager manager = new InventoryEventManager();
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			if(option.equals("refresh")) {
				System.out.println("Updating your stats.json file...");
				for(File f : htmlFiles) {
					if(f.getName().endsWith(".html")) {
						log.debug("Parsing '" + f.getName() + "'...");
						manager.addEvents(HistoryParser.readHtmlFile(f));
						log.debug("Completed parse of " + f.getName());
					}
				}
				System.out.println("Parsed html files, converting to json and writing...");
				FileWriter writer = new FileWriter("html/stats.json"); //maybe don't magic this file name /shrug
				String s = gson.toJson(manager.toJson());
				writer.write(s);
				writer.flush();
				writer.close();
				System.out.println("Successfully updated stats.json with " + manager.getSize() + " events.");
			}
			
			if(option.equals("view")) {
				manager.addEvents(HistoryParser.readJsonFile(new File("html/stats.json")));
				System.out.println("Loaded your events from stats.json");
			}
			
			
			HashMap<Integer, String> mainOptionsMap = new HashMap<Integer, String>();
			for(int i = 1; i<= manager.getTypes().size(); i++) {
				mainOptionsMap.put(i, manager.getTypes().get(i-1));
			}
			boolean mainLoop = true;
			HashMap<Integer, String> optionsMap = new HashMap<Integer, String>();
			ArrayList<String> optionsList = new ArrayList<String>();
			while(mainLoop) {
				System.out.println("Select which event type you would like details about: ");
				Main.printOptionsMap(mainOptionsMap, "");
				String type = "";
				while(type.isEmpty()) {
					String s = in.nextLine();
					if(Main.isInt(s) && Integer.parseInt(s) <= mainOptionsMap.size()) {
						type = mainOptionsMap.get(Integer.parseInt(s));
					}
				}
				System.out.println("Selected type: " + type + ". Loading options...");
				optionsMap.clear();
				optionsList.clear();
				optionsList = manager.getOptionsForType(type);
				for(int i = 1; i <= optionsList.size(); i++) {
					optionsMap.put(i, optionsList.get(i-1));
				}
				System.out.println("What would you like to view?");
				Main.printOptionsMap(optionsMap, "");
				String choice = "";
				while(choice.isEmpty()) {
					String s = in.nextLine();
					if(Main.isInt(s) && Integer.parseInt(s) <= optionsMap.size()) {
						choice = optionsMap.get(Integer.parseInt(s));
					}
				}
				for(String s : manager.getMannUpStats(choice)) {
					System.out.println(s);
				}
				
				
				System.out.println("Press enter to continue or enter 'Q' to exit.");
				if(in.nextLine().equals("Q")) {
					mainLoop = false;
				}
			}
			in.close();
		} catch(IOException e) {
			e.printStackTrace();
			log.error("Error in main: " + e.getMessage());
		}
	}
	
	private static boolean isInt(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	
	private static void printOptionsMap(HashMap<Integer, String> map, String end) {
		for(int i = 1; i <= map.size(); i++) {
			System.out.println("[" + i + "] " + map.get(i) + end);
		}
	}
	
	private static boolean hasFiles(File[] files, String ext) {
		boolean hasHtml = false;
		for(File f : files) {
			if(f.getName().toLowerCase().endsWith(ext)) {
				hasHtml = true;
				break;
			}
		}
		return hasHtml;
	}

}
