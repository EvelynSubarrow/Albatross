package moe.evelyn.albatross.rules;

import moe.evelyn.albatross.utils.Configurable;
import moe.evelyn.albatross.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.regex.Pattern;

public class Rule implements Configurable
{
    private Pattern senderPattern;
    private Pattern messagePattern;

    public RuleType ruleType;

    public String senderPatternFamiliar;
    public String senderPatternText;

    public String messagePatternFamiliar;
    public String messagePatternText;

    public Rule(ConfigurationSection section) {
        this.loadFrom(section);
        this.compile();
    }

    public Rule(RuleType ruleType, String senderPatternFamiliar, String messagePatternFamiliar) {
        this.ruleType = ruleType;
        this.senderPatternFamiliar = senderPatternFamiliar;
        this.messagePatternFamiliar = messagePatternFamiliar;
        this.compile();
    }

    public void compile() {
        this.senderPattern = interpretPattern(senderPatternFamiliar);
        this.messagePattern = interpretPattern(messagePatternFamiliar);
    }

    public boolean matches(CommandSender sender, String[] lines) {
        return (ruleType==RuleType.SIGN || ruleType==RuleType.ALL) &&
                (senderPattern.matcher(sender.getName()).find() || senderPattern.matcher(Utils.getUUID(sender)).find()) &&
                messagePattern.matcher(Utils.join(lines, "\n", 0)).find();
    }

    public boolean matches(CommandSender sender, String message) {
        return (ruleType==RuleType.COMMAND || ruleType==RuleType.ALL) &&
                (senderPattern.matcher(sender.getName()).find() || senderPattern.matcher(Utils.getUUID(sender)).find()) &&
                messagePattern.matcher(message).find();
    }

    public Pattern interpretPattern(String pts) {
        StringBuilder currentToken = new StringBuilder();
        StringBuilder safeBuilder = new StringBuilder();
        for(char c : pts.toCharArray()) {
            if (c=='*' || c=='?') {
                if (currentToken.length()>0) {
                    safeBuilder.append(Pattern.quote(currentToken.toString()));
                    currentToken = new StringBuilder();
                }
                safeBuilder.append('.');
                safeBuilder.append(c);
            } else if (c=='|') {
                safeBuilder.append('\\');
                safeBuilder.append('n');
            } else {
                currentToken.append(c);
            }
        }
        if (currentToken.length()>0)
            safeBuilder.append(Pattern.quote(currentToken.toString()));
        return Pattern.compile("^" + safeBuilder.toString());
    }

    public String toStringColoured() {
        return ruleType.toString() + " " + senderPatternFamiliar + " §b" + messagePatternFamiliar;
    }

    @Override
    public void loadFrom(ConfigurationSection s) {
        this.ruleType = RuleType.valueOf(s.getString("ruleType"));
        this.messagePatternFamiliar = s.getString("messagePatternFamiliar");
        this.messagePatternText = s.getString("messagePatternText");
        this.senderPatternFamiliar = s.getString("senderPatternFamiliar");
        this.senderPatternText = s.getString("senderPatternText");
    }

    @Override
    public void applyTo(ConfigurationSection s) {
        s.set("ruleType", this.ruleType.toString());
        s.set("messagePatternFamiliar", this.messagePatternFamiliar);
        s.set("messagePatternText", this.messagePatternText);
        s.set("senderPatternFamiliar", this.senderPatternFamiliar);
        s.set("senderPatternText", this.senderPatternText);
    }
}
