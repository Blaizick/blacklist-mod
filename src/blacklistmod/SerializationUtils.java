package blacklistmod;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import com.google.gson.Gson;

public class SerializationUtils 
{
    public static SerializationUtils instance;

    private String blacklistPath = "blacklist.json"; 
    private String settingsPath = "blacklist-settings.json";
    
    
    public SerializationUtils() 
    {
        if (instance != null) 
        {
            throw new IllegalStateException("SerializationUtils instance already exists!");
        } 
        instance = this;

        LoadBlacklist();
        LoadSettings();
    }


    public ArrayList<String> blacklist;

    public void LoadBlacklist() 
    {
        String[] blacklistArray = LoadJson(blacklistPath, String[].class);
        if (blacklistArray == null) 
        {
            blacklistArray = new String[0];
        }

        blacklist = new ArrayList<>(Arrays.asList(blacklistArray));
    }

    public void SaveBlacklist() 
    {
        String[] blacklistArray = blacklist.toArray(new String[0]);
        SaveJson(blacklistPath, blacklistArray);
    }

    public void ClearBlacklist() 
    {
        blacklist.clear();
        DeleteFile(blacklistPath);
    }




    public BlacklistSettings settings;

    public class BlacklistSettings
    {
        public boolean removeColorsInNicks = true;
        public boolean lowercaseNicks = true;
        public boolean displayBlacklistedMessages = true;
    }

    public void LoadSettings()
    {
        BlacklistSettings loadedSettings = LoadJson(settingsPath, BlacklistSettings.class);
        if (loadedSettings == null) 
        {
            loadedSettings = new BlacklistSettings();
        }

        settings = loadedSettings;
    }

    public void SaveSettings()
    {
        SaveJson(settingsPath, settings);
    }

    public void ClearSettings()
    {
        settings = new BlacklistSettings();
        DeleteFile(settingsPath);
    }



    private Gson gson = new Gson();

    private void SaveJson(String path, Object data)
    {
        try (FileWriter writer = new FileWriter(path))
        {
            String json = gson.toJson(data);
            writer.write(json);
        } 
        catch (IOException e) 
        {

            e.printStackTrace();
        }
    }

    private <T> T LoadJson(String path, Class<T> type)
    {
        try (FileReader reader = new FileReader(path)) 
        {
            return gson.fromJson(reader, type);
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            return null;
        }
    }

    private void DeleteFile(String path) 
    {
        try 
        {
            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(path));
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }
}