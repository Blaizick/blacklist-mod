package blacklistmod;

import arc.*;
import mindustry.game.EventType.*;
import mindustry.gen.Icon;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.Vars;
import arc.scene.ui.layout.Table;


public class UI 
{
    private BaseDialog RefreshDialog(BaseDialog dialog) 
    {
        dialog.cont.clear();

        for (String name : SerializationUtils.instance.blacklist) 
        {
            dialog.cont.button(Icon.pencil, () ->
            {
                Vars.ui.showTextInput("Edit Blacklist Name", "Enter new name:", name, (String newName) -> 
                {
                    if (newName != null && !newName.isEmpty()) 
                    {
                        int id = SerializationUtils.instance.blacklist.indexOf(name);
                        SerializationUtils.instance.blacklist.set(id , newName);
                        
                        SerializationUtils.instance.SaveBlacklist();
                        RefreshDialog(dialog);
                    }
                });
            }).width(50f).height(50f);

            dialog.cont.add(name).width(200f).height(50f);

            dialog.cont.button(Icon.trash, () -> 
            {
                SerializationUtils.instance.blacklist.remove(name);
                SerializationUtils.instance.SaveBlacklist();

                RefreshDialog(dialog);
            }).width(50f).height(50f);

            dialog.cont.row();
        }

        return dialog;
    }

    private Table settingsTable;

    private void RefreshSettingsTable(BaseDialog blacklistDialog)
    {
        if (settingsTable == null) return;

        settingsTable.clear();

        settingsTable.row();

        settingsTable.check("Remove [.] content in nicks", SerializationUtils.instance.settings.removeColorsInNicks, (boolean value) ->
        {
            SerializationUtils.instance.settings.removeColorsInNicks = value;
            SerializationUtils.instance.SaveSettings();
        });

        settingsTable.row();

        settingsTable.check("Lowercase nicks", SerializationUtils.instance.settings.lowercaseNicks, (boolean value) ->
        {
            SerializationUtils.instance.settings.lowercaseNicks = value;
            SerializationUtils.instance.SaveSettings();
        });

        settingsTable.row();

        settingsTable.check("Display warnings instead of blacklisted messages", SerializationUtils.instance.settings.displayBlacklistedMessages, (boolean value) ->
        {
            SerializationUtils.instance.settings.displayBlacklistedMessages = value;
            SerializationUtils.instance.SaveSettings();
        });

        settingsTable.row();

        settingsTable.button("Blacklist", Icon.chat, () ->
        {
            RefreshDialog(blacklistDialog);
            blacklistDialog.show();
        }).width(300f).height(50f);
    }

    public UI()
    {
        Events.on(ClientLoadEvent.class, e -> 
        {
            BaseDialog blacklistDialog = new BaseDialog("Blacklist");

            blacklistDialog.addCloseButton();

            blacklistDialog.buttons.button(Icon.add, () ->
            {
                Vars.ui.showTextInput("Add to Blacklist", "Enter name to blacklist:", "", (String name) -> 
                {
                    SerializationUtils.instance.blacklist.add(name);
                    SerializationUtils.instance.SaveBlacklist();
                    RefreshDialog(blacklistDialog);
                });
            });

            blacklistDialog.buttons.button("Clear blacklist", () ->
            {
                SerializationUtils.instance.ClearBlacklist();

                RefreshDialog(blacklistDialog);
            });

            blacklistDialog.buttons.button("Reset blacklist settings", () ->
            {
                SerializationUtils.instance.ClearSettings();

                RefreshSettingsTable(blacklistDialog);
            });

            Vars.ui.settings.addCategory("Blacklist", t ->
            {
                settingsTable = t;
                RefreshSettingsTable(blacklistDialog);
            });
        });
    }
}
