package moe.evelyn.albatross.rules;

import java.util.HashMap;

public enum RuleType
{
    COMMAND("command", "cmd", "c"),
    SIGN("sign", "s"),
    ALL("all", "*", "a")
    ;

    private String[] aliases;

    RuleType(String ...aliases) {
        this.aliases = aliases;
    }

    private static HashMap<String, RuleType> aliasMap = new HashMap<>();

    static {
        for (RuleType value : RuleType.values()) {
            for (String alias : value.aliases) {
                aliasMap.put(alias, value);
            }
        }
    }

    public static RuleType fromString(String string) {
        return aliasMap.get(string.toLowerCase());
    }
}
