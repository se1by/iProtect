package me.se1by.iProtect;

import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;

import java.io.IOException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.SignChangeEvent;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class sListener extends BlockListener{
	
	private Player p;
	private Block block;
	private String Owner;
	private Location prefered;
	private HashMap<Location, Location>allLocations = new HashMap<Location, Location>();
	private Location b;
	private boolean closed = false;
	private boolean abort = false;
	private String name;

	public void onSignChange(SignChangeEvent event){
		this.p = event.getPlayer();
		this.block = event.getBlock();
		
		if (event.getLine(0).equalsIgnoreCase("[iProtect]")){
			if (p.hasPermission("iProtect.Set") || p.isOp()){
				
				if (event.getLine(2).trim().equals("")){
					Owner = p.getName();
					event.setLine(2, Owner);
				}
				else{
					this.Owner = event.getLine(2);
					
				}
				if (event.getLine(1).trim().equals("")){
					name = p.getName();
					event.setLine(1, name);
				}
				else{
					this.name = event.getLine(1);
				}
				Block attached = block.getRelative(((org.bukkit.material.Sign) block.getState().getData()).getAttachedFace());
				b = attached.getLocation();
				while (!closed && !abort){
					checkMat();
				}
				if(closed){
					Location sx = null;
					Location bx = null;
					Location sz = null;
					Location bz = null;
					
					for (Location l : allLocations.values()){						
						if (sx == null || sx.getBlockX() > l.getX()){
							sx = l;
						}
						if (bx == null || bx.getBlockX() < l.getX()){
							bx = l;
						}
						if (sz == null || sz.getBlockZ() > l.getZ()){
							sz = l;
						}
						if (bz == null || bz.getBlockZ() < l.getZ()){
							bz = l;
						}
					}
					
					Location sLoc = new Location(sx.getWorld(), sx.getX(), 0, sz.getZ());
					Location bLoc = new Location(bx.getWorld(), bx.getX(), 128, bz.getZ());
					System.out.println(sLoc);
					System.out.println(bLoc);
					
					try {
						protect(name, sLoc, bLoc);
						allLocations.clear();
						closed = false;
					} catch (CommandException e) {
						System.out.println("[iProtect] CommandException!");
						e.printStackTrace();
					}
				}
			}
		}
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
		com.sk89q.worldedit.Vector Max = toVector(maxi);
		
		BlockVector min = Min.toBlockVector();
        BlockVector max = Max.toBlockVector();
        ProtectedRegion region = new ProtectedCuboidRegion(id, min, max);
        WorldGuardPlugin pl = getWorldGuard();
        
        RegionManager mgr = pl.getGlobalRegionManager().get(mini.getWorld());
        DefaultDomain owner = new DefaultDomain();
        owner.addPlayer(Owner);
        region.setOwners(owner);
        
        mgr.addRegion(region);
        try
        {
          mgr.save();
          p.sendMessage(ChatColor.YELLOW + "Region saved as " + id + ".");
        } catch (IOException e) {
          throw new CommandException("Failed to write regions file: " + e.getMessage());
        }
	}

	public void fence(){		
		if (prefered != null && prefered.getBlock().getTypeId() == 85){
			allLocations.put(prefered, prefered);
			b = prefered;
		}
		else{
			checkMat();
		}
	}

	private void checkMat() {
		Location bX = new Location(b.getWorld(), b.getX() + 1, b.getY(), b.getZ());
		Location sX = new Location(b.getWorld(), b.getX() - 1, b.getY(), b.getZ());
		Location bZ = new Location(b.getWorld(), b.getX(), b.getY(), b.getZ() + 1);
		Location sZ = new Location(b.getWorld(), b.getX(), b.getY(), b.getZ() - 1);
		Location up = new Location(b.getWorld(), b.getX(), b.getY() + 1, b.getZ());
		Location down = new Location(b.getWorld(), b.getX(), b.getY() - 1, b.getZ());
		
			if((up.getBlock().getTypeId() == 85 || up.getBlock().getTypeId() == 107 || up.getBlock().getTypeId() == 113) && !allLocations.containsKey(up)){
				allLocations.put(up, up);
				prefered = up;
			}
			else if((down.getBlock().getTypeId() == 85 || down.getBlock().getTypeId() == 107 || down.getBlock().getTypeId() == 113) && !allLocations.containsKey(down)){
				allLocations.put(down, down);
				prefered = down;
			}
			else if((bX.getBlock().getTypeId() == 85 || bX.getBlock().getTypeId() == 107 || bX.getBlock().getTypeId() == 113) && !allLocations.containsKey(bX)){
				allLocations.put(bX, bX);
				prefered = bX;
			}
			else if((sX.getBlock().getTypeId() == 85 || sX.getBlock().getTypeId() == 107 || sX.getBlock().getTypeId() == 113) && !allLocations.containsKey(sX)){
				allLocations.put(sX, sX);
				prefered = sX;
			}
			else if((bZ.getBlock().getTypeId() == 85 || bZ.getBlock().getTypeId() == 107 || bZ.getBlock().getTypeId() == 113) && !allLocations.containsKey(bZ)){
				allLocations.put(bZ, bZ);
				prefered = bZ;
			}
			else if((sZ.getBlock().getTypeId() == 85 || sZ.getBlock().getTypeId() == 107 || sZ.getBlock().getTypeId() == 113) && !allLocations.containsKey(sZ)){
				allLocations.put(sZ, sZ);
				prefered = sZ;
			}
			else if (allLocations.containsKey(sZ)){
				closed = true;
			}
			else if (allLocations.containsKey(bZ)){
				closed = true;
			}
			else if (allLocations.containsKey(sX)){
				closed = true;
			}
			else if (allLocations.containsKey(bX)){
				closed = true;
			}
			else if (allLocations.containsKey(up)){
				closed = true;
			}
			else if (allLocations.containsKey(down)){
				closed = true;
			}
			else{
				abort = true;
			}
			b = prefered;
	}
}
