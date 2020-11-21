package cz.craftmania.craftkeeper.objects;

public enum MultiplierType {
    EVENT,
    GLOBAL,
    PERSONAL;

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
