package me.theredbaron24.autodoors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ADCommand implements CommandExecutor{

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)){
			sender.sendMessage("You must be in game to use this.");
			return false;
		}
		Player player = (Player) sender;
		if(Main.needsPerm && !(player.hasPermission("autodoor.command") || player.isOp())){
			player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
			return false;
		}
		if(args.length == 0){
			player.sendMessage(ChatColor.GREEN + "-----" + ChatColor.DARK_AQUA + "AutoDoor Commands" + ChatColor.GREEN + "-----");
			player.sendMessage(ChatColor.YELLOW + "/ad createdoor <door> <height> <distance>" + ChatColor.AQUA + " creates a new door with the block the player is looking at");
			player.sendMessage(ChatColor.YELLOW + "/ad removedoor <door>" + ChatColor.AQUA + " removes a door");
			player.sendMessage(ChatColor.YELLOW + "/ad wand <door>" + ChatColor.AQUA + " gives a wand that can be used to add and remove blocks to a door.");
			player.sendMessage(ChatColor.YELLOW + "/ad add <door>" + ChatColor.AQUA + " adds a block to a door");
			player.sendMessage(ChatColor.YELLOW + "/ad remove <door>" + ChatColor.AQUA + " removes a block from a door");
			player.sendMessage(ChatColor.YELLOW + "/ad height <height> <door>" + ChatColor.AQUA + " sets the height for a door");
			player.sendMessage(ChatColor.YELLOW + "/ad distance <distance> <door>" + ChatColor.AQUA + " sets the distance for a door");
			player.sendMessage(ChatColor.YELLOW + "/ad sound <remove|sound> [volume] [pitch] <door>" + ChatColor.AQUA + " sets the sound for a door");
			player.sendMessage(ChatColor.YELLOW + "/ad permission <enable:disable> <door>" + ChatColor.AQUA + " sets if permission is needed for a door");
			player.sendMessage(ChatColor.YELLOW + "/ad enable <door>" + ChatColor.AQUA + " enables a door");
			player.sendMessage(ChatColor.YELLOW + "/ad disable <door>" + ChatColor.AQUA + " disables a door");
			player.sendMessage(ChatColor.YELLOW + "/ad list" + ChatColor.AQUA + " lists the doors that are on the server");

		}else if(args.length == 2){
			if(args[0].equalsIgnoreCase("removedoor")){
				removeDoor(player, args[1]);
			}else if(args[0].equalsIgnoreCase("add")){
				addLoc(player, getTarget(player), args[1]);
			}else if(args[0].equalsIgnoreCase("remove")){
				removeLoc(player, getTarget(player), args[1]);
			}else if(args[0].equalsIgnoreCase("enable")){
				enableDoor(player, args[1]);
			}else if(args[0].equalsIgnoreCase("disable")){
				disableDoor(player, args[1]);
			}else if(args[0].equalsIgnoreCase("wand")){
				giveWand(player, args[1]);
			}else{
				sender.sendMessage(ChatColor.YELLOW + "Command syntax not recognized.");
			}
		}else if(args.length == 4){
			if(args[0].equalsIgnoreCase("createdoor")){
				Location location = getTarget(player);
				double distance = 0;
				double height = 0;
				try{
					distance = Double.parseDouble(args[3]);
					height = Double.parseDouble(args[2]);
				}catch(NumberFormatException e){
					sender.sendMessage(ChatColor.YELLOW + "Please use numbers for the height and distance.");
					return false;
				}
				if(distance <= 0 || height <= 0){
					sender.sendMessage(ChatColor.YELLOW + "Please use positive values for the height and distance.");
					return false;
				}
				createDoor(location, args[1], player, height, distance);
			}else{
				sender.sendMessage(ChatColor.YELLOW + "Command syntax not recognized.");

			}
		}else if(args.length == 1){
			if(args[0].equalsIgnoreCase("list")){
				ConfigurationSection section = Configuration.getConfig().getConfigurationSection("doors");
				if(section == null){
					player.sendMessage(ChatColor.YELLOW + "There are no doors.");
				}else{
					Set<String> doors = Configuration.getConfig().getConfigurationSection("doors").getKeys(false);
					if(doors == null || doors.isEmpty()){
						player.sendMessage(ChatColor.YELLOW + "There are no doors.");
						return false;
					}
					player.sendMessage(ChatColor.DARK_AQUA + "AutoDoors (" + ChatColor.GREEN + "Enabled" + ChatColor.DARK_AQUA + " | " + ChatColor.GRAY + "Disabled" + ChatColor.DARK_AQUA + ")");
					for(String door : doors){
						if(Configuration.getConfig().getBoolean("doors." + door + ".enabled")){
							player.sendMessage(ChatColor.DARK_AQUA + "- " + ChatColor.GREEN + door);
						}else{
							player.sendMessage(ChatColor.DARK_AQUA + "- " + ChatColor.GRAY + door);
						}
					}	
				}
			}else{
				sender.sendMessage(ChatColor.YELLOW + "Command syntax not recognized.");
			}
		}else if(args.length == 3){
			if(args[0].equalsIgnoreCase("height")){
				setMeasurement(player, args[1], true, args[2]);
			}else if(args[0].equalsIgnoreCase("distance")){
				setMeasurement(player, args[1], false, args[2]);
			}else if(args[0].equalsIgnoreCase("sound") && args[1].equalsIgnoreCase("remove")){
				removeSound(player, args[2]);
			}else if(args[0].equalsIgnoreCase("permission") && (args[1].equalsIgnoreCase("enable") || args[1].equalsIgnoreCase("disable"))){
				setPermission(args[2], args[1].equalsIgnoreCase("enable"), player);
			}
			else{
				sender.sendMessage(ChatColor.YELLOW + "Command syntax not recognized.");
			}
		}else if(args.length == 5){
			if(args[0].equalsIgnoreCase("sound")){
				setSound(args[1], args[2], args[3], args[4], player);
			}else{
				sender.sendMessage(ChatColor.YELLOW + "Command syntax not recognized.");
			}
		}else{
			sender.sendMessage(ChatColor.YELLOW + "Command syntax not recognized.");
		}
		return false;
	}
	
	private void createDoor(Location location, String title, Player player, double height, double distance){
		ConfigurationSection section = Configuration.getConfig().getConfigurationSection("doors." + title);
		if(section != null){
			player.sendMessage(ChatColor.YELLOW + title + ChatColor.RED + " already exists.");
		}else{
			if(location.getBlock().getType() == Material.AIR){
				player.sendMessage(ChatColor.YELLOW + "Please look at a block.");
			}else{
				String locString = location.getWorld().getName()+ " : " + location.getX() + " : " + location.getY() + " : " + location.getZ();
				Configuration.getConfig().set("doors." + title + ".mainLoc", locString);
				Configuration.getConfig().set("doors." + title + ".height", height);
				Configuration.getConfig().set("doors." + title + ".distance", distance);
				Configuration.getConfig().set("doors." + title + ".enabled", false);
				Configuration.saveConfig();
				player.sendMessage(ChatColor.YELLOW + title + ChatColor.GREEN + " was successfully created!");
			}
		}
	}
	private void removeDoor(Player player, String title){
		if(Configuration.getConfig().get("doors." + title) == null){
			player.sendMessage(ChatColor.AQUA + title + ChatColor.YELLOW + " does not exist.");
			return;
		}
		Door d = TaskHandler.getDoor(title);
		if(d != null){
			d.close();
			TaskHandler.removeDoor(d);
		}
		Configuration.getConfig().set("doors." + title, null);
		Configuration.saveConfig();
		player.sendMessage(ChatColor.AQUA + title + ChatColor.GREEN + " was successfully removed.");
	}
	private void addLoc(Player player, Location location, String title){
		ConfigurationSection section = Configuration.getConfig().getConfigurationSection("doors." + title);
		if(section == null){
			player.sendMessage(ChatColor.AQUA + title + ChatColor.RED + " does not exist.");
			return;
		}else if(section.getBoolean("enabled")){
			player.sendMessage(ChatColor.YELLOW + "Please disable that door first before adding that block.");
			return;
		}
		String locString = location.getWorld().getName()+ " : " + location.getBlockX() + " : " + location.getBlockY() + " : " + location.getBlockZ();
		List<String> locations = section.getStringList("locations");
		if(locations.contains(locString)){
			player.sendMessage(ChatColor.AQUA + title + ChatColor.YELLOW + " already has this block added.");
			return;
		}
		locations.add(locString);
		section.set("locations", locations);
		Configuration.saveConfig();
		player.sendMessage(ChatColor.GREEN + "Location added to " + ChatColor.AQUA + title);
	}
	private void removeLoc(Player player, Location location, String title){
		ConfigurationSection section = Configuration.getConfig().getConfigurationSection("doors." + title);
		if(section == null){
			player.sendMessage(ChatColor.AQUA + title + ChatColor.RED + " does not exist.");
			return;
		}else if(section.getBoolean("enabled")){
			player.sendMessage(ChatColor.YELLOW + "Please disable that door first before removing that block.");
			return;
		}
		String locString = location.getWorld().getName()+ " : " + location.getBlockX() + " : " + location.getBlockY() + " : " + location.getBlockZ();
		List<String> locations = section.getStringList("locations");
		if(!locations.contains(locString)){
			player.sendMessage(ChatColor.AQUA + title + ChatColor.YELLOW + " does not have this block added.");
			return;
		}
		locations.remove(locString);
		section.set("locations", locations);
		Configuration.saveConfig();
		player.sendMessage(ChatColor.GREEN + "Location removed from " + ChatColor.AQUA + title);
	}
	private void enableDoor(Player player, String door){
		ConfigurationSection section = Configuration.getConfig().getConfigurationSection("doors." + door);
		if(section == null){
			player.sendMessage(ChatColor.AQUA + door + ChatColor.YELLOW + " does not exist.");
		}else if(section.getBoolean("enabled")){
			player.sendMessage(ChatColor.AQUA + door + ChatColor.YELLOW + " is already enabled.");
		}else{
			String locString = section.getString("mainLoc");
			if(locString != null){
				List<String> locs = section.getStringList("locations");
				if(locs == null || locs.isEmpty()){
					player.sendMessage(ChatColor.AQUA + door + ChatColor.YELLOW + " has no other locations.");
				}else{
					double distance = section.getDouble("distance");
					double height = section.getDouble("height");
					if(height <= 0 || distance <= 0){
						player.sendMessage(ChatColor.AQUA + door + ChatColor.YELLOW + " has incorrect distance or height.");
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
								Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + door + ChatColor.RED + " was not able to enable the sound set for it.");
							}
						}
						boolean needsPerm = section.getBoolean("needsPermission", false);
						section.set("enabled", true);
						Configuration.saveConfig();
						Door d = new Door(door, mainLoc, locations, height, distance, sound, pitch, volume, needsPerm);
						TaskHandler.addDoor(d);
						player.sendMessage(ChatColor.AQUA + door + ChatColor.GREEN + " was successfully enabled.");
					}
				}
			}else{
				player.sendMessage(ChatColor.AQUA + door + ChatColor.YELLOW  + " has no main location.");
			}
		}
	}
	private void disableDoor(Player player, String door){
		ConfigurationSection section = Configuration.getConfig().getConfigurationSection("doors." + door);
		if(section == null){
			player.sendMessage(ChatColor.AQUA + door + ChatColor.YELLOW + " does not exist.");
		}else if(!section.getBoolean("enabled")){
			player.sendMessage(ChatColor.AQUA + door + ChatColor.YELLOW + " is not enabled.");
		}else{
			Door d = TaskHandler.getDoor(door);
			d.close();
			section.set("enabled", false);
			Configuration.saveConfig();
			if(TaskHandler.removeDoor(d)){
				player.sendMessage(ChatColor.AQUA + door + ChatColor.GREEN + " was successfully disabled.");
			}else{
				player.sendMessage(ChatColor.AQUA + door + ChatColor.GREEN + " was disabled, but was not being used.");
			}
			
		}
	}
	private void setMeasurement(Player player, String measurement, boolean isHeight, String door){
		ConfigurationSection section = Configuration.getConfig().getConfigurationSection("doors." + door);
		if(section == null){
			player.sendMessage(ChatColor.AQUA + door + ChatColor.RED + " does not exist.");
			return;
		}else if(section.getBoolean("enabled")){
			player.sendMessage(ChatColor.YELLOW + "Please disable that door first before changing a value.");
			return;
		}
		double value = 0;
		try{
			value = Double.parseDouble(measurement);
		}catch(NumberFormatException e){
			player.sendMessage(ChatColor.YELLOW + "Please use a number for a value.");
			return;
		}
		if(value <= 0){
			player.sendMessage(ChatColor.YELLOW + "Please use a value that is greater than 0");
			return;
		}
		if(isHeight){
			section.set("height", value);
			Configuration.saveConfig();
			player.sendMessage(ChatColor.AQUA + door + ChatColor.GREEN + " had its height value changed to " + ChatColor.AQUA + value);
		}else{
			section.set("distance", value);
			Configuration.saveConfig();
			player.sendMessage(ChatColor.AQUA + door + ChatColor.GREEN + " had its distance value changed to " + ChatColor.AQUA + value);
		}
	}
	private Location getTarget(Player player){
		HashSet<Material> mats = null;
		Location location = player.getTargetBlock(mats, 20).getLocation();
		location = location.add(0.5, 0.5, 0.5);
		return location;
	}
	private void removeSound(Player player, String door){
		ConfigurationSection section = Configuration.getConfig().getConfigurationSection("doors." + door);
		if(section == null){
			player.sendMessage(ChatColor.AQUA + door + ChatColor.RED + " does not exist.");
			return;
		}else if(section.getConfigurationSection("sound") == null){
			player.sendMessage(ChatColor.YELLOW + "That door does not have a sound associated with it.");
			return;
		}else if(section.getBoolean("enabled")){
			player.sendMessage(ChatColor.YELLOW + "Please disable that door first before changing the sound.");
			return;
		}
		section.set("sound", null);
		Configuration.saveConfig();
		player.sendMessage(ChatColor.YELLOW + "Sound removed for " + ChatColor.AQUA + door);
	}
	private void setSound(String soundStr, String volumeStr, String pitchStr, String door, Player player){
		ConfigurationSection section = Configuration.getConfig().getConfigurationSection("doors." + door);
		if(section == null){
			player.sendMessage(ChatColor.AQUA + door + ChatColor.RED + " does not exist.");
			return;
		}else if(section.getConfigurationSection("sound") != null){
			player.sendMessage(ChatColor.YELLOW + "That door already has a sound associated with it.");
			return;
		}else if(section.getBoolean("enabled")){
			player.sendMessage(ChatColor.YELLOW + "Please disable that door first before changing the sound.");
			return;
		}
		Sound sound = null;
		float pitch = 0;
		float volume = 0;
		try{
			sound = Sound.valueOf(soundStr.toUpperCase());
			pitch = Float.parseFloat(pitchStr);
			volume = Float.parseFloat(volumeStr);
		}catch(NumberFormatException e){
			player.sendMessage(ChatColor.YELLOW + "Please ensure you are using numbers for volume and pitch.");
			return;
		}catch(IllegalArgumentException e){
			player.sendMessage(ChatColor.AQUA + soundStr + ChatColor.YELLOW + " is not a valid sound.");
			return;
		}
		pitch = Math.max(Math.min(pitch, 2.0f), 0f);
		volume = Math.max(Math.min(volume, 2.0f), 0f);
		section.set("sound.sound", sound.name());
		section.set("sound.volume", volume);
		section.set("sound.pitch", pitch);
		Configuration.saveConfig();
		player.sendMessage(ChatColor.GREEN + "Sound set for " + ChatColor.AQUA + door);
	}
	private void setPermission(String door, boolean permission, Player player){
		ConfigurationSection section = Configuration.getConfig().getConfigurationSection("doors." + door);
		if(section == null){
			player.sendMessage(ChatColor.AQUA + door + ChatColor.RED + " does not exist.");
			return;
		}else if(section.getBoolean("enabled")){
			player.sendMessage(ChatColor.YELLOW + "Please disable that door first before changing the door permission.");
			return;
		}else{
			section.set("needsPermission", permission);
			Configuration.saveConfig();
			if(permission){
				player.sendMessage(ChatColor.AQUA + "Permission for door: " + ChatColor.YELLOW + door + ChatColor.AQUA + " set to: " + ChatColor.GREEN + "True");
			}else{
				player.sendMessage(ChatColor.AQUA + "Permission for door: " + ChatColor.YELLOW + door + ChatColor.AQUA + " set to: " + ChatColor.GREEN + "False");
			}
		}
	}
	private void giveWand(Player player, String door){
		ConfigurationSection section = Configuration.getConfig().getConfigurationSection("doors." + door);
		if(section == null){
			player.sendMessage(ChatColor.AQUA + door + ChatColor.RED + " does not exist.");
			return;
		}else if(section.getBoolean("enabled")){
			player.sendMessage(ChatColor.YELLOW + "Please disable that door first before granting yourself a wand.");
			return;
		}else{
			ItemStack item = new ItemStack(Material.STICK);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(ChatColor.AQUA + "Wand for Door: " + ChatColor.YELLOW + door);
			item.setItemMeta(meta);
			player.getInventory().addItem(item);
			player.sendMessage(ChatColor.GREEN + "Wand received for door: " + ChatColor.AQUA + door);
			player.sendMessage(ChatColor.AQUA + "Left Click: " + ChatColor.YELLOW + "Adds a block.");
			player.sendMessage(ChatColor.AQUA + "Right Click: " + ChatColor.YELLOW + "Removes a block.");
		}
	}
}
