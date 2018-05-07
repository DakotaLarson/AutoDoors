package me.theredbaron24.autodoors;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

class Configuration {
	
	private static Main main = null;
	private static FileConfiguration config = null;
	private static File configFile = null;

	static void init(Main main){
		Configuration.main = main;
		if(!getConfig().getBoolean("permForDoors")){
			Main.needsPerm = false;
			getConfig().set("permForDoors", false);
			saveConfig();
		}else{
			Main.needsPerm = true;
		}
	}
	
	static FileConfiguration getConfig(){
		if(config == null){
			reloadConfig();
		}
		return config;
	}
	
	private static void reloadConfig(){
		if (configFile == null) {
			configFile = new File(main.getDataFolder(), "config.yml");
		}
		config = YamlConfiguration.loadConfiguration(configFile);
	}

	static void saveConfig(){
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
