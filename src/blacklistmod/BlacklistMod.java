package blacklistmod;

import arc.*;
import arc.struct.*;
import mindustry.game.EventType.*;
import mindustry.gen.Icon;
import mindustry.mod.*;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.gen.Player;
import mindustry.Vars;
import mindustry.ui.fragments.ChatFragment;
import arc.scene.ui.layout.Table;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import com.google.gson.Gson;

public class BlacklistMod extends Mod
{
    private String jsonPath = "blacklist.json"; 
    private String jsonSettingsPath = "blacklist-settings.json";

    private ArrayList<String> blacklist;

    private void LoadBlacklist() 
    {
        String[] blacklistArray = LoadJson(jsonPath, String[].class);
        if (blacklistArray == null) 
        {
            blacklistArray = new String[0];
        }

        blacklist = new ArrayList<>(Arrays.asList(blacklistArray));
    }

    private void SaveBlacklist() 
    {
        String[] blacklistArray = blacklist.toArray(new String[0]);
        SaveJson(jsonPath, blacklistArray);
    }


    private BlacklistSettings settings;

    private class BlacklistSettings
    {
        public boolean removeColorsInNicks = true;
        public boolean lowercaseNicks = true;
        public boolean displayBlacklistedMessages = true;
    }

    private void LoadSettings()
    {
        BlacklistSettings loadedSettings = LoadJson(jsonSettingsPath, BlacklistSettings.class);
        if (loadedSettings == null) 
        {
            loadedSettings = new BlacklistSettings();
        }

        settings = loadedSettings;
    }

    private void SaveSettings()
    {
        SaveJson(jsonSettingsPath, settings);
    }


    
    private void AddMessage(Seq<String> messages, Player player, String message)
    {
        if (message == null || message == "") return;

        if (player == null || player.name == null || player.name.isEmpty())
        {
            messages.add("[white]" + message);
            return;
        }

        messages.add(Vars.netServer.chatFormatter.format(player, message));
    }

    private void RefreshChat(ChatFragment chatFragment, Seq<String> messages) 
    {
        if (chatFragment == null || messages == null) return;

        chatFragment.clearMessages();

        for (String message : messages) 
        {
            chatFragment.addMessage(message);
        }
    }


    private BaseDialog RefreshDialog(BaseDialog dialog) 
    {
        dialog.cont.clear();

        for (String name : blacklist)
        {
            dialog.cont.button(Icon.pencil, () ->
            {
                Vars.ui.showTextInput("Edit Blacklist Name", "Enter new name:", name, (String newName) -> 
                {
                    if (newName != null && !newName.isEmpty()) 
                    {
                        int id = blacklist.indexOf(name);
                        blacklist.set(id , newName);
                        
                        SaveBlacklist();
                        RefreshDialog(dialog);
                    }
                });
            }).width(50f).height(50f);

            dialog.cont.add(name).width(200f).height(50f);

            dialog.cont.button(Icon.trash, () -> 
            {
                blacklist.remove(name);
                SaveBlacklist();
                RefreshDialog(dialog);
            }).width(50f).height(50f);

            dialog.cont.row();
        }

        return dialog;
    }

    Table settingsTable;

    private void RefreshSettingsTable(BaseDialog blacklistDialog)
    {
        if (settingsTable == null) return;

        settingsTable.clear();

        settingsTable.row();

        settingsTable.check("Remove [.] content in nicks", settings.removeColorsInNicks, (boolean value) ->
        {
            settings.removeColorsInNicks = value;
            SaveSettings();
        });

        settingsTable.row();

        settingsTable.check("Lowercase nicks", settings.lowercaseNicks, (boolean value) ->
        {
            settings.lowercaseNicks = value;
            SaveSettings();
        });

        settingsTable.row();

        settingsTable.check("Display warnings instead of blacklisted messages", settings.displayBlacklistedMessages, (boolean value) ->
        {
            settings.displayBlacklistedMessages = value;
            SaveSettings();
        });

        settingsTable.row();

        settingsTable.button("Blacklist", Icon.chat, () ->
        {
            RefreshDialog(blacklistDialog);
            blacklistDialog.show();
        }).width(300f).height(50f);
    }


    @Override
    public void init()
    {
        LoadBlacklist();
        LoadSettings();

        Events.on(ClientLoadEvent.class, e -> 
        {
            BaseDialog blacklistDialog = new BaseDialog("Blacklist");

            blacklistDialog.addCloseButton();

            blacklistDialog.buttons.button(Icon.add, () ->
            {
                Vars.ui.showTextInput("Add to Blacklist", "Enter name to blacklist:", "", (String name) -> 
                {
                    blacklist.add(name);
                    SaveBlacklist();
                    RefreshDialog(blacklistDialog);
                });
            });

            blacklistDialog.buttons.button("Clean blacklist", () ->
            {
                DeleteFile(jsonPath);
                LoadBlacklist();

                RefreshDialog(blacklistDialog);
            });

            blacklistDialog.buttons.button("Reset blacklist settings", () ->
            {
                DeleteFile(jsonSettingsPath);
                LoadSettings();

                RefreshSettingsTable(blacklistDialog);
            });

            Vars.ui.settings.addCategory("Blacklist", t ->
            {
                settingsTable = t;
                RefreshSettingsTable(blacklistDialog);
            });
        });


        Events.on(ClientServerConnectEvent.class, e ->
        {
            ChatFragment chatFragment = Vars.ui.chatfrag;
            Seq<String> messages = new Seq<>();

            Events.run(Trigger.draw, () ->{
                RefreshChat(chatFragment, messages);
            });

            Events.on(PlayerChatEvent.class, chatEvent -> 
            {
                if (chatEvent.message == null) return;
                if (chatEvent.player == null)
                {
                    AddMessage(messages, null, chatEvent.message);
                    return;
                }

                Player player = chatEvent.player;
                String message = chatEvent.message;


                if (IsBlacklisted(player))
                {
                    if (settings.displayBlacklistedMessages) 
                    {
                        AddMessage(messages, null, "[red]Blacklisted user: [white]" + player.name);
                    } 
                    return;
                } 

                AddMessage(messages, player, message);
            });
        });
    }

    private boolean IsBlacklisted(Player player)
    {
        String editedPlayer = player.name;

        if (settings.removeColorsInNicks) 
        {
            editedPlayer = GetNameWithoutColor(editedPlayer);
        }
        if (settings.lowercaseNicks) 
        {
            editedPlayer = GetNameLowercase(editedPlayer);
        }

        for (String name : blacklist) 
        {
            if (editedPlayer.contains(name))
            {
                return true;
            }
        }

        return false;
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


    private String GetNameWithoutColor(String name) 
    {
        if (name == null || name.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        boolean isColor = false;

        for (char c : name.toCharArray()) 
        {
            if (c == '[') 
            {
                isColor = true;
            }
            else if (c == ']') 
            {
                isColor = false;
            } 
            else if (!isColor) 
            {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    private String GetNameLowercase(String name) 
    {
        if (name == null || name.isEmpty()) return "";

        return name.toLowerCase();
    }
}