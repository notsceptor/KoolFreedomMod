package me.totalfreedom.totalfreedommod.command;

import me.totalfreedom.totalfreedommod.admin.Admin;
import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.discord.Discord;
import me.totalfreedom.totalfreedommod.player.FPlayer;
import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

@CommandPermissions(level = Rank.OP, source = SourceType.BOTH)
@CommandParameters(description = "List, add, remove, or set the rank of admins, clean or reload the admin list, or view admin information.", usage = "/<command> <list | clean | reload | | setrank <username> <rank> | <add | remove | info> <username>>", aliases = "slconfig")
public class Command_saconfig extends FreedomCommand
{

    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (args.length < 1)
        {
            return false;
        }

        switch (args[0])
        {
            case "list":
            {
                msg("Superadmins: " + StringUtils.join(plugin.al.getAdminNames(), ", "), ChatColor.GOLD);
                return true;
            }

            case "clean":
            {
                checkRank(Rank.SENIOR);

                FUtil.adminAction(sender.getName(), "Cleaning the superadmin list", true);
                plugin.al.deactivateOldEntries(true);
                msg("Admins: " + StringUtils.join(plugin.al.getAdminNames(), ", "), ChatColor.GOLD);

                return true;
            }

            case "setrank":
            {
                checkRank(Rank.SENIOR);

                if (args.length < 3)
                {
                    return false;
                }

                Rank rank = Rank.findRank(args[2]);
                if (rank == null)
                {
                    msg("Unknown rank: " + args[2]);
                    return true;
                }

                if(rank.isAtLeast(Rank.SENIOR)){
                    checkConsole();
                }

                if (!rank.isAtLeast(Rank.SUPER))
                {
                    msg("Rank must be Super or higher.", ChatColor.RED);
                    return true;
                }

                Admin admin = plugin.al.getEntryByName(args[1]);
                if (admin == null)
                {
                    msg("Unknown admin: " + args[1]);
                    return true;
                }

                FUtil.adminAction(sender.getName(), "Setting " + admin.getName() + "'s rank to " + rank.getName(), true);

                admin.setRank(rank);
                plugin.al.save(admin);

                Player player = getPlayer(admin.getName());
                if (player != null)
                {
                    plugin.rm.updateDisplay(player);
                }

                if (plugin.dc.enabled && ConfigEntry.DISCORD_ROLE_SYNC.getBoolean())
                {
                    Discord.syncRoles(admin, plugin.pl.getData(admin.getName()).getDiscordID());
                }

                plugin.ptero.updateAccountStatus(admin);

                msg("Set " + admin.getName() + "'s rank to " + rank.getName());
                return true;
            }

            case "info":
            {
                checkRank(Rank.SENIOR);

                if (args.length < 2)
                {
                    return false;
                }

                Admin admin = plugin.al.getEntryByName(args[1]);

                if (admin == null)
                {
                    final Player player = getPlayer(args[1]);
                    if (player != null)
                    {
                        admin = plugin.al.getAdmin(player);
                    }
                }

                if (admin == null)
                {
                    msg("Superadmin not found: " + args[1]);
                }
                else
                {
                    msg(admin.toString());
                }

                return true;
            }

            case "add":
            {
                checkRank(Rank.SENIOR);

                if (args.length < 2)
                {
                    return false;
                }

                // Player already admin?
                final Player player = getPlayer(args[1]);

                // Find the old admin entry
                Admin admin = null;
                for (Admin loopAdmin : plugin.al.getAllAdmins())
                {
                    if (loopAdmin.getName().equals(player.getName()))
                    {
                        admin = loopAdmin;
                        break;
                    }
                }

                if (player == null)
                {
                    msg(FreedomCommand.PLAYER_NOT_FOUND);
                    return true;
                }

                if (plugin.al.isAdmin(player))
                {
                    msg("That player is already an admin.");
                    return true;
                }


                if (admin == null) // New admin
                {
                    FUtil.adminAction(sender.getName(), "Adding " + player.getName() + " to the superadmin list", true);
                    admin = new Admin(player);

                    plugin.al.addAdmin(admin);
                    plugin.rm.updateDisplay(player);
                }
                else // Existing admin
                {
                    FUtil.adminAction(sender.getName(), "Re-adding " + player.getName() + " to the superadmin list", true);
                    admin.addIp(FUtil.getIp(player));
                    admin.setActive(true);
                    admin.setLastLogin(new Date());

                    plugin.al.save(admin);
                    plugin.al.updateTables();
                    plugin.rm.updateDisplay(player);

                    if (plugin.dc.enabled && ConfigEntry.DISCORD_ROLE_SYNC.getBoolean())
                    {
                        Discord.syncRoles(admin, plugin.pl.getData(player).getDiscordID());
                    }
                }
                plugin.ptero.updateAccountStatus(admin);

                final FPlayer fPlayer = plugin.pl.getPlayer(player);
                if (fPlayer.getFreezeData().isFrozen())
                {
                    fPlayer.getFreezeData().setFrozen(false);
                    msg(player, "You have been unfrozen.");
                }

                if (!player.isOp())
                {
                    player.setOp(true);
                    msg(player, YOU_ARE_OP);
                }
                return true;
            }

            case "remove":
            {
                if (args.length < 2)
                {
                    return false;
                }

                checkRank(Rank.SENIOR);

                Player player = getPlayer(args[1]);
                Admin admin = player != null ? plugin.al.getAdmin(player) : plugin.al.getEntryByName(args[1]);
                String adminName = admin.getName();

                if (admin == null)
                {
                    msg("Admin not found: " + args[1]);
                    return true;
                }

                if(admin.getRank().isAtLeast(Rank.SENIOR)){
                }

                FUtil.adminAction(sender.getName(), "Removing " + admin.getName() + " from the superadmin list", true);
                admin.setActive(false);

                plugin.al.save(admin);
                plugin.al.updateTables();

                if (player != null)
                {
                    plugin.rm.updateDisplay(player);
                }

                if (plugin.dc.enabled && ConfigEntry.DISCORD_ROLE_SYNC.getBoolean())
                {
                    Discord.syncRoles(admin, plugin.pl.getData(adminName).getDiscordID());
                }

                plugin.ptero.updateAccountStatus(admin);

                return true;
            }

            default:
            {
                return false;
            }
        }
    }

    @Override
    public List<String> getTabCompleteOptions(CommandSender sender, Command command, String alias, String[] args)
    {
        if (args.length == 1)
        {
            List<String> arguments = new ArrayList<>();
            arguments.add("list");
            if (plugin.al.isSeniorAdmin(sender))
            {
                arguments.add("info");
                arguments.add("add");
                arguments.add("remove");
                arguments.add("reload");
                arguments.add("clean");
                arguments.add("setrank");
            }
            return arguments;
        }
        if (args.length == 2 && (args[0].equals("add") || args[0].equals("remove") || args[0].equals("setrank") || args[0].equals("info")))
        {
            return FUtil.getPlayerList();
        }
        if (args.length == 3 && args[0].equals("setrank"))
        {
            return Arrays.asList("SUPER", "SENIOR");
        }

        return Collections.emptyList();
    }
}