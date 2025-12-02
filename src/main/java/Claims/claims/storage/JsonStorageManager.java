package Claims.claims.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import Claims.claims.models.Claim;
import Claims.claims.models.Party;
import Claims.claims.models.PlayerData;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class JsonStorageManager {
    private final JavaPlugin plugin;
    private final Gson gson;
    private final File claimsFile;
    private final File partiesFile;
    private final File playersFile;

    public JsonStorageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.claimsFile = new File(plugin.getDataFolder(), "claims.json");
        this.partiesFile = new File(plugin.getDataFolder(), "parties.json");
        this.playersFile = new File(plugin.getDataFolder(), "players.json");

        initFiles();
    }

    private void initFiles() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        createIfNotExists(claimsFile);
        createIfNotExists(partiesFile);
        createIfNotExists(playersFile);
    }

    private void createIfNotExists(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write("[]");
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create file " + file.getName(), e);
            }
        }
    }

    // --- Claims ---

    public CompletableFuture<List<Claim>> loadClaims() {
        return CompletableFuture.supplyAsync(() -> {
            try (Reader reader = new FileReader(claimsFile)) {
                Type listType = new TypeToken<ArrayList<Claim>>() {
                }.getType();
                List<Claim> claims = gson.fromJson(reader, listType);
                return claims != null ? claims : new ArrayList<>();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load claims", e);
                return new ArrayList<>();
            }
        });
    }

    public CompletableFuture<Void> saveClaims(Collection<Claim> claims) {
        return CompletableFuture.runAsync(() -> {
            try (Writer writer = new FileWriter(claimsFile)) {
                gson.toJson(claims, writer);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save claims", e);
            }
        });
    }

    // --- Parties ---

    public CompletableFuture<List<Party>> loadParties() {
        return CompletableFuture.supplyAsync(() -> {
            try (Reader reader = new FileReader(partiesFile)) {
                Type listType = new TypeToken<ArrayList<Party>>() {
                }.getType();
                List<Party> parties = gson.fromJson(reader, listType);
                return parties != null ? parties : new ArrayList<>();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load parties", e);
                return new ArrayList<>();
            }
        });
    }

    public CompletableFuture<Void> saveParties(Collection<Party> parties) {
        return CompletableFuture.runAsync(() -> {
            try (Writer writer = new FileWriter(partiesFile)) {
                gson.toJson(parties, writer);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save parties", e);
            }
        });
    }

    // --- PlayerData ---

    public CompletableFuture<List<PlayerData>> loadPlayerData() {
        return CompletableFuture.supplyAsync(() -> {
            try (Reader reader = new FileReader(playersFile)) {
                Type listType = new TypeToken<ArrayList<PlayerData>>() {
                }.getType();
                List<PlayerData> data = gson.fromJson(reader, listType);
                return data != null ? data : new ArrayList<>();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load player data", e);
                return new ArrayList<>();
            }
        });
    }

    public CompletableFuture<Void> savePlayerData(Collection<PlayerData> data) {
        return CompletableFuture.runAsync(() -> {
            try (Writer writer = new FileWriter(playersFile)) {
                gson.toJson(data, writer);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save player data", e);
            }
        });
    }
}
