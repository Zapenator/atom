package org.shotrush.atom.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class DataManager {
    private final JavaPlugin plugin;
    private final Gson gson;
    private final Path dataFolder;
    
    public DataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.dataFolder = plugin.getDataFolder().toPath();
        
        try {
            Files.createDirectories(dataFolder);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create data folder: " + e.getMessage());
        }
    }
    
    public <T> CompletableFuture<Void> saveAsync(String fileName, T data) {
        return CompletableFuture.runAsync(() -> {
            try {
                Path file = dataFolder.resolve(fileName);
                String json = gson.toJson(data);
                Files.writeString(file, json);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save " + fileName + ": " + e.getMessage());
            }
        });
    }
    
    public <T> CompletableFuture<T> loadAsync(String fileName, Class<T> clazz) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path file = dataFolder.resolve(fileName);
                if (!Files.exists(file)) return null;
                
                String json = Files.readString(file);
                return gson.fromJson(json, clazz);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to load " + fileName + ": " + e.getMessage());
                return null;
            }
        });
    }
    
    public <T> void save(String fileName, T data) {
        try {
            Path file = dataFolder.resolve(fileName);
            String json = gson.toJson(data);
            Files.writeString(file, json);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save " + fileName + ": " + e.getMessage());
        }
    }
    
    public <T> T load(String fileName, Class<T> clazz) {
        try {
            Path file = dataFolder.resolve(fileName);
            if (!Files.exists(file)) return null;
            
            String json = Files.readString(file);
            return gson.fromJson(json, clazz);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load " + fileName + ": " + e.getMessage());
            return null;
        }
    }
    
    public boolean exists(String fileName) {
        return Files.exists(dataFolder.resolve(fileName));
    }
    
    public void delete(String fileName) {
        try {
            Files.deleteIfExists(dataFolder.resolve(fileName));
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to delete " + fileName + ": " + e.getMessage());
        }
    }
}
