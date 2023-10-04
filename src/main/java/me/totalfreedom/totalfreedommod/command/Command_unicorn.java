//Made by Hawkeyeshi
package me.totalfreedom.totalfreedommod.command;

import me.totalfreedom.totalfreedommod.util.FUtil;
import me.totalfreedom.totalfreedommod.rank.Rank;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = Rank.SUPER)
@CommandParameters(description = "Displays a text unicorn is everyone's chat!", usage = "/<command>")
public class Command_unicorn extends FreedomCommand
{
    @Override
    public boolean run(CommandSender sender, Player sender_p, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {

        for (Player player : server.getOnlinePlayers())
        {
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_SHOOT, 1.0F, 1.0F);

        }

        FUtil.bcastMsg(FUtil.randomChatColor() + "                                                         ,/");
        FUtil.bcastMsg(FUtil.randomChatColor() + "                                                        //");
        FUtil.bcastMsg(FUtil.randomChatColor() + "                                                      ,//");
        FUtil.bcastMsg(FUtil.randomChatColor() + "                                          ___   /|   |//");
        FUtil.bcastMsg(FUtil.randomChatColor() + "                                      `__/\\_ --(/|___/-/");
        FUtil.bcastMsg(FUtil.randomChatColor() + "                                   \\|\\_-\\___ __-_`- /-/ \\.");
        FUtil.bcastMsg(FUtil.randomChatColor() + "                                  |\\_-___,-\\_____--/_)' ) \\");
        FUtil.bcastMsg(FUtil.randomChatColor() + "                                   \\ -_ /     __ \\( `( __`\\|");
        FUtil.bcastMsg(FUtil.randomChatColor() + "                                   `\\__|      |\\)\\ ) /(/|");
        FUtil.bcastMsg(FUtil.randomChatColor() + "           ,._____.,            ',--//-|      \\  |  '   /");
        FUtil.bcastMsg(FUtil.randomChatColor() + "          /     __. \\,          / /,---|       \\       /");
        FUtil.bcastMsg(FUtil.randomChatColor() + "        |  | ( (  \\   |      ,/\\'__/'/          |     |");
        FUtil.bcastMsg(FUtil.randomChatColor() + "        |  \\  \\`--, `_/_------______/           \\(   )/");
        FUtil.bcastMsg(FUtil.randomChatColor() + "        | | \\  \\_. \\,                            \\___/\\");
        FUtil.bcastMsg(FUtil.randomChatColor() + "        | |  \\_   \\  \\                                 \\");
        FUtil.bcastMsg(FUtil.randomChatColor() + "        \\ \\    \\_ \\   \\   /                             \\");
        FUtil.bcastMsg(FUtil.randomChatColor() + "         \\ \\  \\._  \\__ \\_|       |                       \\");
        FUtil.bcastMsg(FUtil.randomChatColor() + "          \\ \\___  \\      \\       |                        \\");
        FUtil.bcastMsg(FUtil.randomChatColor() + "           \\__ \\__ \\  \\_ |       \\                         |");
        FUtil.bcastMsg(FUtil.randomChatColor() + "           |  \\_____ \\  ____      |                           |");
        FUtil.bcastMsg(FUtil.randomChatColor() + "           | \\  \\__ ---' .__\\     |        |                 |");
        FUtil.bcastMsg(FUtil.randomChatColor() + "           \\  \\__ ---   /   )     |        \\                /");
        FUtil.bcastMsg(FUtil.randomChatColor() + "            \\   \\____/ / ()(      \\          `---_         /|");
        FUtil.bcastMsg(FUtil.randomChatColor() + "             \\__________/(,--__    \\_________.    |       ./ |");
        FUtil.bcastMsg(FUtil.randomChatColor() + "               |     \\ \\  `---_\\--,           \\   \\_,./   |");
        FUtil.bcastMsg(FUtil.randomChatColor() + "               |      \\  \\_ ` \\    /`---_______-\\   \\\\    /");
        FUtil.bcastMsg(FUtil.randomChatColor() + "                \\      \\.___,`|   /              \\   \\\\   \\");
        FUtil.bcastMsg(FUtil.randomChatColor() + "                 \\     |  \\_ \\|   \\              (   |:    |");
        FUtil.bcastMsg(FUtil.randomChatColor() + "                  \\    \\      \\    |             /  / |    ;");
        FUtil.bcastMsg(FUtil.randomChatColor() + "                   \\    \\      \\    \\          ( `_'   \\  |");
        FUtil.bcastMsg(FUtil.randomChatColor() + "                    \\.   \\      \\.   \\          `__/   |  |");
        FUtil.bcastMsg(FUtil.randomChatColor() + "                      \\   \\       \\.  \\                |  |");
        FUtil.bcastMsg(FUtil.randomChatColor() + "                       \\   \\        \\  \\               (  )");
        FUtil.bcastMsg(FUtil.randomChatColor() + "                        \\   |        \\  |                |  |");
        FUtil.bcastMsg(FUtil.randomChatColor() + "                         |  \\         \\ \\               I  `");
        FUtil.bcastMsg(FUtil.randomChatColor() + "                         ( __;        ( _;                ('-_';");
        FUtil.bcastMsg(FUtil.randomChatColor() + "                         |___\\       \\___:              \\___:");

        FUtil.bcastMsg("   ");
        FUtil.bcastMsg("   ");
        FUtil.bcastMsg(FUtil.randomChatColor() + "You've been " + FUtil.randomChatColor() + "unicorned by " + FUtil.randomChatColor() + sender.getName());

        return false;
    }
}