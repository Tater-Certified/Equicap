package com.github.tatercertified.equicap;

import com.github.tatercertified.equicap.interfaces.MobCapAccess;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.world.entity.MobCategory;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

public final class Config {
    private static final Config INSTANCE = new Config();
    private static final Path CONFIG_PATH = FabricLoaderImpl.INSTANCE.getConfigDir().resolve("equicap.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Values
    @Expose
    EnumMap<MobCategory, Integer> spawnGroupCapacityOverrides = new EnumMap<>(MobCategory.class);
    @Expose
    MobCapMerge mergeMode = MobCapMerge.VanillaLike;

    public static Config getInstance() {
        return INSTANCE;
    }

    public void loadConfig() {
        if (Files.exists(CONFIG_PATH)) {
            try (FileReader reader = new FileReader(CONFIG_PATH.toFile())) {
                Config config = GSON.fromJson(reader, TypeToken.get(Config.class));
                parse(config);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            saveConfig();
        }
    }

    public void saveConfig() {
        String data = GSON.toJson(this);
        try (FileWriter writer = new FileWriter(CONFIG_PATH.toFile(), false)) {
            writer.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void parse(Config fromJson) {
        this.spawnGroupCapacityOverrides = fromJson.spawnGroupCapacityOverrides;
        for (Map.Entry<MobCategory, Integer> entry : this.spawnGroupCapacityOverrides.entrySet()) {
            ((MobCapAccess)(Object)entry.getKey()).setMobCapSize(entry.getValue());
        }

        if (fromJson.mergeMode != null) {
            this.mergeMode = fromJson.mergeMode;
        }
    }
}
