package me.theredbaron24.autodoors;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public class Door {

	//Material : data
	private String title = null;
	private Map<Location, String> locations = null;
	private Location loc = null;
	private double distance = 0;
	private double height = 0;
	private boolean isOpen = false;
	private boolean needsPerm = false;
	private Sound sound = null;
	private float pitch = 0f;
	private float volume = 0f;
	
	public Door(String title, Location location, Map<Location, String> locations, double height, double distance, Sound sound, float pitch, float volume, boolean needsPerm){
		this.title = title;
		this.loc = location;
		this.locations = locations;
		this.height = height;
		this.distance = distance;
		this.sound = sound;
		this.pitch = pitch;
		this.volume = volume;
		this.needsPerm = needsPerm;
	}
	
	public void open(){
		if(isOpen) return;
		isOpen = true;
		for(Location location : locations.keySet()){
			Block block = location.getBlock();
			block.setType(Material.AIR);
		}
		if(sound != null){
			loc.getWorld().playSound(loc, sound, volume, pitch);
		}
	}
	@SuppressWarnings("deprecation")
	public void close(){
		if(isOpen == false) return;
		isOpen = false;
		for(Location location : locations.keySet()){
			Block block = location.getBlock();
			String[] str = locations.get(location).split(" : ");
			Material mat = Material.valueOf(str[0].toUpperCase());
			byte b = Byte.parseByte(str[1]);
			block.setType(mat);
			block.setData(b);
			BlockState state = block.getState();
			state.update();
		}
	}
	public Location getLoc(){
		return loc;
	}
	
	public double getDistance(){
		return distance;
	}
	public double getHeight(){
		return height;
	}
	public String getTitle(){
		return title;
	} 
	public boolean needsPermission(){
		return needsPerm;
	}
}
