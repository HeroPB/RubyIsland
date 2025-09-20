package me.herohd.rubyisland.utils;


import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class NBTEditor {
    public static boolean hasNBT(ItemStack stack, String tag){
        if(stack == null) return false;
        if(stack.getType().equals(Material.AIR)) return false;
        return new NBTItem(stack).hasKey("rubyisland-" + tag);
    }

    public static ItemStack addNBT(ItemStack stack, String tag){
        NBTItem item = new NBTItem(stack);
        item.setString("rubyisland-" + tag, tag);
        return item.getItem();
    }

    public static ItemStack addNBT(ItemStack stack, String key, String tag){
        NBTItem item = new NBTItem(stack);
        item.setString("rubyisland-" + key, tag);
        return item.getItem();
    }

    public static String getNBT(ItemStack stack, String key) {
        if(stack == null) return null;
        if(stack.getType().equals(Material.AIR)) return null;
        NBTItem item = new NBTItem(stack);
        return item.getString("rubyisland-" + key);
    }

    /**
     * Aggiunge un tag NBT di tipo Integer a un item.
     * @param stack L'item da modificare.
     * @param key La chiave del tag.
     * @param value Il valore intero da salvare.
     * @return L'item con il nuovo tag.
     */
    public static ItemStack addNBT(ItemStack stack, String key, int value) {
        NBTItem item = new NBTItem(stack);
        item.setInteger("rubyisland-" + key, value);
        return item.getItem();
    }
    public static ItemStack addNBT(ItemStack stack, String key, double value) {
        NBTItem item = new NBTItem(stack);
        item.setDouble("rubyisland-" + key, value);
        return item.getItem();
    }

    /**
     * Legge un tag NBT di tipo Integer da un item.
     * @param stack L'item da cui leggere.
     * @param key La chiave del tag.
     * @return Il valore intero, o 0 se il tag non esiste.
     */
    public static double getNBTDouble(ItemStack stack, String key) {
        if (stack == null || stack.getType() == Material.AIR) return 0;
        NBTItem item = new NBTItem(stack);
        // Restituisce 0 come default se il tag non è presente
        return item.getDouble("rubyisland-" + key);
    }
    public static int getNBTInt(ItemStack stack, String key) {
        if (stack == null || stack.getType() == Material.AIR) return 0;
        NBTItem item = new NBTItem(stack);
        // Restituisce 0 come default se il tag non è presente
        return item.getInteger("rubyisland-" + key);
    }

    public static ItemStack removeNBT(ItemStack stack, String tag){
        NBTItem item = new NBTItem(stack);
        item.removeKey("rubyisland-" + tag);
        return item.getItem();
    }
}