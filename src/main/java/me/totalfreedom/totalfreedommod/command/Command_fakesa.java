package me.totalfreedom.totalfreedommod.command;

import me.totalfreedom.totalfreedommod.util.FUtil;
import me.totalfreedom.totalfreedommod.rank.Rank;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = Rank.SUPER, source = SourceType.BOTH)
@CommandParameters(description = "Manage superadmins in a fake way", usage = "/<command> [add:del] <player>")
public class Command_fakesa extends FreedomCommand
{

    @Override
    public boolean run(CommandSender sender, Player sender_p, Command cmd, String commandLabel, String[] args, boolean senderIsConsole) {
        if (args.length < 0)
        {
            return false;
        }

        if (args.length == 1)
        {
            final Player player = getPlayer(args[0]);

            if (player == null)
            {
                msg(FreedomCommand.PLAYER_NOT_FOUND, ChatColor.RED);

                if (args[0].equals("add"))
                {
                    FUtil.bcastMsg(sender.getName() + " - Adding " + player.getName() + " to the superadmin list.", ChatColor.RED);
                    return true;
                }
                else if (args[0].equals("delete"))
                {
                    FUtil.bcastMsg(sender.getName() + " - Removing " + player.getName() + " from the superadmin list.", ChatColor.RED);
                    return true;
                }
                return true;

            }
            {
                return true;
            }
        }
        return true;
    }
}