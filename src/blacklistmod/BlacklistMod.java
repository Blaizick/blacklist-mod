package blacklistmod;

import arc.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.net.Administration.ChatFilter;
import mindustry.Vars;
import arc.util.*;
import arc.struct.*;
import mindustry.gen.*;


public class BlacklistMod extends Mod
{
    public static BlacklistMod instance;

    private Seq<ChatFilter> filters = new Seq<>();

    public void AddFilter(ChatFilter filter) 
    {
        filters.add(filter);
    }

    public void RemoveFilter(ChatFilter filter) 
    {
        filters.remove(filter);
    }

    public void ClearFilters() 
    {
        filters.clear();
    }

    public String Filter(Player player, String message) 
    {
        for (ChatFilter filter : filters) 
        {
            message = filter.filter(player, message);
        }

        return message;
    }

    @Override
    public void init()
    {
        new SerializationUtils();
        new UI();

        if (instance != null) 
        {
            throw new IllegalStateException("BlacklistMod instance already exists!");
        } 
        instance = this;


        AddFilter(new BlacklistFilter());

        Events.on(PlayerChatEvent.class, e -> 
        {
            String filteredMessage = Filter(e.player, e.message);
            if (filteredMessage == e.message)
            {
                return;
            }

            long exitTime = Time.millis() + 1000;

            new Thread(() -> 
            {
                while (Time.millis() < exitTime)
                {
                    Seq<String> messages = Reflect.get(Vars.ui.chatfrag, "messages");

                    boolean isFiltered = false;
                    for  (int i = 0; i < messages.size; i++)
                    {
                        String message = messages.get(i);

                        if (message.contains(e.message))
                        {
                            if (filteredMessage == null)
                            {
                                messages.remove(i);
                            }
                            else
                            {
                                messages.set(i, filteredMessage);
                            }

                            Reflect.set(Vars.ui.chatfrag, "messages", messages);
                            return;
                        }
                    }

                    if (isFiltered)
                    {
                        break;
                    }

                    try
                    {
                        Thread.sleep(16);
                    }
                    catch (InterruptedException ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }).start();
        });
    }
}