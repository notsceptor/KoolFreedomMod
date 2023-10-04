package me.totalfreedom.totalfreedommod.command;

import me.totalfreedom.totalfreedommod.GameRuleHandler;
import me.totalfreedom.totalfreedommod.player.PlayerData;
import me.totalfreedom.totalfreedommod.util.FUtil;
import me.totalfreedom.totalfreedommod.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@CommandPermissions(level = Rank.SUPER, source = SourceType.BOTH)
@CommandParameters(
        description = "Make someone's balls explode.",
        usage = "/<command> [player]")
public class Command_balls extends FreedomCommand
{
    @Override
    public boolean run(CommandSender sender, Player sender_p, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (args.length != 1)
        {
            return false;
        }

        final Player player = getPlayer(args[0]);

        if (player == null)
        {
            sender.sendMessage(FreedomCommand.PLAYER_NOT_FOUND);
            return true;
        }
        FUtil.bcastMsg(ChatColor.RED + player.getName() + "'s balls exploded.");
        player.sendMessage("This is for no reason at all. We just like to blow peoples balls up.");
        player.getWorld().createExplosion(player.getLocation(), 4F);
        player.getWorld().strikeLightning(player.getLocation());
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                player.getWorld().createExplosion(player.getLocation(), 4F);
                player.getWorld().strikeLightning(player.getLocation());
                player.setHealth(0.0);
            }
        }.runTaskLater(plugin, 2L * 2L);
        return true;
    }
}