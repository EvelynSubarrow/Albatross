package moe.evelyn.albatross.rules;

import moe.evelyn.albatross.utils.AnnotationConfig;
import moe.evelyn.albatross.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class RuleGroup extends AnnotationConfig implements Iterable<Rule>
{
    public File originalFile = null;

            public GroupEffect effect = GroupEffect.ACCEPT;
    @config public String permission = "commandspy.use";
    @config public String identifier = "";
    @config public String familiar = "";

    private ArrayList<Rule> rules = new ArrayList<>();

    private CommandSender owner = null;

    public RuleGroup(CommandSender sender) {
        this.owner = sender;
        this.identifier = Utils.getUUID(sender);
        this.familiar = sender.getName();
    }

    public RuleGroup(String identifier, String familiar, GroupEffect effect, String permission) {
        this.identifier = identifier;
        this.familiar = familiar;
        this.effect = effect;
        this.permission = permission;
    }

    public RuleGroup(ConfigurationSection section, File file) {
        this.originalFile = file;
        loadFrom(section);
    }

    public String getSlug() {
        return identifier + "_" + familiar.toLowerCase();
    }

    @Subcommand(
            permissions = {"commandspy.use"},
            aliases = {"s"},
            description = "Replace all of this group's rules with a single new one",
            minimumArgsLength = 3,
            maximumArgsLength = 3
    )
    public void set(CommandSender sender, String[] args) {
        RuleType type = RuleType.fromString(args[0]);
        if (type == null) {
            sender.sendMessage("§cType must be §8command§c, §8sign§c, or §8all");
        } else if (args[1].isEmpty() || args[2].isEmpty()) {
            sender.sendMessage("§cUsername and message can't be empty");
        } else {
            this.clear();
            this.add(new Rule(type, args[1], args[2]));
            this.sendSummary(sender);
        }
    }

    @Subcommand(
            permissions = {"commandspy.use"},
            aliases = {"a"},
            description = "Add a rule to this group",
            minimumArgsLength = 3,
            maximumArgsLength = 3
    )
    public void add(CommandSender sender, String[] args) {
        RuleType type = RuleType.fromString(args[0]);
        if (type == null) {
            sender.sendMessage("§cType must be §8command§c, §8sign§c, or §8all");
        } else if (args[1].isEmpty() || args[2].isEmpty()) {
            sender.sendMessage("§cUsername and message can't be empty");
        } else {
            this.add(new Rule(type, args[1], args[2]));
            this.sendSummary(sender);
        }
    }

    @Suggestion({"add", "a", "set", "s"})
    public List<String> addCompletion(CommandSender sender, String[] args) {
        List<String> possibilities = new ArrayList<>();
        String lastArgument = args[args.length-1];
        if (args.length == 1)
        {
            possibilities.add("command");
            possibilities.add("sign");
        } else if (args.length > 1 && args.length < 4) {
            possibilities.add("*");
        }
        return possibilities.stream().filter((x) -> x.startsWith(lastArgument)).collect(Collectors.toList());
    }

    @Subcommand(visible=false)
    @Override
    public void Null(CommandSender sender, String[] args) {
        this.sendSummary(sender);
    }

    public boolean matches(CommandSender sender, String[] lines) {
        for(Rule rule : this) {
            if (rule.matches(sender, lines)) return true;
        }
        return false;
    }

    public boolean matches(CommandSender sender, String message) {
        for(Rule rule : this) {
            if (rule.matches(sender, message)) return true;
        }
        return false;
    }

    public void add(Rule rule) {
        rules.add(rule);
    }

    public void clear() {
        rules.clear();
    }

    public void sendSummary(CommandSender sender) {
        sender.sendMessage(String.format("§8%s §r%s %s", this.identifier, this.familiar, this.effect.getStringColoured()));
        for(Rule rule : this) {
            sender.sendMessage("    " + rule.toStringColoured());
        }
    }

    @Override
    public Iterator<Rule> iterator() {
        return rules.iterator();
    }

    @Override
    public void loadFrom(ConfigurationSection section) {
        super.loadFrom(section);
        this.effect = GroupEffect.valueOf(section.getString("effect"));

        // This is absurd, but it somehow works out easier
        for (Map<String,Object> map : (List<Map<String,Object>>)section.getList("rules")) {
            MemoryConfiguration cs = new MemoryConfiguration();
            cs.addDefaults(map);
            rules.add(new Rule(cs));
        }
    }

    @Override
    public void applyTo(ConfigurationSection section) {
        super.applyTo(section);
        section.set("effect", this.effect.toString());
        section.set("rules", this.rules.stream().map((x) -> {
            ConfigurationSection s = new MemoryConfiguration();
            x.applyTo(s);
            return s;
        }).collect(Collectors.toList()));
    }

    @Override
    public String getPrefix() {
        //RuleGroup has no real awareness of how it was called, so a prefix isn't possible
        return "";
    }
}
