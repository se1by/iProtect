package me.se1by.iProtect;

import java.io.IOException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.Plugin;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import com.sk89q.worldguard.protection.managers.RegionManager;
import static com.sk89q.worldguard.bukkit.BukkitUtil.*;

public class signListener extends BlockListener{
	//private iProtect plugin;
	private String chatpre = ChatColor.DARK_GREEN + "[iProtect] " + ChatColor.DARK_AQUA;
	Location actualLoc;
	Location smallestXLoc;
	Location smallestYLoc;
	Location biggestXLoc;
	Location biggestYLoc;
	Location smallestLoc;
	Location biggestLoc;
	private HashMap<Location, Boolean> AllLocations = new HashMap<Location, Boolean>();
	private boolean closed = false;
	Player p;
	private String owner;

	public signListener(iProtect iPro){
		//this.plugin = iPro;
	}
	
	public void onSignChange(SignChangeEvent event){
		this.p = event.getPlayer();
		Block block = event.getBlock();
		
		if (event.getLine(0).equalsIgnoreCase("[iProtect]")){
			if (p.hasPermission("iProtect.Set") || p.isOp()){
				this.owner = event.getLine(1);
				
				if (block.getRelative(BlockFace.EAST ).getType() == Material.FENCE || block.getRelative(BlockFace.NORTH).getType() == Material.FENCE || block.getRelative(BlockFace.SOUTH).getType() == Material.FENCE || block.getRelative(BlockFace.WEST).getType() == Material.FENCE){
					around(block.getLocation());
					while (!closed){
						around(actualLoc);
					}
					smallestLoc = new Location(p.getWorld(), smallestXLoc.getX(), smallestYLoc.getY(), smallestXLoc.getZ());
					biggestLoc = new Location(p.getWorld(), biggestXLoc.getX(), biggestYLoc.getY(), biggestYLoc.getZ());
					try {
						protect(owner, smallestLoc, biggestLoc);
					} catch (CommandException e) {
						System.out.println("Unable to protect region!");
						e.printStackTrace();
					}
				}
				else {
					
				}
				
				
			}
			else{
				p.sendMessage(chatpre + "Insufficient permissions!");
			}
		}
	}
	
	public boolean around(Location loc){
		double x = loc.getX();
		double z = loc.getZ();
		
		Location[] locations = new Location[4];		
		locations[0] = new Location(loc.getWorld(), x + 1, loc.getY(), z);
		locations[1] = new Location(loc.getWorld(), x - 1, loc.getY(), z);
		locations[2] = new Location(loc.getWorld(), x, loc.getY(), z + 1);
		locations[3] = new Location(loc.getWorld(), x, loc.getY(), z - 1);
		
		for (Location location : locations){
			if (location.getBlock().getType() == Material.FENCE){
				this.actualLoc = location;
				
				if (smallestXLoc != null){
					if (location.getBlockX() < smallestXLoc.getX()){
						this.smallestXLoc = location;
					}
					if (location.getX() > biggestXLoc.getX()){
						this.biggestXLoc = location;
					}
					if (location.getY() < smallestYLoc.getY()){
						this.smallestYLoc = location;
					}
					if (location.getY() > biggestYLoc.getY()){
						this.biggestYLoc = location;
					}
					if (AllLocations.get(location) != null){
						closed = true;
					}
				}
				else{
					this.smallestXLoc = location;
					this.biggestXLoc = location;
					this.smallestYLoc = location;
					this.biggestYLoc = location;
				}
			}
			else{
				if (!closed){
					p.sendMessage(chatpre + "The fence isn't closed yet!");
					closed = true;
				}
			}
		}
		
		
		return false;
	}
	
	private WorldGuardPlugin getWorldGuard() {
	    Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
	 
	    // WorldGuard may not be loaded
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        return null; // Maybe you want throw an exception instead
	    }
	 
	    return (WorldGuardPlugin) plugin;
	}
	
	public void protect (String id, Location mini, Location maxi) throws CommandException{
		com.sk89q.worldedit.Vector Min = toVector(mini);
		com.sk89q.worldedit.Vector Max = toVector(mini);
		
		BlockVector min = Min.toBlockVector();
        BlockVector max = Max.toBlockVector();
        ProtectedRegion region = new ProtectedCuboidRegion(id, min, max);
        WorldGuardPlugin pl = getWorldGuard();
        
        RegionManager mgr = pl.getGlobalRegionManager().get(mini.getWorld());
        region.setOwners((DefaultDomain) p);
        
        mgr.addRegion(region);
        try
        {
          mgr.save();
          p.sendMessage(ChatColor.YELLOW + "Region saved as " + id + ".");
        } catch (IOException e) {
          throw new CommandException("Failed to write regions file: " + e.getMessage());
        }
	}
}
