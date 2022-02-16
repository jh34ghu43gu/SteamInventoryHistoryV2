package main;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
				System.out.println("Successfully updated stats.json");
			}
			
			in.close();
		} catch(IOException e) {
			e.printStackTrace();
			log.error("Error in main: " + e.getMessage());
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
