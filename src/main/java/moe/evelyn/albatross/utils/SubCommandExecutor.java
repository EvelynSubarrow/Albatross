package moe.evelyn.albatross.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public abstract class SubCommandExecutor implements CommandExecutor, TabCompleter
{
    private HashMap<String, Method> commands = new LinkedHashMap<String, Method>();
    private HashMap<String, Method> commandsNoAlias = new LinkedHashMap<String, Method>();
    {
        for(Method method : this.getClass().getMethods()) {
            command annotation = method.getAnnotation(command.class);
            if (annotation == null) continue;
            commands.put(method.getName(), method);
            commandsNoAlias.put(method.getName(), method);
            for (String alias : annotation.aliases()) {
                commands.put(alias, method);
            }
        }
    }

	/*
	 * For passing from other subcommandexecutor classes
	 */
	public void onCommand(CommandSender sender,String[] args) {
		onCommand(sender,"","",args);
	}
	
	@command(description="Empty command", visible=false)
	public void Null(CommandSender sender, String[] args) {
		sender.sendMessage(ChatColor.RED + "It is not possible to use this command with no arguments.");
		sender.sendMessage(ChatColor.RED + "Use the 'help' subcommand for a list of available subcommands.");
	}
	
	public void onInvalidCommand(CommandSender sender,String[] arguments, String commandName) {
		sender.sendMessage(ChatColor.RED + "The subcommand '" + commandName + "' does not exist.");
	}
	
	public void onConsoleExecutePlayerOnlyCommand(CommandSender sender, String[] args, String commandName) {
		sender.sendMessage(ChatColor.RED + "This command must be executed as a player.");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		return onCommand(sender, command.getName(), label, args);
	}
	
	public boolean onCommand(CommandSender sender, String command, String label, String[] args) {
		ArrayList<String> arguments=new ArrayList<String>();
		String c="";
		try{
			c=args[0];
			boolean b=false;
			for(String s:args){
				if(!b){b=true;continue;}
				arguments.add(s);
			}
		}catch(Exception e){

		}
		onSubCommand(sender, arguments.toArray(new String[arguments.size()]), c);
		return true;
	}

	public void onSubCommand(CommandSender sender,String[] arguments,String commandName){
		if(commandName.isEmpty()) commandName="Null";
        try {
            Method method = commands.get(commandName);
            if (method == null) {
                onInvalidCommand(sender, arguments, commandName);
                return;
            }
            command c = method.getAnnotation(command.class);

            if(arguments==null) arguments=c.defaultArguments();

            if(!(sender instanceof Player)&&c.playerOnly()) {
                onConsoleExecutePlayerOnlyCommand(sender,arguments,commandName);
            }else if(arguments.length>c.maximumArgsLength()) {
                sender.sendMessage(c.usage());
            }else if(arguments.length<c.minimumArgsLength()) {
                sender.sendMessage(c.usage());
            }else if(!hasPerms(sender,c.permissions())) {
                sender.sendMessage(ChatColor.RED + "You do not have permission do do that!");
                for(String p:c.permissions()) {
                    sender.sendMessage("- " + p);
                }
            } else {
                //well..... if they're here...
                method.invoke(this, sender,arguments);
            }
        } catch(Exception e) {
			e.printStackTrace();
			sender.sendMessage(ChatColor.RED + "Internal error!");
		}
	}

	private boolean hasPerms(CommandSender sender,String[] perms){
	    if (sender instanceof ConsoleCommandSender) return true;
		for(String p:perms) {
			if(!sender.hasPermission(p)) return false;
		}
		return true;
	}
	
	@command(
			maximumArgsLength=1,
			usage="[command]",
			description="displays help"
			)
	public void help(CommandSender sender,String[] args){
		if(args.length==1){
            Method method = this.commands.get(args[0]);
            if (method==null) {
                sender.sendMessage(ChatColor.RED + "Unknown subcommand.");
                return;
            }
            command c = method.getAnnotation(command.class);
                sender.sendMessage("[" + ((hasPerms(sender,c.permissions())) ? ChatColor.GREEN : ChatColor.RED) +
                        method.getName() + ChatColor.GRAY + " subcommand Summary]");
            sender.sendMessage(ChatColor.GRAY + method.getName() + " " + c.usage() + ChatColor.GRAY + " - " + c.description());
            sender.sendMessage(ChatColor.GRAY + "Permissions: " + (c.permissions().length==0 ? ChatColor.GREEN + "none" : Utils.join(c.permissions(), ",", 0)));
		} else {
            for (Method m : this.commandsNoAlias.values()) {
                command c = m.getAnnotation(command.class);
                if (c.visible()) {
                    sender.sendMessage(((hasPerms(sender, c.permissions())) ? ChatColor.GREEN : ChatColor.RED) +
                            m.getName() + " " + c.usage() + ChatColor.GRAY + " - " + c.description());
                }
            }
        }
	}

	@Retention(RetentionPolicy.RUNTIME) public @interface command{
		String[] permissions() default {};
		String[] aliases() default {};
        boolean playerOnly() default false;

        String[] defaultArguments() default {};
		int minimumArgsLength() default 0;
		int maximumArgsLength() default 100;

		String usage() default "";
		String description() default "";
        boolean visible() default true;
	}
}
