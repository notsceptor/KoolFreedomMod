package me.totalfreedom.totalfreedommod.command;

import me.totalfreedom.totalfreedommod.player.FPlayer;
import me.totalfreedom.totalfreedommod.rank.Rank;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = Rank.SENIOR, source = SourceType.BOTH)
@CommandParameters(description = "Allows Senior Administrators to talk in a chat with other senior administrators.", usage = "/<command> [message...]", aliases = "sc,p")
public class Command_seniorchat extends FreedomCommand
{

    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (args.length == 0)
        {
            if (senderIsConsole)
            {
                msg("Only in-game players can toggle SeniorChat.");
                return true;
            }

            FPlayer userinfo = plugin.pl.getPlayer(playerSender);
            userinfo.setSeniorChat(!userinfo.inSeniorChat());
            msg("Senior chat turned " + (userinfo.inSeniorChat() ? "on" : "off") + ".");
        }
        else
        {
            plugin.cm.seniorChat(sender, StringUtils.join(args, " "));
        }
        return true;
    }
}