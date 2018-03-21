package me.theredbaron24.autodoors;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class Main extends JavaPlugin{

	private ADCommand command = new ADCommand();
	public static Set<Door> doors = null;
	public static BukkitTask task = null;
	public static boolean needsPerm = false;
	@Override
	public void onEnable(){
		this.getCommand("ad").setExecutor(command);
		this.getCommand("autodoor").setExecutor(command);
		Configuration.init(this);
		TaskHandler.init(this);
		Bukkit.getPluginManager().registerEvents(new TaskHandler(), this);
		this.getLogger().info("has been enabled");
	}
	
	@Override
	public void onDisable(){
		if(Main.task != null) Main.task.cancel();
		Main.task = null;
		if(Main.doors != null && Main.doors.isEmpty() == false){
			for(Door door : Main.doors){
				door.close();
			}
		}
		this.getLogger().info("has been disabled");
	}
}
