package moe.evelyn.albatross.rules;

import moe.evelyn.albatross.Main;
import moe.evelyn.albatross.rules.RuleGroup;
import moe.evelyn.albatross.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class RuleManager
{
    private Main main;
    private LinkedHashMap<String, RuleGroup>  generalGroups = new LinkedHashMap<>();
    private HashMap<CommandSender, RuleGroup> userGroups = new HashMap<>();

    private Set<CommandSender> subscribers = new HashSet<CommandSender>();

    public RuleManager(Main main) {
        this.main = main;
        loadGeneral();
        ensureIgnore();
        senderJoin(main.getServer().getConsoleSender());
    }

    public List<RuleGroup> getApplicableRuleGroups(CommandSender sender) {
        List<RuleGroup> groups = new ArrayList<>();
        for (RuleGroup group : generalGroups.values()) {
            if(sender.hasPermission(group.permission)) groups.add(group);
        }
        if (userGroups.containsKey(sender))
            groups.add(userGroups.get(sender));
        return groups;
    }

    public RuleGroup get(String familiar) {
        return generalGroups.get(familiar);
    }

    public RuleGroup get(CommandSender sender) {
        if (userGroups.containsKey(sender)) {
            return userGroups.get(sender);
        }
        return loadSender(sender);
    }

    public void notify(SignChangeEvent event) {
        List<CommandSender> receivers = new ArrayList<>();
        for(CommandSender sender : subscribers) {
            for (RuleGroup group : getApplicableRuleGroups(sender)) {
                if(group.matches(event.getPlayer(), event.getLines())) {
                    if(group.effect==GroupEffect.ACCEPT) {
                        receivers.add(sender);
                    } else {
                        break;
                    }
                }
            }
        }
        String highlightColour = getHighlightColour(event.getPlayer(), receivers);
        for (CommandSender sender : receivers) {
            sender.sendMessage(String.format("§8<%s%s§8> %s%s", highlightColour, event.getPlayer().getName(), highlightColour, Utils.join(event.getLines(), "§8|" + highlightColour, 0)));
        }
    }

    public void notify(ServerCommandEvent event) {
        notify(event.getSender(), "/" + event.getCommand());
    }

    public void notify(PlayerCommandPreprocessEvent event) {
        notify(event.getPlayer(), event.getMessage());
    }

    public void notify(CommandSender eventSender, String message) {
        List<CommandSender> receivers = new ArrayList<>();
        for(CommandSender sender : subscribers) {
            for (RuleGroup group : getApplicableRuleGroups(sender)) {
                if(group.matches(eventSender, message)) {
                    if(group.effect==GroupEffect.ACCEPT) {
                        receivers.add(sender);
                    } else {
                        break;
                    }
                }
            }
        }
        String highlightColour = getHighlightColour(eventSender, receivers);
        for (CommandSender sender : receivers) {
            sender.sendMessage(String.format("§8<%s%s§8> %s%s", highlightColour, eventSender.getName(), highlightColour, message));
        }
    }

    public String getHighlightColour(CommandSender sender, List<CommandSender> receivers) {
        if (sender == main.getServer().getConsoleSender()) {
            return "§5";
        } else if (receivers.contains(sender)) {
            return "§b";
        } else if (subscribers.contains(sender)) {
            return "§3";
        } else {
            return "§9";
        }
    }

    public void startPermissionCheck() {
        Bukkit.getScheduler().runTaskTimer(main, () -> {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                if (player.hasPermission("commandspy.receive")) {
                    subscribers.add(player);
                } else {
                    subscribers.remove(player);
                }
            }
        }, 40, 40);
    }

    public void senderJoin(CommandSender sender) {
        this.loadSender(sender);
        if (sender.hasPermission("commandspy.receive")) {
            this.subscribers.add(sender);
        }
    }

    public void senderQuit(CommandSender sender) {
        this.unloadSender(sender);
        subscribers.remove(sender);
    }

    public void ensureIgnore() {
        if (!generalGroups.containsKey("ignore")) {
            RuleGroup group = new RuleGroup("0500", "ignore", GroupEffect.REJECT, "commandspy.receive");
            group.add(new Rule(RuleType.COMMAND,"CONSOLE", "/list"));
            generalGroups.put("ignore", group);
            main.getLogger().info("Initialised ignore list as general/" + group.getSlug());
        }
    }

    public void loadGeneral() {
        Path baseDirectory = Paths.get(main.getDataFolder().getAbsolutePath(), "general");
        File[] matchingFiles = baseDirectory.toFile().listFiles((x) -> x.getName().endsWith(".yml"));
        if (matchingFiles != null) {
            for (File f : matchingFiles) {
                YamlConfiguration groupSection = YamlConfiguration.loadConfiguration(matchingFiles[0]);
                RuleGroup group = new RuleGroup(groupSection, f);
                generalGroups.put(group.familiar, group);
                main.getLogger().info("Successfully loaded general/" + group.getSlug());
            }
        }
    }

    public RuleGroup loadSender(CommandSender sender) {
        Path baseDirectory = Paths.get(main.getDataFolder().getAbsolutePath(), "users");
        File[] matchingFiles = baseDirectory.toFile().listFiles((x) -> x.getName().startsWith(Utils.getUUID(sender) + "_"));
        if (matchingFiles!=null && matchingFiles.length > 0) {
            YamlConfiguration groupSection = YamlConfiguration.loadConfiguration(matchingFiles[0]);
            RuleGroup group = new RuleGroup(groupSection, matchingFiles[0]);
            userGroups.put(sender, group);
            main.getLogger().info("Successfully loaded users/" + group.getSlug());
            return group;
        } else if (sender.hasPermission("commandspy.use")) {
            RuleGroup group = new RuleGroup(sender);
            this.userGroups.put(sender, group);
            main.getLogger().info("Initialised new rule list for " + group.getSlug());
            return group;
        }
        return null;
    }

    public void unloadSender(CommandSender sender) {
        RuleGroup group = userGroups.get(sender);
        if (group != null) {
            userGroups.remove(sender);
            try {
                commitRuleGroup(group, "users");
            } catch (IOException e) {
                main.getLogger().severe("Failed to commit users/" + group.getSlug() + " to file");
                main.getLogger().severe(e.getClass().getCanonicalName() + ": " + e.getMessage());
            }
        }
    }

    public void unloadAll() {
        for (RuleGroup group : this.generalGroups.values()) {
            try {
                commitRuleGroup(group, "general");
            } catch (IOException e) {
                main.getLogger().severe("Failed to commit general/" + group.getSlug() + " to file");
                main.getLogger().severe(e.getClass().getCanonicalName() + ": " + e.getMessage());
            }
        }
        generalGroups.clear();
        for (RuleGroup group : this.userGroups.values()) {
            try {
                commitRuleGroup(group, "users");
            } catch (IOException e) {
                main.getLogger().severe("Failed to commit users/" + group.getSlug() + " to file");
                main.getLogger().severe(e.getClass().getCanonicalName() + ": " + e.getMessage());
            }
        }
        userGroups.clear();
    }

    protected void commitRuleGroup(RuleGroup group, String type) throws IOException {
        File fn = Paths.get(main.getDataFolder().getAbsolutePath(), type, group.getSlug() + ".yml").toFile();
        YamlConfiguration config = new YamlConfiguration();
        group.applyTo(config);
        config.save(fn);
        if (group.originalFile!=null && !fn.equals(group.originalFile)) {
            if(group.originalFile.delete()) {
                main.getLogger().info("Successfully purged old file for " + type + "/" + group.getSlug());
            } else {
                main.getLogger().info("Failed to purge old file for " + type + "/" + group.getSlug() + ": " + group.originalFile.getAbsolutePath());
            }
        }
        main.getLogger().info("Successfully committed " + type + "/" + group.getSlug());
    }
}
