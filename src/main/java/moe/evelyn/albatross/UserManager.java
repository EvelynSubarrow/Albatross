package moe.evelyn.albatross;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class UserManager
{
    private Main main;
    private HashMap<CommandSender, UserContainer> userMap = new HashMap<>();
    public UserManager(Main main) {
        this.main = main;
        userMap.put(main.getServer().getConsoleSender(), new UserContainer(main.getServer().getConsoleSender()));
    }

    public UserContainer forSender(CommandSender sender) {
        if (!userMap.containsKey(sender)) {
            if (sender instanceof Player) {
                userMap.put(sender, new UserContainer((Player)sender));
            } else {
                userMap.put(sender, new UserContainer((ConsoleCommandSender)sender));
            }
        }
        return userMap.get(sender);
    }

    public boolean removePlayer(Player player) {
        if (userMap.containsKey(player)) {
            userMap.remove(player);
            return true;
        } else {
            return false;
        }
    }
}
