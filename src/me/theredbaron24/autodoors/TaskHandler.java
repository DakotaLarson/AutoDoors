package me.theredbaron24.autodoors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class TaskHandler implements Listener {

	private static Map<UUID, Door> players = new HashMap<UUID, Door>();
	
	public static void init(Main main){
		Main.doors = new HashSet<Door>();
		ConfigurationSection superSection = Configuration.getConfig().getConfigurationSection("doors");
		if(superSection != null){
			Set<String> issues = new HashSet<String>();
			Set<String> doors = superSection.getKeys(false);
			for(String title : doors){
				ConfigurationSection section = Configuration.getConfig().getConfigurationSection("doors." + title);
				if(section.getBoolean("enabled")){
					String locString = section.getString("mainLoc");
					if(locString != null){
						List<String> locs = section.getStringList("locations");
						if(locs == null || locs.isEmpty()){
							Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + title + ChatColor.YELLOW + " has no other locations.");
						}else{
							double distance = section.getDouble("distance");
							double height = section.getDouble("height");
							if(height <= 0 || distance <= 0){
								Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + title + ChatColor.YELLOW + " has incorrect distance or height.");
							}else{
								String[] locArgs = locString.split(" : ");
								World world = Bukkit.getWorld(locArgs[0]);
								double x = Double.parseDouble(locArgs[1]);
								double y = Double.parseDouble(locArgs[2]);
								double z = Double.parseDouble(locArgs[3]);
								Location mainLoc = new Location(world, x, y, z);
								Map<Location, String> locations = new HashMap<Location, String>();
								for(String s : locs){
									locArgs = s.split(" : ");
									world = Bukkit.getWorld(locArgs[0]);
									x = Double.parseDouble(locArgs[1]);
									y = Double.parseDouble(locArgs[2]);
									z = Double.parseDouble(locArgs[3]);
									Location location = new Location(world, x, y, z);
									Block b = location.getBlock();
									if(b.getType() == Material.AIR){
										issues.add(title);
									}
									@SuppressWarnings("deprecation")
									String str = b.getType().name() + " : " + b.getData();
									locations.put(location, str);
								}
								Sound sound = null;
								float pitch = 0f;
								float volume = 0f;
								if(section.getConfigurationSection("sound") != null){
									try{
										sound = Sound.valueOf(section.getString("sound.sound", "").toUpperCase());
										pitch = (float) section.getDouble("sound.pitch");
										volume = (float) section.getDouble("sound.volume");
									}catch(Exception e){
										Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + title + ChatColor.RED + " was not able to enable the sound set for it.");
									}
								}
								boolean needsPerm = section.getBoolean("needsPermission", false);
								Door d = new Door(title, mainLoc, locations, height, distance, sound, pitch, volume, needsPerm);
								TaskHandler.addDoor(d);
							}
						}
					}else{
						Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + title + ChatColor.YELLOW  + " has no main location.");
					}
					
				}
			}
			if(issues.isEmpty()){
				if(doors.isEmpty() == false){
					Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "All Doors were enabled successfully!");
				}
			}else{
				Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "The following doors may not have closed, and may be corrupt");
				for(String issue : issues){
					Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "- " + issue);
				}
			}
		}
	}
	public static void addDoor(Door door){
		Main.doors.add(door);
	}
	public static boolean removeDoor(Door door){
		if(Main.doors.contains(door) == false) return false;
		Main.doors.remove(door);
		return true;
	}
	public static Door getDoor(String title){
		if(Main.doors == null) return null;
		for(Door door : Main.doors){
			if(door.getTitle().equals(title)){
				return door;
			}
		}
		return null;
	}
	private static double flatDistance(Location loc1, Location loc2){
		double xDiff = Math.abs(loc1.getX() - loc2.getX());
		double zDiff = Math.abs(loc1.getZ() - loc2.getZ());
		return Math.sqrt(Math.pow(xDiff, 2) + Math.pow(zDiff, 2));
	}
	
	private static boolean locationHasChanged(Location loc1, Location loc2){
		return(loc1.getX() != loc2.getX() || loc1.getZ() != loc2.getZ());
	}
	
	private static boolean isNear(Location playerLoc, Location doorLoc, double distance, double height){
		return (playerLoc.getWorld().getName().equals(doorLoc.getWorld().getName()) && Math.abs(doorLoc.getY() - playerLoc.getY()) <= height) 
				&& (Math.abs(doorLoc.getX() - playerLoc.getX()) < distance) && (Math.abs(doorLoc.getZ() - playerLoc.getZ()) < distance);
	}
	@EventHandler
	public void onMove(PlayerMoveEvent event){
		if(locationHasChanged(event.getFrom(), event.getTo())){
			boolean removePlayer = true;
			for(Door door : Main.doors){
				if(isNear(event.getTo(), door.getLoc(), door.getDistance(), door.getHeight())){
					if(door.needsPermission()){
						if(event.getPlayer().isOp() || event.getPlayer().hasPermission("autodoor." + door.getTitle())){
							if(flatDistance(door.getLoc(), event.getTo()) <= door.getDistance()){
								players.put(event.getPlayer().getUniqueId(), door);
								removePlayer = false;
							}
						}
					}else{
						if(flatDistance(door.getLoc(), event.getTo()) <= door.getDistance()){
							players.put(event.getPlayer().getUniqueId(), door);
							removePlayer = false;
						}
					}
					break;
				}		
			}
			if(removePlayer){
				if(players.containsKey(event.getPlayer().getUniqueId())){
					players.remove(event.getPlayer().getUniqueId());
				}
			}
			for(Door door : Main.doors){
				if(players.values().contains(door)){
					door.open();
				}else{
					door.close();
				}
			}
		}
	}
	@EventHandler
	public void onQuit(PlayerQuitEvent event){
		UUID id = event.getPlayer().getUniqueId();
		if(players.containsKey(id)){
			Door door = players.get(id);
			players.remove(id);
			if(players.values().contains(door) == false){
				door.close();
			}
		}
	}
	@EventHandler
	public void onClick(PlayerInteractEvent event){
		if(event.hasBlock() && event.hasItem()){
			ItemStack item = event.getItem();
			if(item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().startsWith(ChatColor.AQUA + "Wand for Door: ")){
				if(event.getAction() == Action.LEFT_CLICK_BLOCK){
					String doorTitle = ChatColor.stripColor(item.getItemMeta().getDisplayName()).split(": ")[1];
					event.getPlayer().performCommand("ad add " + doorTitle);
					event.setCancelled(true);
				}else if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
					String doorTitle = ChatColor.stripColor(item.getItemMeta().getDisplayName()).split(": ")[1];
					event.getPlayer().performCommand("ad remove " + doorTitle);
					event.setCancelled(true);
				}
			}
		}
	}
}
