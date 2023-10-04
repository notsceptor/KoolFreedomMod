package me.totalfreedom.totalfreedommod.command;

import org.bukkit.Bukkit;
import me.totalfreedom.totalfreedommod.rank.Rank;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = Rank.SUPER, source = SourceType.BOTH)
@CommandParameters(description = "Lolno.", usage = "/<command>")
public class Command_lolno extends FreedomCommand
{
    @Override
    public boolean run(CommandSender sender, Player sender_p, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        sender_p.chat("Lolno. Just.. just.. go. just GO AWAY! BITCH");
        Bukkit.dispatchCommand(sender, "expel");
        return true;
    }
}