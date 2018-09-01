package moe.evelyn.albatross;

import moe.evelyn.albatross.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;

import java.text.MessageFormat;

public class EventListener implements Listener
{
    private Main main;
    public EventListener(Main main) {
        this.main = main;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoinEvent(PlayerJoinEvent event)
    {
        main.userManager.forSender(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event)
    {
        main.userManager.removePlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSignChangeEvent(SignChangeEvent event)
    {

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerCommandEvent(ServerCommandEvent event){

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {

    }
}
