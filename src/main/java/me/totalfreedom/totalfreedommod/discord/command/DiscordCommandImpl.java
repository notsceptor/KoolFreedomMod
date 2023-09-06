package me.totalfreedom.totalfreedommod.discord.command;

import net.dv8tion.jda.api.entities.Member;

public abstract class DiscordCommandImpl implements DiscordCommand
{
    @Override
    public boolean canExecute(Member member)
    {
        // TODO Implement administrator checks if/when any administrative Discord commands are added to the bot
        return !isAdmin();
    }
}