package moe.evelyn.albatross;

import moe.evelyn.albatross.net.UpdateCheck;
import moe.evelyn.albatross.rules.Rule;
import moe.evelyn.albatross.rules.RuleGroup;
import moe.evelyn.albatross.rules.RuleType;
import moe.evelyn.albatross.utils.SubCommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler extends SubCommandExecutor
{
    private Main main;
    public CommandHandler(Main main) {
        this.main = main;
    }

    @Override
    public String getPrefix() {
        return "/commandspy";
    }

    @Subcommand(
        permissions = {"commandspy.use"},
        visible=false
    )
    @Override
    public void Null(CommandSender sender, String[] args) {
        for(RuleGroup group : main.ruleManager.getApplicableRuleGroups(sender)) {
            group.sendSummary(sender);
        }
    }

    @Subcommand(
        permissions = {"commandspy.use"},
        aliases = {"true"},
        description = "Equivalent to /cs add all * *",
        maximumArgsLength = 0
    )
    public void on(CommandSender sender, String[] args) {
        main.ruleManager.get(sender).clear();
        main.ruleManager.get(sender).add(new Rule(RuleType.ALL, "*", "*"));
        main.ruleManager.get(sender).sendSummary(sender);
    }

    @Subcommand(
            permissions = {"commandspy.use"},
            aliases = {"false", "off"},
            description = "Removes all of your rules",
            maximumArgsLength = 0
    )
    public void clear(CommandSender sender, String[] args) {
        main.ruleManager.get(sender).clear();
        main.ruleManager.get(sender).sendSummary(sender);
    }

    @Subcommand(
            permissions = {"commandspy.use"},
            aliases = {"s"},
            description = "Replace all of your rules with a single new one",
            usage = "[type] [username] [content]",
            minimumArgsLength = 3,
            maximumArgsLength = 3
    )
    public void set(CommandSender sender, String[] args) {
        main.ruleManager.get(sender).set(sender, args);
    }

    @Subcommand(
            permissions = {"commandspy.use"},
            aliases = {"a"},
            description = "Add a rule to your own group",
            usage = "[type] [username] [content]",
            minimumArgsLength = 3,
            maximumArgsLength = 3
    )
    public void add(CommandSender sender, String[] args) {
        main.ruleManager.get(sender).add(sender, args);
    }

    @Suggestion({"add", "a", "set", "s"})
    public List<String> addCompletion(CommandSender sender, String[] args) {
        RuleGroup group = main.ruleManager.get(sender);
        if (group != null) {
            return group.addCompletion(sender, args);
        } else {
            return new ArrayList<String>();
        }
    }

    @Subcommand(
            permissions = {"commandspy.configure"},
            aliases = {"i"},
            description = "Manipulate the global ignore list. No-one can see commands or sign changes specified here"
    )
    public void ignore(CommandSender sender, String[] args) {
        main.ruleManager.get("ignore").onCommand(sender, args);
    }

    @Suggestion({"ignore", "i"})
    public List<String> ignoreCompletion(CommandSender sender, String[] args) {
        return main.ruleManager.get("ignore").onTabComplete(sender, null, null, args);
    }

    @Subcommand(
            permissions = {"commandspy.use"},
            aliases = {"u"},
            description = "Checks plugin update status",
            maximumArgsLength = 0
    )
    public void updates(CommandSender sender, String[] args) {
        UpdateCheck.VersionEntry entry = main.updateCheck.getCurrentVersion();
        if (main.config.updateCheck) {
            if (entry == null) {
                sender.sendMessage("§cFailed to retrieve version information for v" + main.getDescription().getVersion());
            } else {
                sender.sendMessage(String.format("§8This is §6%s §8v§6%s", main.getDescription().getName(), entry.version));
                if (entry.isAhead) {
                    sender.sendMessage("§3This version is marked as a pre-release");
                }
                if (entry.hasBugs) {
                    sender.sendMessage("§cThis version has bugs");
                    sender.sendMessage("§c" + entry.bugsDescription);
                }
                if (entry.hasUpdate) {
                    sender.sendMessage("§3An update is available");
                } else {
                    sender.sendMessage("§3This is the latest version");
                }
            }
        } else {
            sender.sendMessage("§cUpdate checking is disabled");
        }
    }

    @Subcommand(
        permissions = {"commandspy.configure"},
        aliases = {"c"},
        description = "View and alter plugin configuration"
    )
    public void config(CommandSender sender, String[] args) {
        main.config.onCommand(sender, args);
    }
}
