package me.totalfreedom.totalfreedommod.discord.commands;

import me.totalfreedom.totalfreedommod.TotalFreedomMod;
import me.totalfreedom.totalfreedommod.admin.AdminList;
import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.discord.Discord;
import me.totalfreedom.totalfreedommod.discord.command.DiscordCommandImpl;
import me.totalfreedom.totalfreedommod.rank.Displayable;
import me.totalfreedom.totalfreedommod.rank.RankManager;
import me.totalfreedom.totalfreedommod.util.FUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListCommand extends DiscordCommandImpl
{
    private static final TotalFreedomMod PLUGIN = TotalFreedomMod.plugin();

    @Override
    public String getCommandName()
    {
        return "list";
    }

    @Override
    public String getDescription()
    {
        return "Gives a list of online players.";
    }

    @Override
    public String getCategory()
    {
        return "Server Commands";
    }

    @Override
    public List<String> getAliases()
    {
        return List.of("online", "who", "l", "lsit");
    }

    @Override
    public boolean isAdmin()
    {
        return false;
    }

    @Override
    public MessageBuilder execute(Member member, List<String> args)
    {
        if (PLUGIN == null)
        {
            throw new IllegalStateException("TotalFreedomMod somehow null while executing a command!");
        }

        final AdminList adminList = PLUGIN.al;
        final RankManager rankManager = PLUGIN.rm;

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Player List - " + ConfigEntry.SERVER_NAME.getString())
                .setDescription("There are " + FUtil.getFakePlayerCount() + " / " + Bukkit.getMaxPlayers() + " online players");

        Map<Displayable, List<String>> displayables = new HashMap<>();

        Bukkit.getOnlinePlayers().stream().filter(player -> !adminList.isVanished(String.valueOf(player.getUniqueId()))).forEach(player ->
        {
            final Displayable displayable = rankManager.getDisplay(player);
            final String name = Discord.deformat(player.getName());

            if (displayables.containsKey(displayable))
            {
                displayables.get(displayable).add(name);
            }
            else
            {
                displayables.put(displayable, new ArrayList<>(List.of(name)));
            }
        });

        for (Map.Entry<Displayable, List<String>> entry : displayables.entrySet())
        {
            final Displayable displayable = entry.getKey();
            final List<String> players = entry.getValue();

            embedBuilder.addField(displayable.getPlural() + " (" + players.size() + ")",
                    String.join(", ", players), false);
        }

        return new MessageBuilder().setEmbed(embedBuilder.build());
    }
}