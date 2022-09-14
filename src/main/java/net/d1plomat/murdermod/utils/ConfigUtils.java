package net.d1plomat.murdermod.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import net.d1plomat.murdermod.MurderMod;
import net.d1plomat.murdermod.models.MMConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.JsonSerializer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class ConfigUtils {

    public static final String configPath = "./config/MMConfig.json";

    private static Gson gson = new Gson();

    public static MMConfig LoadConfig()
    {
        MurderMod.LOGGER.info("----------Loading config..----------");
        try {
            String json = Files.readString(Paths.get(configPath));
            MurderMod.LOGGER.info(json);
            MMConfig config = gson.fromJson(json, MMConfig.class);
            MurderMod.LOGGER.info(config.Token);
            return config;
        } catch (Exception ex) {
            MurderMod.LOGGER.error(ex.getMessage());
            MMConfig mmConfig = new MMConfig();
            mmConfig.Token = "";
            mmConfig.ChatId = 0;
            return mmConfig;
        }
    }

    public static void SaveConfig(MMConfig config)
    {
        try
        {
            String json = gson.toJson(config);
            var path = Paths.get(configPath).getParent();
            if (!Files.exists(path))
            {
                Files.createDirectories(path);
            }
            Files.write(Paths.get(configPath), json.getBytes());
        } catch (Exception exception) {}
    }
}
