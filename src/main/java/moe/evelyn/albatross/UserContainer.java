package moe.evelyn.albatross;

import moe.evelyn.albatross.utils.AnnotationConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class UserContainer extends AnnotationConfig
{
    private final UUID identifier;
    private final CommandSender sender;

    public UserContainer(ConsoleCommandSender sender) {
        this.identifier = UUID.fromString("00000000-0000-0000-0000-000000000000");
        this.sender = sender;
    }

    public UserContainer(Player player) {
        this.identifier = player.getUniqueId();
        this.sender = player;
    }
}
