package moe.evelyn.albatross;

import moe.evelyn.albatross.net.Statistics;
import moe.evelyn.albatross.net.UpdateCheck;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Main extends JavaPlugin
{
    private EventListener eventListener = new EventListener(this);
    private CommandHandler commandHandler = new CommandHandler(this);
    private Statistics statistics;
    private Server server;

    protected UserManager userManager;
    protected UpdateCheck updateCheck;
    protected Config config = new Config();


    @Override
    public void onEnable() {
        server = Bukkit.getServer();
        userManager = new UserManager(this);
        server.getPluginManager().registerEvents(eventListener, this);
        server.getPluginCommand("commandspy").setExecutor(commandHandler);
        server.getPluginCommand("commandspy").setTabCompleter(commandHandler);
        this.statistics = new Statistics(this);
        this.updateCheck = new UpdateCheck(this);

        config.loadFrom(this.getConfig());
        config.applyTo(this.getConfig());
        this.saveConfig();
    }

    @Override
    public void onDisable(){
        config.applyTo(this.getConfig());
        this.saveConfig();
    }
}
