package Claims.claims.utils;

import Claims.claims.models.Claim;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class ClaimUtils {

    public static void visualizeClaim(Player player, Claim claim) {
        World world = player.getWorld();
        if (!world.getName().equals(claim.getWorldName()))
            return;

        int minX = claim.getMinX();
        int minZ = claim.getMinZ();
        int maxX = claim.getMaxX();
        int maxZ = claim.getMaxZ();
        int y = player.getLocation().getBlockY();

        // Draw borders
        for (int x = minX; x <= maxX; x++) {
            player.spawnParticle(Particle.FLAME, x + 0.5, y + 1, minZ + 0.5, 1, 0, 0, 0, 0);
            player.spawnParticle(Particle.FLAME, x + 0.5, y + 1, maxZ + 0.5, 1, 0, 0, 0, 0);
        }
        for (int z = minZ; z <= maxZ; z++) {
            player.spawnParticle(Particle.FLAME, minX + 0.5, y + 1, z + 0.5, 1, 0, 0, 0, 0);
            player.spawnParticle(Particle.FLAME, maxX + 0.5, y + 1, z + 0.5, 1, 0, 0, 0, 0);
        }
    }

    public static int calculateArea(Claim claim) {
        return (claim.getMaxX() - claim.getMinX() + 1) * (claim.getMaxZ() - claim.getMinZ() + 1);
    }
}
