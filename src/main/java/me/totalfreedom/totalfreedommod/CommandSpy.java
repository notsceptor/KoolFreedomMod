package me.totalfreedom.totalfreedommod;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandSpy extends FreedomService
{
    @Override
    public void onStart()
    {
    }

    @Override
    public void onStop()
    {
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        server.getOnlinePlayers().stream().filter(player -> plugin.al.isAdmin(player)
                && plugin.al.getAdmin(player).getCommandSpy() && player != event.getPlayer()).forEach(player ->
                player.sendMessage(Component.text(event.getPlayer().getName(), NamedTextColor.GRAY).append(Component.text(": ", NamedTextColor.GRAY))
                        .append(Component.text(event.getMessage(), NamedTextColor.GRAY))));
    }
}