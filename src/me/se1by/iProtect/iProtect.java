package me.se1by.iProtect;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class iProtect extends JavaPlugin{

	private final sListener signListener = new sListener();

	@Override
	public void onDisable() {
		System.out.println("[iProtect] disabled");
		
	}

	@Override
	public void onEnable() {
		System.out.println("[iProtect] enabled");
		PluginManager pm = Bukkit.getPluginManager();
		
		pm.registerEvent(Event.Type.SIGN_CHANGE, this.signListener, Event.Priority.High, this);
	}

}
