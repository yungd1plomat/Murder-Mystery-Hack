package net.d1plomat.murdermod.utils;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.registry.Registry;

public class ItemUtils {
    public static Item getItemFromNameOrID(String nameOrId)
    {
        if(isInteger(nameOrId))
        {
            // There is no getOrEmpty() for raw IDs, so this detects when the
            // Registry defaults and returns null instead
            int id = Integer.parseInt(nameOrId);
            Item item = Registry.ITEM.get(id);
            if(id != 0 && Registry.ITEM.getRawId(item) == 0)
                return null;

            return item;
        }

        try
        {
            return Registry.ITEM.getOrEmpty(new Identifier(nameOrId))
                    .orElse(null);

        }catch(InvalidIdentifierException e)
        {
            return null;
        }
    }

    public static boolean isInteger(String s)
    {
        try
        {
            Integer.parseInt(s);
            return true;

        }catch(NumberFormatException e)
        {
            return false;
        }
    }
}
