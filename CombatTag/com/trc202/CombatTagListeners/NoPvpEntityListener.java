package com.trc202.CombatTagListeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.trc202.CombatTag.CombatTag;
import com.trc202.Containers.PlayerDataContainer;
import com.trc202.Containers.Settings;

public class NoPvpEntityListener implements Listener{

	CombatTag plugin;
	
	public NoPvpEntityListener(CombatTag combatTag){
		this.plugin = combatTag;
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDamage(EntityDamageEvent EntityDamaged){
		if (EntityDamaged.isCancelled()){return;}
		if (EntityDamaged.getCause() == DamageCause.ENTITY_ATTACK && (EntityDamaged instanceof EntityDamageByEntityEvent)){
    		EntityDamageByEntityEvent e = (EntityDamageByEntityEvent)EntityDamaged;
    		if ((e.getDamager() instanceof Player) && (e.getEntity() instanceof Player)){//Check to see if the damager and damaged are players
    			Player damager = (Player) e.getDamager();
    			Player tagged = (Player) e.getEntity();
    			for(String disallowedWorlds : plugin.settings.getDisallowedWorlds()){
    				if(damager.getWorld().getName().equalsIgnoreCase(disallowedWorlds)){
    					//Skip this tag the world they are in is not to be tracked by combat tag
    					return;
    				}
    			}
    			if(plugin.settings.getCurrentMode() == Settings.SettingsType.NPC){
	    			onPlayerDamageByPlayerNPCMode(damager,tagged);
    			}else if(plugin.settings.getCurrentMode() == Settings.SettingsType.TIMED){
    				onPlayerDamageByPlayerTimedMode(damager,tagged);
    			}
    		}
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDeath(EntityDeathEvent event){
		if(plugin.npcm.isNPC(event.getEntity())){
			onNPCDeath(event);
		}
		//if Player died with a tag duration, cancel the timeout and remove the data container
		else if(event.getEntity() instanceof Player){
			Player deadPlayer = (Player) event.getEntity();
			onPlayerDeath(deadPlayer);
		}
	}
	
	private void onNPCDeath(EntityDeathEvent event){
		if(plugin.hasDataContainer(plugin.getPlayerName(event.getEntity()))){
			plugin.killPlayerEmptyInventory(plugin.getPlayerData(plugin.getPlayerName(event.getEntity())));
		}
	}
	
	private void onPlayerDeath(Player deadPlayer){
		if(plugin.hasDataContainer(deadPlayer.getName())){
			PlayerDataContainer deadPlayerData = plugin.getPlayerData(deadPlayer.getName());
			deadPlayerData.setPvPTimeout(0);
			plugin.removeDataContainer(deadPlayer.getName());
		}
	}
	
	private void onPlayerDamageByPlayerNPCMode(Player damager, Player damaged){
		if(plugin.npcm.isNPC(damaged)){return;} //If the damaged player is an npc do nothing
		PlayerDataContainer damagerData;
		PlayerDataContainer damagedData;
		if(!damager.hasPermission("combattag.ignore")){	
			//Get damager player data container
			if(plugin.hasDataContainer(damager.getName())){damagerData = plugin.getPlayerData(damager.getName());
			}else{damagerData = plugin.createPlayerData(damager.getName());}
			damagerData.setPvPTimeout(plugin.getTagDuration());
		}
		if(!damaged.hasPermission("combattag.ignore")){	
			//Get damaged player data container
			if(plugin.hasDataContainer(damaged.getName())){damagedData = plugin.getPlayerData(damaged.getName());
			}else{damagedData = plugin.createPlayerData(damaged.getName());}
			damagedData.setPvPTimeout(plugin.getTagDuration());
		}
		if(plugin.isDebugEnabled()){plugin.log.info("[CombatTag] Player tagged another player, setting pvp timeout");}
	}
	
	private void onPlayerDamageByPlayerTimedMode(Player damager, Player tagged) {
		// TODO Auto-generated method stub
		if(!damager.hasPermission("combattag.ignore")){	
			PlayerDataContainer damagerData;
			if(plugin.hasDataContainer(damager.getName())){
				damagerData = plugin.getPlayerData(damager.getName());
			}else{damagerData = plugin.createPlayerData(damager.getName());}
			damagerData.setPvPTimeout(plugin.settings.getTagDuration());
		}
		if(!tagged.hasPermission("combattag.ignore")){
			PlayerDataContainer taggedData;
			if(plugin.hasDataContainer(tagged.getName())){
				taggedData = plugin.getPlayerData(tagged.getName());
			}else{taggedData = plugin.createPlayerData(tagged.getName());}
			taggedData.setPvPTimeout(plugin.settings.getTagDuration());
		}
	}
	
	
}