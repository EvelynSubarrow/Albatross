package moe.evelyn.albatross;

import moe.evelyn.albatross.net.Statistics;
import moe.evelyn.albatross.net.UpdateCheck;
import moe.evelyn.albatross.rules.RuleManager;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Timer;

public class Main extends JavaPlugin
{
    private EventListener eventListener = new EventListener(this);
    private CommandHandler commandHandler = new CommandHandler(this);
    private Statistics statistics;
    private Server server;

    protected RuleManager ruleManager;
    protected UpdateCheck updateCheck;
    public Config config = new Config();

    public Timer timer = new Timer(true);

    @Override
    public void onEnable() {
        server = Bukkit.getServer();
        ruleManager = new RuleManager(this);
        server.getPluginManager().registerEvents(eventListener, this);
        server.getPluginCommand("commandspy").setExecutor(commandHandler);
        server.getPluginCommand("commandspy").setTabCompleter(commandHandler);
        this.statistics = new Statistics(this);
        this.updateCheck = new UpdateCheck(this);
        this.updateCheck.startUpdateCheck();

        config.loadFrom(this.getConfig());
        config.applyTo(this.getConfig());
        this.saveConfig();
    }

    @Override
    public void onDisable(){
        this.config.applyTo(this.getConfig());
        this.saveConfig();
        this.ruleManager.unloadAll();
    }
}
