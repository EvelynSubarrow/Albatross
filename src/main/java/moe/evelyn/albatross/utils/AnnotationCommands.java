package moe.evelyn.albatross.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AnnotationCommands extends SubCommandExecutor{
	
	@command(
		maximumArgsLength=0,
		description="null command",
		permissions="*",
        visible=false
    )
	public void Null(CommandSender sender,String[] args){
		for(Field f:this.getClass().getFields()){
			if(f.isAnnotationPresent(config.class)){
				try{
					if(f.getType()==Location.class){
						if(f.get(this)!=null){
						sender.sendMessage(f.getName() + ": " +
                            ((Location)f.get(this)).getWorld().getName() + "," +
                            ((Location)f.get(this)).getBlockX() + "," +
                            ((Location)f.get(this)).getBlockY() + "," +
                            ((Location)f.get(this)).getBlockZ());
						}else{
							sender.sendMessage(ChatColor.GRAY + f.getName() + ": " + "null");
						}
					}else{
						if(f.get(this).toString().equals("")||f.get(this).toString().equals("-1")){
							sender.sendMessage(ChatColor.GRAY + f.getName() + ": " + f.get(this).toString());
						}else{
							sender.sendMessage(f.getName() + ": " + f.get(this).toString());
						}
					}
				}catch(Exception e){

				}
			}
		}
	}

@command(
		maximumArgsLength=1000,
		minimumArgsLength=2,
		usage="<name> <value>",
		description="Changes settings.",
		permissions="*"
		)
    public void set(CommandSender sender,String[] args){
        for(Field f:this.getClass().getFields()){
            if(f.getName().equalsIgnoreCase(args[0])){
                if(f.isAnnotationPresent(config.class)){
                    if(!f.getAnnotation(config.class).settable()) return;
                    try{
                        if(f.get(this) instanceof String){
                            f.set(this, Utils.join(args," ",1));
                        }else if(f.get(this) instanceof Long){
                            f.set(this, Long.parseLong(args[1]));
                        }else if(f.get(this) instanceof Integer){
                            f.set(this, Integer.parseInt(args[1]));
                        }else if(f.get(this) instanceof Double){
                            f.set(this, Double.parseDouble(args[1]));
                        }else if(f.get(this) instanceof Boolean){
                            f.set(this, Boolean.parseBoolean(args[1]));
                        }else if(f.getType()==Location.class){
                            if(args[1].equalsIgnoreCase("me")){
                                f.set(this, ((Player)sender).getLocation());
                            }else if(args[1].equalsIgnoreCase("null")){
                                f.set(this, null);
                            }else{
                                sender.sendMessage("fail.");
                            }
                        }else if(f.getType()==SubCommandExecutor.class){
                            ((SubCommandExecutor)f.get(this)).onCommand(sender, Utils.stripArray(args));
                            return;
                        }
                        sender.sendMessage(ChatColor.GREEN + "Set " + f.getName() + " to " + Utils.join(args," ",1));

                    }catch(Exception e){
                        sender.sendMessage(ChatColor.RED + "An error occurred!");
                        e.printStackTrace();
                    }
                return;
                }
            }
        }
    }

	@command(
		maximumArgsLength=2,
		minimumArgsLength=1,
		usage="<name>",
		description="Clears a value.",
		permissions="*"
		)
	public void clear(CommandSender sender,String[] args){
		for(Field f:this.getClass().getFields()){
			if(f.getName().equalsIgnoreCase(args[0])){
				if(f.isAnnotationPresent(config.class)){
					if(!f.getAnnotation(config.class).settable()) return;
					try{
						f.set(this, null);
						sender.sendMessage(ChatColor.GREEN + "Set " + f.getName() + " to null");

					}catch(Exception e){
						sender.sendMessage(ChatColor.RED + "An error occurred!");
						e.printStackTrace();
					}
					return;
				}
			}
		}
	}

    @Retention(RetentionPolicy.RUNTIME)
    public @interface config{
        String usage() default "";
        boolean comparison() default false;
        boolean settable() default true;
    }
}
