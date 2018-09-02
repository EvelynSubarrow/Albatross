package moe.evelyn.albatross;

import moe.evelyn.albatross.net.UpdateCheck;
import moe.evelyn.albatross.utils.SubCommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandHandler extends SubCommandExecutor
{
    private Main main;
    public CommandHandler(Main main) {
        this.main = main;
    }

    @Subcommand(
        permissions = {"commandspy.use"},
        visible=false
    )
    @Override
    public void Null(CommandSender sender, String[] args) {

    }

    @Subcommand(
        permissions = {"commandspy.use"},
        aliases = {"true"}
    )
    public void on(CommandSender sender, String[] args) {

    }

    @Subcommand(
            permissions = {"commandspy.use"},
            aliases = {"false"}
    )
    public void off(CommandSender sender, String[] args) {

    }

    @Subcommand(
        permissions = {"commandspy.use"},
        aliases = {"s"}
    )
    public void set(CommandSender sender, String[] args) {

    }

    @Subcommand(
            permissions = {"commandspy.use"},
            aliases = {"a"}
    )
    public void add(CommandSender sender, String[] args) {

    }

    @Subcommand(
            permissions = {"commandspy.use"},
            aliases = {"u"}
    )
    public void updates(CommandSender sender, String[] args) {
        UpdateCheck.VersionEntry entry = main.updateCheck.getCurrentVersion();

        sender.sendMessage(entry.version);
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
