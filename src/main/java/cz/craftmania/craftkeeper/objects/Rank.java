package cz.craftmania.craftkeeper.objects;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Později to bude brát Rank z CraftPrisonu
 */
public enum Rank {

    TUTORIAL_A(1, "A", 0, 0, new ItemStack(Material.COBBLESTONE), "", ""),
    TUTORIAL_B(2, "B", 1000, 1, new ItemStack(Material.STONE, 1, (short) 4), "NightVision v dolech", "craftprison.mine.nightvision"),
    TUTORIAL_C(3, "C", 4000, 1, new ItemStack(Material.STONE, 1, (short) 1), "", ""),
    TUTORIAL_D(4, "D", 10000, 1, new ItemStack(Material.LAPIS_ORE), "Odemknuti vytvareni ostrova /is", "askyblock.island.create");

    private int weight;
    private String name;
    private long price;
    private int prisCoins;
    private String reward;
    private String[] array;
    private ItemStack item;

    Rank(int weight, String name, long price, int prisCoins, ItemStack item, String reward, String... array) {
        this.name = name;
        this.price = price;
        this.weight = weight;
        this.prisCoins = prisCoins;
        this.reward = reward;
        this.array = array;
        this.item = item;
    }

    Rank(int weight) {
        this.weight = weight;
    }

    public static Rank getByName(String name) {
        for (Rank r : getTypes()) {
            if (r.getName().equalsIgnoreCase(name)) {
                return r;
            }
        }
        return null;
    }

    public static Rank getByWeight(int weight) {
        for (Rank r : getTypes()) {
            if (r.getWeight() == weight) {
                return r;
            }
        }
        return null;
    }

    public static Rank[] getTypes() {
        return Rank.values();
    }

    public static Rank getLast() {
        return Rank.values()[getTypes().length - 1];
    }

    public String getName() {
        return name;
    }

    public long getPrice() {
        return this.price;
    }

    public int getWeight() {
        return weight;
    }

    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }

    public String getPermission() {
        return "craftprison.rank." + this.getName().toLowerCase(); // craftprison.rank.?
    }

    public String getPermission(Rank r) {
        return "craftprison.rank." + r.getName().toLowerCase(); // craftprison.rank.octopus
    }

    public boolean isAtLeast(Rank other) {
        return getWeight() >= other.getWeight();
    }

    public Rank getNext() {
        return this.ordinal() < Rank.values().length - 1 ? Rank.values()[this.ordinal() + 1] : null;
    }

    public int getPrisCoins() {
        return prisCoins;
    }

    public String getReward() {
        return reward;
    }

    public String[] getCommands() {
        return array;
    }

    public ItemStack getItem() {
        return item;
    }
}
