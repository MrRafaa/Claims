package Claims.claims.gui;

import Claims.claims.Claims;
import Claims.claims.models.Claim;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class PlayerClaimsGui extends PaginatedGui {

    public PlayerClaimsGui(Claims plugin, Player player, List<Claim> claims) {
        super(plugin, player, claims, "Your Claims");
        updateInventory();
    }

    @Override
    protected ItemStack createItem(Object item) {
        Claim claim = (Claim) item;
        ItemStack stack = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            String name = claim.getName() != null ? claim.getName() : "Unnamed Claim";
            meta.setDisplayName("§a" + name);
            meta.setLore(Arrays.asList(
                    "§7World: " + claim.getWorldName(),
                    "§7X: " + claim.getMinX() + " Z: " + claim.getMinZ(),
                    "§eClick to view info"));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    @Override
    protected void onItemClick(Object item, InventoryClickEvent event) {
        Claim claim = (Claim) item;
        new ClaimDetailGui(plugin, player, claim);
    }
}
