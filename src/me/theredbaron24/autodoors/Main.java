package me.theredbaron24.autodoors;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class Main extends JavaPlugin{

	private ADCommand command = new ADCommand();
	private static BukkitTask task = null;

	static Set<Door> doors = null;
	static boolean needsPerm = false;

	@Override
	public void onEnable(){
		this.getCommand("ad").setExecutor(command);
		this.getCommand("autodoor").setExecutor(command);
		Configuration.init(this);
		TaskHandler.init();
		Bukkit.getPluginManager().registerEvents(new TaskHandler(), this);
		this.getLogger().info("has been enabled");
	}
	
	@Override
	public void onDisable(){
		if(Main.task != null) Main.task.cancel();
		Main.task = null;
		if(Main.doors != null && !Main.doors.isEmpty()){
			for(Door door : Main.doors){
				door.close();
			}
		}
		this.getLogger().info("has been disabled");
	}
}
