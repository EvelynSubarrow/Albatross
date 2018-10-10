package moe.evelyn.albatross.rules;

public enum GroupEffect
{
    ACCEPT("§aACCEPT§r"), REJECT("§cREJECT§r");

    private final String fullColour;

    GroupEffect(String fullColour) {
        this.fullColour = fullColour;
    }

    public String getStringColoured() {
        return this.fullColour;
    }
}
