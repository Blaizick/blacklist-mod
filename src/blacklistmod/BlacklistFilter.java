package blacklistmod;

import mindustry.gen.Player;
import mindustry.net.Administration.ChatFilter;


public class BlacklistFilter implements ChatFilter
{
    @Override
    public String filter(Player player, String message) 
    {
        if (player == null || player.name == null || player.name.isEmpty()) 
        {
            return message;
        }

        String editedPlayer = player.name;

        if (SerializationUtils.instance.settings.removeColorsInNicks)
        {
            editedPlayer = GetNameWithoutColor(editedPlayer);
        }
        if (SerializationUtils.instance.settings.lowercaseNicks)
        {
            editedPlayer = GetNameLowercase(editedPlayer);
        }

        for (String name : SerializationUtils.instance.blacklist) 
        {
            if (SerializationUtils.instance.settings.lowercaseNicks)
            {
                name = GetNameLowercase(name);
            }

            if (editedPlayer.contains(name)) 
            {
                if (SerializationUtils.instance.settings.displayBlacklistedMessages)
                {
                    return "[red]Message from blacklisted player: " + player.name;
                }

                return null;
            }
        }

        return message;
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
