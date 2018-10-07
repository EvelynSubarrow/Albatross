package moe.evelyn.albatross.utils;

import java.lang.reflect.Field;
import java.util.List;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;


public abstract class AnnotationConfig extends AnnotationCommands implements Configurable
{

	/*
	 * Saving and loading methods
	 */
	public ConfigurationSection save(ConfigurationSection c){
		for(Field f:this.getClass().getFields()){
			if(f.isAnnotationPresent(config.class)){
				try{
                    if(f.get(this) instanceof Location){
                        c.set(f.getName() + ".world", ((Location)f.get(this)).getWorld().getName());
                        c.set(f.getName() + ".x", ((Location)f.get(this)).getX());
                        c.set(f.getName() + ".y", ((Location)f.get(this)).getY());
                        c.set(f.getName() + ".z", ((Location)f.get(this)).getZ());
                    }else if(f.get(this) instanceof List<?>){
                        c.set(f.getName(),f.get(this));
                    }else if(f.get(this) instanceof Configurable){
                        c.createSection(f.getName());
                        ((Configurable)f.get(this)).applyTo(c.getConfigurationSection(f.getName()));
                    }else{
                        c.set(f.getName(), f.get(this));
                    }
				}catch(Exception e){
                    e.printStackTrace();
				}
			}
		}
		return c;
	}

	public void load(ConfigurationSection c){
		for(Field f:this.getClass().getFields()){
			if(f.isAnnotationPresent(config.class)&&c.isSet(f.getName())){
				try{
					if(f.getType().equals(String.class)) {
						f.set(this, c.getString(f.getName()));
					} else if(f.getType().equals(Long.class)){
						f.set(this, c.getLong(f.getName()));
					}else if(f.getType().equals(Integer.class)){
						f.set(this, c.getInt(f.getName()));
					}else if(f.getType().equals(Double.class)){
						f.set(this, c.getDouble(f.getName()));
					}else if(f.getType().equals(Boolean.class)) {
						f.set(this, c.getBoolean(f.getName()));
                    }else if(f.getType().equals(Location.class)){
						f.set(this, new Location(
						    Bukkit.getServer().getWorld(c.getString(f.getName() + ".world")),
						    c.getDouble(f.getName() + ".x"),
						    c.getDouble(f.getName() + ".y"),
						    c.getDouble(f.getName() + ".z")
						    ));
					}else if(f.get(this) instanceof Configurable){
						((Configurable)f.get(this)).loadFrom(c.getConfigurationSection(f.getName()));
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void applyTo(ConfigurationSection s) {
		save(s);
	}

	@Override
	public void loadFrom(ConfigurationSection s) {
		load(s);
	}
}
