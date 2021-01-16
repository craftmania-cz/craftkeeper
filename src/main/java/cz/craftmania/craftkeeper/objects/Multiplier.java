package cz.craftmania.craftkeeper.objects;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class Multiplier {

    private @Getter MultiplierType type; // Typ multiplieru
    private @Getter String target; // @a = Event; Global = Hráč, který aktivoval globální boost; Personal = Hráč, kterému se bude boostit
    private @Getter UUID targetUUID; // Event = null; Global = Hráč uuid; Personal = Hráč uuid;
    private @Getter long length; // Délka multiplieru v MS; U global a event tam je System.currentMill...() + MS
    private @Getter @Setter long remainingLength; // Délka zbývajícího času multiplieru v MS
    private @Getter double percentageBoost; // O kolik to bude boostovat ceny v procentech - 134% = 1.34; 0% = 0; 50% = 0.5; etc...
    private @Getter long internalID;

    public Multiplier(MultiplierType type, String target, UUID targetUUID, long length, long remainingLength, double percentageBoost) {
        this.type = type;
        this.target = target;
        this.targetUUID = targetUUID;
        this.length = length;
        this.remainingLength = remainingLength;
        this.percentageBoost = percentageBoost;
        this.internalID = System.currentTimeMillis();
    }

    public Multiplier(MultiplierType type, String target, UUID targetUUID, long length, long remainingLength, double percentageBoost, long internalID) {
        this.type = type;
        this.target = target;
        this.targetUUID = targetUUID;
        this.length = length;
        this.remainingLength = remainingLength;
        this.percentageBoost = percentageBoost;
        this.internalID = internalID;
    }

    public String getRemainingTimeReadable() {
        long ms = remainingLength;
        int minutes = (int) ((ms / (1000 * 60)) % 60);
        int hours = (int) ms / (1000 * 60 * 60);

        if (hours == 0 && minutes == 0)
            return "<1m";

        String str = "";
        if (hours != 0)
            str += hours + "h";
        if (minutes != 0)
            str += minutes + "m";
        return str;
    }

    @Override
    public String toString() {
        String returnValue = "{";

        try {
            returnValue += "type=" + type.name() + ";";
        } catch (Exception exception) {
        }
        try {
            returnValue += "target=" + target + ";";
        } catch (Exception exception) {
        }
        try {
            returnValue += "targetUUID=" + targetUUID.toString() + ";";
        } catch (Exception exception) {
        }
        try {
            returnValue += "length=" + length + ";";
        } catch (Exception exception) {
        }
        try {
            returnValue += "remainingLength=" + remainingLength + ";";
        } catch (Exception exception) {
        }
        try {
            returnValue += "percentageBoost=" + percentageBoost + ";";
        } catch (Exception exception) {
        }
        try {
            returnValue += "internalID=" + internalID + ";";
        } catch (Exception exception) {
        }

        returnValue += "}";
        return returnValue;
    }
}
