package Claims.claims.utils;

import Claims.claims.Claims;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;

public class ClaimTool {
    private static final NamespacedKey CLAIM_TOOL_KEY = new NamespacedKey(Claims.getInstance(), "claim_tool");

    public static ItemStack getTool() {
        Material mat = Claims.getInstance().getConfigManager().getClaimItem();
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Claims.getInstance().getConfigManager().getClaimItemName());
            meta.setLore(Collections.singletonList("§7Right-click to set corners."));
            meta.getPersistentDataContainer().set(CLAIM_TOOL_KEY, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isClaimTool(ItemStack item) {
        if (item == null || !item.hasItemMeta())
            return false;
        return item.getItemMeta().getPersistentDataContainer().has(CLAIM_TOOL_KEY, PersistentDataType.BYTE);
    }
}
