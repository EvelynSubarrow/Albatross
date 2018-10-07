package moe.evelyn.albatross;

import moe.evelyn.albatross.net.UpdateCheck;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;

public class EventListener implements Listener
{
    private Main main;
    public EventListener(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        main.ruleManager.senderJoin(event.getPlayer());
        main.updateCheck.maybeUpdateNotify(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        main.ruleManager.senderQuit(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSignChangeEvent(SignChangeEvent event) {
        main.ruleManager.notify(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerCommandEvent(ServerCommandEvent event){
        main.ruleManager.notify(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        main.ruleManager.notify(event);
    }
}
