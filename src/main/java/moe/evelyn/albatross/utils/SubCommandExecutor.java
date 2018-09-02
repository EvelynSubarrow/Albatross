package moe.evelyn.albatross.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public abstract class SubCommandExecutor implements CommandExecutor, TabCompleter
{
    private HashMap<String, Method> commands = new LinkedHashMap<>();
    private HashMap<String, Method> commandsNoAlias = new LinkedHashMap<>();
    private HashMap<String, Method> tabCompleters = new LinkedHashMap<>();
    {
        for(Method method : this.getClass().getMethods()) {
            Subcommand annotation = method.getAnnotation(Subcommand.class);
            if (annotation == null) continue;
            commands.put(method.getName(), method);
            commandsNoAlias.put(method.getName(), method);
            for (String alias : annotation.aliases()) {
                commands.put(alias, method);
            }
        }
        for (Method method : this.getClass().getMethods()) {
            Suggestion annotation = method.getAnnotation(Suggestion.class);
            if (annotation == null) continue;
            for (String alias : annotation.value()) {
                tabCompleters.put(alias, method);
            }
        }
    }

	/*
	 * For passing from other subcommandexecutor classes
	 */
	public void onCommand(CommandSender sender,String[] args) {
		onCommand(sender,"","",args);
	}
	
	@Subcommand(description="Empty command", visible=false)
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
            Subcommand c = method.getAnnotation(Subcommand.class);

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
                method.invoke(this, sender, arguments);
            }
        } catch(Exception e) {
			e.printStackTrace();
			sender.sendMessage(ChatColor.RED + "Internal error!");
		}
	}

    @SuppressWarnings("unchecked")
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<String>();
        if (args.length == 1) {
            for (Method m : commandsNoAlias.values()) {
                Subcommand c = m.getAnnotation(Subcommand.class);
                if (c.visible() && m.getName().startsWith(args[0])) {
                    out.add(m.getName());
                }
            }
        } else if (args.length > 1) {
            try {
                Method m = tabCompleters.get(args[0]);
                if (m != null) {
                    out = (List)m.invoke(this, sender, Utils.stripArray(args));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return out;
    }

	private boolean hasPerms(CommandSender sender, String[] perms) {
	    if (sender instanceof ConsoleCommandSender) return true;
		for(String p:perms) {
			if(!sender.hasPermission(p)) return false;
		}
		return true;
	}

	@Subcommand(
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
            Subcommand c = method.getAnnotation(Subcommand.class);
                sender.sendMessage("[" + ((hasPerms(sender,c.permissions())) ? ChatColor.GREEN : ChatColor.RED) +
                        method.getName() + ChatColor.GRAY + " subcommand Summary]");
            sender.sendMessage(ChatColor.GRAY + method.getName() + " " + c.usage() + ChatColor.GRAY + " - " + c.description());
            sender.sendMessage(ChatColor.GRAY + "Permissions: " + (c.permissions().length==0 ? ChatColor.GREEN + "none" : Utils.join(c.permissions(), ",", 0)));
		} else {
            for (Method m : this.commandsNoAlias.values()) {
                Subcommand c = m.getAnnotation(Subcommand.class);
                if (c.visible()) {
                    sender.sendMessage(((hasPerms(sender, c.permissions())) ? ChatColor.GREEN : ChatColor.RED) +
                            m.getName() + " " + c.usage() + ChatColor.GRAY + " - " + c.description());
                }
            }
        }
	}

	@Suggestion("help")
    public List<String> helpCompletion(CommandSender sender, String[] args) {
	    if (args.length!=1) return new ArrayList<>();
        return commandsNoAlias.keySet().stream().filter(
            (x) -> x.startsWith(args[0]) && commandsNoAlias.get(x).getAnnotation(Subcommand.class).visible()
            ).collect(Collectors.toList());
    }

	@Retention(RetentionPolicy.RUNTIME)
    public @interface Subcommand{
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

	@Retention(RetentionPolicy.RUNTIME)
    public @interface Suggestion {
	    String[] value();
    }
}
