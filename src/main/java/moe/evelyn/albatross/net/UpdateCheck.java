package moe.evelyn.albatross.net;

import moe.evelyn.albatross.Main;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class UpdateCheck
{
    private URL url;
    private Main main;

    private VersionEntry currentVersion = null;

    public UpdateCheck(Main main) {
        try{
            this.url = new URL("https://albatross.evelyn.moe/updates.json");
        } catch(Exception e) {
            e.printStackTrace();
        }
        this.main = main;
    }

    public synchronized VersionEntry getCurrentVersion()
    {
        return this.currentVersion;
    }

    public void startUpdateCheck() {
        main.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // If the user has update checking off, don't do anything when running
                if (!main.config.updateCheck) {
                    return;
                }
                try {
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                    connection.setReadTimeout(1000);
                    connection.setRequestMethod("GET");
                    connection.setUseCaches(false);
                    connection.setDoInput(true);

                    JSONParser parser = new JSONParser();
                    JSONArray document = (JSONArray) parser.parse(new InputStreamReader(connection.getInputStream()));
                    for (Object o : document) {
                        VersionEntry ve = new VersionEntry((JSONObject)o);
                        if (main.getDescription().getVersion().equals(ve.version)) {
                            currentVersion = ve;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1000*60*60); //TODO: 1200 Immediately, then every sixty minutes hence
    }

    public void maybeUpdateNotify(CommandSender sender) {
        if (!sender.hasPermission("commandspy.use")) return;
        UpdateCheck.VersionEntry entry = this.getCurrentVersion();
        if (entry != null && main.config.updateCheck) {
            if (entry.isAhead) {
                sender.sendMessage("§8[§9Commandspy§8] §3This version is marked as a pre-release");
            }
            if (entry.hasBugs) {
                sender.sendMessage("§8[§9Commandspy§8] §cThis version has bugs");
                sender.sendMessage("§8[§9Commandspy§8] §c" + entry.bugsDescription);
            }
            if (entry.hasUpdate) {
                sender.sendMessage("§8[§9Commandspy§8] §3An update is available");
            }
        }
    }

    public static final class VersionEntry
    {
        public VersionEntry(JSONObject map) throws IllegalAccessException {
            for(Field f: this.getClass().getFields()) {
                Object value = map.get(f.getName());
                if(value!=null) {
                    f.set(this, value);
                }
            }
        }

        public String version          = "0.0.0";
        public String dateTimeReleased = "";
        public String bugsDescription  = "";
        public boolean hasBugs          = false;
        public boolean hasUpdate        = false;
        public boolean isAhead          = false;
    }
}
