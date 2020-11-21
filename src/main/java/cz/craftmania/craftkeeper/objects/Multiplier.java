package cz.craftmania.craftkeeper.objects;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class Multiplier {

    private @Getter MultiplierType multiplierType; // Typ multiplieru
    private @Getter String target; // @a = Event; Global = Hráč, který aktivoval globální boost; Personal = Hráč, kterému se bude boostit
    private @Getter UUID targetUUID; // Event = null; Global = Hráč uuid; Personal = Hráč uuid;
    private @Getter long length; // Délka multiplieru v MS
    private @Getter @Setter long remainingLength; // Délka zbývajícího času multiplieru v MS
    private @Getter double percentageBoost; // O kolik to bude boostovat ceny v procentech - 134% = 1.34; 0% = 0; 50% = 0.5; etc...
    private @Getter boolean active; // Jestli multiplier je aktivní (jen u PERSONAL multiplieru.. asi)
    private @Getter long internalID;

    public Multiplier(MultiplierType multiplierType, String target, UUID targetUUID, long length, long remainingLength, double percentageBoost, boolean active) {
        this.multiplierType = multiplierType;
        this.target = target;
        this.targetUUID = targetUUID;
        this.length = length;
        this.remainingLength = remainingLength;
        this.percentageBoost = percentageBoost;
        this.active = active;
        this.internalID = System.currentTimeMillis();
    }

    public long getRemainingMS() {
        return length - remainingLength;
    }

    @Override
    public String toString() {
        String returnValue = "{";

        returnValue += "type=" + multiplierType.name() + ";";
        returnValue += "target=" + target + ";";
        returnValue += "targetUUID=" + targetUUID.toString() + ";";
        returnValue += "length=" + length + ";";
        returnValue += "remainingLength=" + remainingLength + ";";
        returnValue += "percentageBoost=" + percentageBoost + ";";
        returnValue += "active=" + active + ";";
        returnValue += "internalID=" + internalID + ";";

        returnValue += "}";
        return returnValue;
    }
}
