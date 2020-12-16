package cz.craftmania.craftkeeper.objects;

public enum MultiplierType {
    EVENT,
    GLOBAL,
    PERSONAL;

    public static MultiplierType getByName(String name) {
        switch (name.toLowerCase()) {
            case "event":
                return MultiplierType.EVENT;
            case "global":
                return MultiplierType.GLOBAL;
            case "personal":
                return MultiplierType.PERSONAL;
        }
        return null;
    }

    public String translate() {
        switch (this) {
            case EVENT:
                return "Event";
            case GLOBAL:
                return "Globální";
            case PERSONAL:
                return "Osobní";
        }
        return "";
    }
}
