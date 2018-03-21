package me.theredbaron24.autodoors;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Configuration {
	
	private static Main main = null;
	private static FileConfiguration config = null;
	private static File configFile = null;

	public static void init(Main main){
		Configuration.main = main;
		/*configFile = loadResource(main, "config.yml");
		config = YamlConfiguration.loadConfiguration(configFile);
		main.saveDefaultConfig();*/
		if(getConfig().getBoolean("permForDoors") == false){
			Main.needsPerm = false;
			getConfig().set("permForDoors", false);
			saveConfig();
		}else{
			Main.needsPerm = true;
		}
	}
	
	public static FileConfiguration getConfig(){
		if(config == null){
			reloadConfig();
		}
		return config;
	}
	
	public static void reloadConfig(){
		if (configFile == null) {
			configFile = new File(main.getDataFolder(), "config.yml");
		}
		config = YamlConfiguration.loadConfiguration(configFile);
	}
	
	public static void saveConfig(){
		if (config == null || configFile == null) {
			return;
		}
		try {
			config.save(configFile);
		} catch (IOException e) {
			main.getLogger().severe("Could not save config");
		}
	}
}
