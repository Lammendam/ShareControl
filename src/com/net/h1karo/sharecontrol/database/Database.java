/*******************************************************************************
 * Copyright (C) 2016 H1KaRo (h1karo)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package com.net.h1karo.sharecontrol.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;

import com.net.h1karo.sharecontrol.ShareControl;
import com.net.h1karo.sharecontrol.configuration.Configuration;
import com.net.h1karo.sharecontrol.listeners.multiinventories.PlayerGameModeChangeListener;

public class Database {
	
	private static ShareControl main;
	
	public Database(ShareControl h)
	{
		Database.main = h;
	}
	
    /** CACHE **/
	
    static HashMap<List<Integer>, Integer> cache = new HashMap<List<Integer>, Integer>();
    static HashMap<List<Integer>, Integer> extracache = new HashMap<List<Integer>, Integer>();
    static HashMap<List<Integer>, Integer> fullcache = new HashMap<List<Integer>, Integer>();
    static boolean saveStatus = false;
    static BukkitTask AsyncSave = null;
    static BukkitTask AsyncSaveInv = null;
	
	public static void saveDatabase() {
		saveStatus = true;
		SQLSave();
		cache.clear();
		cache.putAll(extracache);
		extracache.clear();
		saveStatus = false;
	}
	
	public static void SyncSaveDatabase() {
		saveDatabase();
	}
	
	public static void AsyncSaveDatabase() {
		AsyncSave = Bukkit.getServer().getScheduler().runTaskAsynchronously(main,  new Runnable() {
            @Override
            public void run() {
            	if(cache.size() != 0)
            		saveDatabase();
            }
		});
	}
	
	
	
	
	public static void AsyncSaveInvSave() {
		AsyncSaveInv = Bukkit.getServer().getScheduler().runTaskAsynchronously(main,  new Runnable() {
            @Override
            public void run() {
            	PlayerGameModeChangeListener.saveMultiInv();
            }
		});
	}
	
	public static void autoSaveDatabase() {
		Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(main, new Runnable() {
            @Override
            public void run() {
            	if(cache.size() > 0 || PlayerGameModeChangeListener.cache.size() > 0)  {
            		if(cache.size() > 0) AsyncSaveDatabase();
            		if(PlayerGameModeChangeListener.cache.size() > 0) AsyncSaveInvSave();
            		main.log("Database have been background saved!");
            	}
            }
        }, Configuration.DBInterval * 120L, Configuration.DBInterval * 120L);
	}
	
	/** GENERAL FUNCTION OF HANDLER **/
	
	public static void AddBlockMoreArguments(Block b, int id) {
		int x = b.getX(), y = b.getY(), z = b.getZ(), w = Bukkit.getWorlds().indexOf(b.getWorld());
		List<Integer> key = new ArrayList<Integer>();
		key.add(x); key.add(y); key.add(z); key.add(w);
		
		if(!saveStatus) cache.put(key, id);
		else extracache.put(key, id);
		fullcache.put(key, id);
	}
	
	@SuppressWarnings("deprecation")
	public static void AddBlock(Block b) {
		int x = b.getX(), y = b.getY(), z = b.getZ(), w = Bukkit.getWorlds().indexOf(b.getWorld()), id = b.getTypeId();
		List<Integer> key = new ArrayList<Integer>();
		key.add(x); key.add(y); key.add(z); key.add(w);
		
		if(!saveStatus) cache.put(key, id);
		else extracache.put(key, id);
		fullcache.put(key, id);
	}
	
	public static void AddLocation(Location l) {
		World w = l.getWorld();
		Block b = w.getBlockAt(l);
		AddBlock(b);
	}
	
	public static void RemoveBlock(Block b) {
		int x = b.getX(), y = b.getY(), z = b.getZ(), w = Bukkit.getWorlds().indexOf(b.getWorld());
		List<Integer> key = new ArrayList<Integer>();
		key.add(x); key.add(y); key.add(z); key.add(w);
		
		if(!saveStatus) cache.put(key, 0);
		else extracache.put(key, 0);
		fullcache.remove(key);
	}
	
	@SuppressWarnings("deprecation")
	public static boolean CheckCreative(Block b) {
		int x = b.getX(), y = b.getY(), z = b.getZ(), id = b.getTypeId(), w = Bukkit.getWorlds().indexOf(b.getWorld());
		List<Integer> key = new ArrayList<Integer>();
		key.add(x); key.add(y); key.add(z); key.add(w);
		
			if(fullcache.containsKey(key) && fullcache.get(key) == id &&  id != 0)
				return true;
			else  {
				if(fullcache.containsKey(key) && fullcache.get(key) != id)
					RemoveBlock(b);
				return false;
			}
	}
	
	@SuppressWarnings("deprecation")
	public static int CheckCreativeRough(Block b) {
		int x = b.getX(), y = b.getY(), z = b.getZ(), id = b.getTypeId(), w = Bukkit.getWorlds().indexOf(b.getWorld());
		List<Integer> key = new ArrayList<Integer>();
		key.add(x); key.add(y); key.add(z); key.add(w);
			
		if(fullcache.containsKey(key) &&  id != 0)
			if(fullcache.get(key) == id) return 1;
			else return 2;
		return 0;
	}
	
	public static boolean ListCheckCreative(List<Block> Blocks) {
		for(Block b : Blocks) {
			if(CheckCreative(b))
				return true;
		}
		return false;
	}
	
	
	/** EXTRA **/
	
	public static void UpdateBlockToLocation(Block b, Location l) {
		if(!CheckCreative(b)) return;
		World w = l.getWorld();
		AddBlock(w.getBlockAt(l));
	}
	
	public static void FullClear(Block b) {
		if(!CheckCreative(b)) return;
		b.setType(Material.AIR);
		RemoveBlock(b);
	}
	
	
	@SuppressWarnings("deprecation")
	public static void DropBlocks(Block b)
	{
		int h = b.getLocation().getBlockY();
		World w = b.getWorld();
		for(int j = b.getLocation().getBlockY() + 1; j <= 256; j++) {
			h++;
			Block thish = w.getBlockAt(b.getX(), j, b.getZ());
			if(!ifUpDrop(thish) && !ifOneUpDrop(thish)) {
				j = 257;
			}
		}
		
		for(int j = h; j > b.getLocation().getBlockY(); j--) 
		{
			Block NewB = w.getBlockAt(b.getLocation().getBlockX(), j, b.getLocation().getBlockZ());
				if(ifUpDrop(NewB)) FullClear(NewB);
		}
		
		Block NewB = w.getBlockAt(b.getLocation().getBlockX(), b.getLocation().getBlockY() + 1, b.getLocation().getBlockZ());
		if(ifOneUpDrop(NewB))	FullClear(NewB);
		
		NewB = w.getBlockAt(b.getLocation().getBlockX(), b.getLocation().getBlockY(), b.getLocation().getBlockZ() - 1);
		if(ifLaterallyDrop(NewB) && NewB.getData() == 2)
			FullClear(NewB);
		
		NewB = w.getBlockAt(b.getLocation().getBlockX(), b.getLocation().getBlockY(), b.getLocation().getBlockZ() + 1);
		if(ifLaterallyDrop(NewB) && NewB.getData() == 3)
			FullClear(NewB);
		
		NewB = w.getBlockAt(b.getLocation().getBlockX() + 1, b.getLocation().getBlockY(), b.getLocation().getBlockZ());
		if(ifLaterallyDrop(NewB) && NewB.getData() == 5)
			FullClear(NewB);
		
		NewB = w.getBlockAt(b.getLocation().getBlockX() - 1, b.getLocation().getBlockY(), b.getLocation().getBlockZ());
		if(ifLaterallyDrop(NewB) && NewB.getData() == 4) 
			FullClear(NewB);
	}
	
	public static boolean CheckBlock(Block b) {
		if(ifWaterDrop(b) && CheckCreative(b)) {
				FullClear(b);
				return true;
		}
		return false;
	}
	
	public static void cactusClear(Block b) {
		Block cactus = null;
		
		cactus = b.getWorld().getBlockAt(b.getX() + 1, b.getY(), b.getZ());
		if(cactus.getType().equals(Material.CACTUS) && CheckCreative(cactus)) {
			DropBlocks(cactus);
			FullClear(cactus);
		}
			
		cactus = b.getWorld().getBlockAt(b.getX() - 1, b.getY(), b.getZ());
		if(cactus.getType().equals(Material.CACTUS) && CheckCreative(cactus)) {
			DropBlocks(cactus);
			FullClear(cactus);
		}
		
		cactus = b.getWorld().getBlockAt(b.getX(), b.getY(), b.getZ() + 1);
		if(cactus.getType().equals(Material.CACTUS) && CheckCreative(cactus)) {
			DropBlocks(cactus);
			FullClear(cactus);
		}
		
		cactus = b.getWorld().getBlockAt(b.getX(), b.getY(), b.getZ() - 1);
		if(cactus.getType().equals(Material.CACTUS) && CheckCreative(cactus)) {
			DropBlocks(cactus);
			FullClear(cactus);
		}
	}

	
	/** EXTRA **/
	
	public static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s);
	    }
	    catch(NumberFormatException e) { 
	    	return false; 
	    }
	    return true;
	}
	
	public static boolean ifUpDrop(Block b) {
		return b.getType() == Material.CARPET ||
			b.getType() == Material.SIGN_POST ||
			b.getType() == Material.CACTUS ||
			b.getType() == Material.SUGAR_CANE_BLOCK;
	}
	
	public static boolean ifOneUpDrop(Block b) {
		if(b.getType() == Material.BED_BLOCK ||
			b.getType() == Material.LEVER ||
			b.getType() == Material.TORCH ||
			b.getType() == Material.REDSTONE_TORCH_ON ||
			b.getType() == Material.REDSTONE_TORCH_OFF ||
			b.getType() == Material.STONE_PLATE ||
			b.getType() == Material.WOOD_PLATE ||
			b.getType() == Material.GOLD_PLATE ||
			b.getType() == Material.IRON_PLATE ||
			b.getType() == Material.WOODEN_DOOR ||
			b.getType() == Material.IRON_DOOR_BLOCK ||
			b.getType() == Material.REDSTONE ||
			b.getType() == Material.REDSTONE_COMPARATOR ||
			b.getType() == Material.REDSTONE_COMPARATOR_OFF ||
			b.getType() == Material.REDSTONE_COMPARATOR_ON ||
			b.getType() == Material.REDSTONE_WIRE ||
			b.getType() == Material.DIODE_BLOCK_ON ||
			b.getType() == Material.DIODE_BLOCK_OFF ||
			b.getType() == Material.DIODE ||
			b.getType() == Material.RAILS ||
			b.getType() == Material.DETECTOR_RAIL ||
			b.getType() == Material.ACTIVATOR_RAIL ||
			b.getType() == Material.POWERED_RAIL ||
			b.getType() == Material.BED ||
			b.getType() == Material.FLOWER_POT ||
			b.getType() == Material.DOUBLE_PLANT ||
			b.getType() == Material.RED_ROSE ||
			b.getType() == Material.YELLOW_FLOWER ||
			b.getType() == Material.STONE_BUTTON ||
			b.getType() == Material.WOOD_BUTTON ||
			b.getType() == Material.BROWN_MUSHROOM ||
			b.getType() == Material.RED_MUSHROOM ||
			b.getType() == Material.GOLD_PLATE ||
			b.getType() == Material.IRON_PLATE ||
			b.getType() == Material.STONE_PLATE ||
			b.getType() == Material.WOOD_PLATE)
			return true;
		if(ShareControl.isOneDotEightPlus())
			if(b.getType() == Material.ACACIA_DOOR ||
				b.getType() == Material.SPRUCE_DOOR ||
				b.getType() == Material.BIRCH_DOOR ||
				b.getType() == Material.JUNGLE_DOOR ||
				b.getType() == Material.DARK_OAK_DOOR ||
				b.getType() == Material.STANDING_BANNER)
				return true;
		return false;
	}
	
	public static boolean ifLaterallyDrop(Block b) {
		if(b.getType() == Material.LEVER ||
				b.getType() == Material.TORCH ||
				b.getType() == Material.REDSTONE_TORCH_ON ||
				b.getType() == Material.REDSTONE_TORCH_OFF ||
				b.getType() == Material.STONE_BUTTON ||
				b.getType() == Material.WOOD_BUTTON ||
				b.getType() == Material.TRAP_DOOR ||
				b.getType() == Material.LADDER ||
				b.getType() == Material.WALL_SIGN)
		return true;
		if(ShareControl.isOneDotEightPlus())
			if(b.getType() == Material.STANDING_BANNER ||
				b.getType() == Material.IRON_TRAPDOOR)
				return true;
		return false;
	}
	
	public static boolean ifWaterDrop(Block b) {
		return b.getType() == Material.LEVER ||
			b.getType() == Material.WOOD_BUTTON ||
			b.getType() == Material.TORCH ||
			b.getType() == Material.REDSTONE_TORCH_ON ||
			b.getType() == Material.REDSTONE_TORCH_OFF ||
			b.getType() == Material.REDSTONE ||
			b.getType() == Material.REDSTONE_COMPARATOR ||
			b.getType() == Material.REDSTONE_COMPARATOR_OFF ||
			b.getType() == Material.REDSTONE_COMPARATOR_ON ||
			b.getType() == Material.REDSTONE_WIRE ||
			b.getType() == Material.DIODE_BLOCK_ON ||
			b.getType() == Material.DIODE_BLOCK_OFF ||
			b.getType() == Material.DIODE ||
			b.getType() == Material.RAILS ||
			b.getType() == Material.DETECTOR_RAIL ||
			b.getType() == Material.ACTIVATOR_RAIL ||
			b.getType() == Material.POWERED_RAIL ||
			b.getType() == Material.FLOWER_POT ||
			b.getType() == Material.FLOWER_POT_ITEM ||
			b.getType() == Material.DOUBLE_PLANT ||
			b.getType() == Material.RED_ROSE ||
			b.getType() == Material.YELLOW_FLOWER ||
			b.getType() == Material.STONE_BUTTON ||
			b.getType() == Material.CARPET ||
			b.getType() == Material.SAPLING ||
			b.getType() == Material.WEB ||
			b.getType() == Material.SKULL ||
			b.getType() == Material.SKULL_ITEM ||
			b.getType() == Material.ITEM_FRAME ||
			b.getType() == Material.BROWN_MUSHROOM ||
			b.getType() == Material.RED_MUSHROOM;
	}
	
	@SuppressWarnings("unchecked")
	public static void SQLSave() {
		Integer id;
		Set<List<Integer>> keys = cache.keySet();
		for(int i=0; i < keys.size(); i++) {
			List<Integer> key = (List<Integer>) keys.toArray()[i];
			Integer x = key.get(0),
    			y = key.get(1),
    			z = key.get(2),
    			w = key.get(3);
    		
    		if(cache.get(key) == 0) id = null;
    		else id = cache.get(key);
    		
    		MySQL.SQLUpdate(x, y, z, id, w);
    		MySQL.SQLUpdate(x, y, z, id, w);
		}
    	id = null;
	}
	
	/** DEBUG FUNCTIONS **/
	
	public static void getCache() {
		for(List<Integer> key : cache.keySet()) {
    		String coords = "";
    		for(int i=0; i < key.size(); i++)
    			if(coords == "") coords = key.get(i).toString();
    			else coords = coords + "." + key.get(i);
    		main.getLogger().info(coords + " " + cache.get(key));
    	}
	}
}


