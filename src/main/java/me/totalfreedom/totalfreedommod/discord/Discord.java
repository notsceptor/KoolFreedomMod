package me.totalfreedom.totalfreedommod.discord;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import me.totalfreedom.totalfreedommod.FreedomService;
import me.totalfreedom.totalfreedommod.admin.Admin;
import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.discord.command.DiscordCommandManager;
import me.totalfreedom.totalfreedommod.player.PlayerData;
import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.util.FLog;
import me.totalfreedom.totalfreedommod.util.FUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.internal.utils.concurrent.CountingThreadFactory;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.GameRule;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Discord extends FreedomService
{

    public static HashMap<String, PlayerData> LINK_CODES = new HashMap<>();
    public static HashMap<String, PlayerData> VERIFICATION_CODES = new HashMap<>();
    public static JDA bot = null;
    public static DiscordCommandManager DISCORD_COMMAND_MANAGER;
    public ScheduledThreadPoolExecutor RATELIMIT_EXECUTOR;
    public List<CompletableFuture<Message>> sentMessages = new ArrayList<>();
    public Boolean enabled = false;
    private static final ImmutableList<String> DISCORD_SUBDOMAINS = ImmutableList.of("discordapp.com", "discord.com", "discord.gg");

    public static String getMD5(String string)
    {
        return DigestUtils.md5Hex(string);
    }

    public static String getCode(PlayerData playerData)
    {
        for (String code : LINK_CODES.keySet())
        {
            if (LINK_CODES.get(code).equals(playerData))
            {
                return code;
            }
        }
        return null;
    }

    public static boolean syncRoles(Admin admin, String discordID)
    {
        if (discordID == null)
        {
            return false;
        }

        Guild server = bot.getGuildById(ConfigEntry.DISCORD_SERVER_ID.getString());
        if (server == null)
        {
            FLog.severe("The Discord server ID specified is invalid, or the bot is not on the server.");
            return false;
        }

        Member member = server.getMemberById(discordID);
        if (member == null)
        {
            return false;
        }

        Role adminRole = server.getRoleById(ConfigEntry.DISCORD_NEW_ADMIN_ROLE_ID.getString());
        if (adminRole == null)
        {
            FLog.severe("The specified Admin role does not exist!");
            return false;
        }

        Role senioradminRole = server.getRoleById(ConfigEntry.DISCORD_SENIOR_ADMIN_ROLE_ID.getString());
        if (senioradminRole == null)
        {
            FLog.severe("The specified Senior Admin role does not exist!");
            return false;
        }

        if (!admin.isActive())
        {
            if (member.getRoles().contains(adminRole))
            {
                server.removeRoleFromMember(member, adminRole).complete();
            }
            if (member.getRoles().contains(senioradminRole))
            {
                server.removeRoleFromMember(member, senioradminRole).complete();
            }
            return true;
        }

        if (admin.getRank().equals(Rank.SUPER))
        {
            if (!member.getRoles().contains(adminRole))
            {
                server.addRoleToMember(member, adminRole).complete();
            }
            if (member.getRoles().contains(senioradminRole))
            {
                server.removeRoleFromMember(member, senioradminRole).complete();
            }
            return true;
        }
        else if (admin.getRank().equals(Rank.SENIOR))
        {
            if (!member.getRoles().contains(senioradminRole))
            {
                server.addRoleToMember(member, senioradminRole).complete();
            }
            if (member.getRoles().contains(adminRole))
            {
                server.removeRoleFromMember(member, adminRole).complete();
            }
            return true;
        }
        return false;
    }

    public void startBot()
    {
        boolean verificationEnabled = ConfigEntry.DISCORD_VERIFICATION.getBoolean();
        if (!verificationEnabled)
        {
            FLog.info("Discord Verification has been manually disabled.");
        }
        DISCORD_COMMAND_MANAGER = new DiscordCommandManager();
        DISCORD_COMMAND_MANAGER.init(this);
        enabled = !Strings.isNullOrEmpty(ConfigEntry.DISCORD_TOKEN.getString());
        if (!enabled)
        {
            return;
        }

        if (bot != null)
        {
            RATELIMIT_EXECUTOR = new ScheduledThreadPoolExecutor(5, new CountingThreadFactory(this::poolIdentifier, "RateLimit"));
            RATELIMIT_EXECUTOR.setRemoveOnCancelPolicy(true);
            for (Object object : bot.getRegisteredListeners())
            {
                bot.removeEventListener(object);
            }
        }

        try
        {
            bot = JDABuilder.createDefault(ConfigEntry.DISCORD_TOKEN.getString())
                    .addEventListeners(new PrivateMessageListener(),
                            new DiscordToMinecraftListener(),
                            new DiscordToAdminChatListener(),
                            new DiscordToSeniorChatListener(),
                            new MessageReactionListener(),
                            new ListenerAdapter()
                            {
                                @Override
                                public void onReady(@NotNull ReadyEvent event)
                                {
                                    new StartEvent().start();
                                }
                            })
                    .setAutoReconnect(true)
                    .setRateLimitPool(RATELIMIT_EXECUTOR)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .build();
            FLog.info("Discord verification bot has successfully enabled!");
        }
        catch (LoginException e)
        {
            FLog.warning("An invalid token for the discord verification bot, the bot will not enable.");
        }
        catch (IllegalArgumentException e)
        {
            FLog.warning("Discord verification bot failed to start.");
        }
        catch (NoClassDefFoundError e)
        {
            FLog.warning("The JDA plugin is not installed, therefore the discord bot cannot start.");
            FLog.warning("To resolve this error, please download the latest JDA from: https://github.com/TFPatches/Minecraft-JDA/releases");
        }

    }

    public String poolIdentifier()
    {
        return "JDA";
    }

    public void clearQueue()
    {
        for (CompletableFuture<Message> messages : sentMessages)
        {
            if (!messages.isDone())
            {
                messages.cancel(true);
            }
        }
        sentMessages.clear();
        messageChatChannel("**Message queue cleared**", true);
    }


    public User getUser(String id)
    {
        Guild guild = bot.getGuildById(ConfigEntry.DISCORD_SERVER_ID.getString());
        if (guild == null)
        {
            FLog.severe("Either the bot is not in the Discord server or it doesn't exist. Check the server ID.");
            return null;
        }

        Member member = guild.getMemberById(id);
        if (member == null)
        {
            return null;
        }

        return member.getUser();
    }

    public boolean sendBackupCodes(PlayerData playerData)
    {
        List<String> codes = generateBackupCodes();
        List<String> encryptedCodes = generateEncryptedBackupCodes(codes);
        User user = getUser(playerData.getDiscordID());
        File file = generateBackupCodesFile(playerData.getName(), codes);
        if (file == null)
        {
            return false;
        }
        PrivateChannel privateChannel = user.openPrivateChannel().complete();
        privateChannel.sendMessage("Do not share these codes with anyone as they can be used to impose as you.").addFile(file).complete();
        playerData.setBackupCodes(encryptedCodes);
        plugin.pl.save(playerData);
        //noinspection ResultOfMethodCallIgnored
        file.delete();
        return true;
    }

    public List<String> generateBackupCodes()
    {
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < 10; i++)
        {
            codes.add(FUtil.randomAlphanumericString(10));
        }
        return codes;
    }

    public String generateCode(int size)
    {
        return RandomStringUtils.randomNumeric(size);
    }

    public List<String> generateEncryptedBackupCodes(List<String> codes)
    {
        List<String> encryptedCodes = new ArrayList<>();
        for (String code : codes)
        {
            encryptedCodes.add(getMD5(code));
        }
        return encryptedCodes;
    }

    public File generateBackupCodesFile(String name, List<String> codes)
    {
        StringBuilder text = new StringBuilder();
        text.append("Below are your backup codes for use on TotalFreedom in the event you lose access to your discord account.\n")
                .append("Simply pick a code, and run /verify <code> on the server. Each code is one use, so be sure to cross it off once you use it.\n")
                .append("To generate new codes, simply run /generatebackupcodes\n\n");

        for (String code : codes)
        {
            text.append(code).append("\n");
        }

        String fileUrl = plugin.getDataFolder().getAbsolutePath() + "/TF-Backup-Codes-" + name + ".txt";
        try
        {
            FileWriter fileWriter = new FileWriter(fileUrl);
            fileWriter.write(text.toString());
            fileWriter.close();
        }
        catch (IOException e)
        {
            FLog.severe("Failed to generate backup codes file: " + e.toString());
            return null;
        }
        return new File(fileUrl);
    }

    public void addVerificationCode(String code, PlayerData playerData)
    {
        VERIFICATION_CODES.put(code, playerData);
    }

    public void removeVerificationCode(String code)
    {
        VERIFICATION_CODES.remove(code);
    }

    public HashMap<String, PlayerData> getVerificationCodes()
    {
        return VERIFICATION_CODES;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        //Avoiding NPE Unboxing Warnings
        Boolean b = event.getEntity().getWorld().getGameRuleValue(GameRule.SHOW_DEATH_MESSAGES);
        if (b == null || !b)
        {
            return;
        }

        if (event.getDeathMessage() != null)
        {
            messageChatChannel("**" + event.getDeathMessage() + "**");
        }
    }

    @Override
    public void onStart()
    {
        startBot();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        if (!plugin.al.isVanished(event.getPlayer().getName()))
        {
            messageChatChannel("**" + event.getPlayer().getName() + " joined the server" + "**", true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLeave(PlayerQuitEvent event)
    {
        if (!plugin.al.isVanished(event.getPlayer().getName()))
        {
            messageChatChannel("**" + event.getPlayer().getName() + " left the server" + "**", true);
        }
    }

    public static String sanitizeChatMessage(String message)
    {
        String newMessage = message;

        if (message.contains("@"))
        {
            // \u200B is Zero Width Space, invisible on Discord
            newMessage = message.replaceAll("@", "@\u200B");
        }

        if (message.toLowerCase().contains("discord.gg")) // discord.gg/invite works as an invite
        {
            return "";
        }

        for (String subdomain : DISCORD_SUBDOMAINS)
        {
            if (message.toLowerCase().contains(subdomain + "/invite"))
            {
                return "";
            }
        }

        if (message.contains("§"))
        {
            newMessage = message.replaceAll("§", "");
        }

        return deformat(newMessage);
    }

    public void messageChatChannel(String message)
    {
        messageChatChannel(message, false);
    }

    public void messageChatChannel(String message, boolean system)
    {
        String chat_channel_id = ConfigEntry.DISCORD_CHAT_CHANNEL_ID.getString();

        String sanitizedMessage = (system) ? message : sanitizeChatMessage(message);

        if (sanitizedMessage.isBlank()) return;

        if (enabled && !chat_channel_id.isEmpty())
        {
            CompletableFuture<Message> sentMessage = Objects.requireNonNull(bot.getTextChannelById(chat_channel_id)).sendMessage(sanitizedMessage).submit(true);
            sentMessages.add(sentMessage);
        }
    }

    public void messageAdminChatChannel(String message)
    {
        messageAdminChatChannel(message, false);
    }

    public void messageAdminChatChannel(String message, boolean system)
    {
        String chat_channel_id = ConfigEntry.DISCORD_ADMINCHAT_CHANNEL_ID.getString();

        String sanitizedMessage = sanitizeChatMessage(message);

        if (sanitizedMessage.isBlank()) return;

        if (enabled && !chat_channel_id.isEmpty())
        {
            CompletableFuture<Message> sentMessage = Objects.requireNonNull(bot.getTextChannelById(chat_channel_id)).sendMessage(sanitizedMessage).submit(true);
            sentMessages.add(sentMessage);
        }
    }

    public void messageSeniorChatChannel(String message)
    {
        messageSeniorChatChannel(message, false);
    }

    public void messageSeniorChatChannel(String message, boolean system)
    {
        String chat_channel_id = ConfigEntry.DISCORD_SENIORCHAT_CHANNEL_ID.getString();

        String sanitizedMessage = sanitizeChatMessage(message);

        if (sanitizedMessage.isBlank()) return;

        if (enabled && !chat_channel_id.isEmpty())
        {
            CompletableFuture<Message> sentMessage = Objects.requireNonNull(bot.getTextChannelById(chat_channel_id)).sendMessage(sanitizedMessage).submit(true);
            sentMessages.add(sentMessage);
        }
    }

    public String formatBotTag()
    {
        SelfUser user = bot.getSelfUser();
        return user.getAsTag();
    }

    @Override
    public void onStop()
    {
        if (bot != null)
        {
            messageChatChannel("**Server has stopped**", true);
        }

        FLog.info("Discord verification bot has successfully shutdown.");
    }

    public static String deformat(String input)
    {
        return input.replaceAll("([_\\\\`*>|])", "\\\\$1");
    }


    public boolean shouldISendReport() {
        if (ConfigEntry.DISCORD_REPORT_CHANNEL_ID.getString().isEmpty()) {
            return false;
        }

        if (ConfigEntry.DISCORD_SERVER_ID.getString().isEmpty()) {
            FLog.severe("No Discord server ID was specified in the config, but there is a report channel ID.");
            return false;
        }

        Guild server = bot.getGuildById(ConfigEntry.DISCORD_SERVER_ID.getString());
        if (server == null) {
            FLog.severe("The Discord server ID specified is invalid, or the bot is not on the server.");
            return false;
        }

        TextChannel channel = server.getTextChannelById(ConfigEntry.DISCORD_REPORT_CHANNEL_ID.getString());
        if (channel == null) {
            FLog.severe("The report channel ID specified in the config is invalid.");
            return false;
        }
        return true;
    }
    public boolean sendReportOffline(Player reporter, OfflinePlayer reported, String reason){
        if (!shouldISendReport())
        {
            return false;
        }

        final Guild server = bot.getGuildById(ConfigEntry.DISCORD_SERVER_ID.getString());
        final TextChannel channel = server.getTextChannelById(ConfigEntry.DISCORD_REPORT_CHANNEL_ID.getString());

        final EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Report for " + reported.getName() + " (offline)");
        embedBuilder.setDescription(reason);
        embedBuilder.setFooter("Reported by " + reporter.getName(), "https://minotar.net/helm/" + reporter.getName() + ".png");
        embedBuilder.setTimestamp(Instant.from(ZonedDateTime.now()));
        com.earth2me.essentials.User user = plugin.esb.getEssentialsUser(reported.getName());
        String location = "World: " + Objects.requireNonNull(user.getLastLocation().getWorld()).getName() + ", X: " + user.getLastLocation().getBlockX() + ", Y: " + user.getLastLocation().getBlockY() + ", Z: " + user.getLastLocation().getBlockZ();
        embedBuilder.addField("Location", location, true);
        embedBuilder.addField("God Mode", WordUtils.capitalizeFully(String.valueOf(user.isGodModeEnabled())), true);
        if (user.getNickname() != null)
        {
            embedBuilder.addField("Nickname", user.getNickname(), true);
        }
        MessageEmbed embed = embedBuilder.build();
        Message message = channel.sendMessage(embed).complete();

        if (!ConfigEntry.DISCORD_REPORT_ARCHIVE_CHANNEL_ID.getString().isEmpty())
        {
            message.addReaction("\uD83D\uDCCB").complete();
        }

        return true;
    }
    public boolean sendReport(Player reporter, Player reported, String reason){
        if (!shouldISendReport())
        {
            return false;
        }

        final Guild server = bot.getGuildById(ConfigEntry.DISCORD_SERVER_ID.getString());
        final TextChannel channel = server.getTextChannelById(ConfigEntry.DISCORD_REPORT_CHANNEL_ID.getString());

        final EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Report for " + reported.getName());
        embedBuilder.setDescription(reason);
        embedBuilder.setFooter("Reported by " + reporter.getName(), "https://minotar.net/helm/" + reporter.getName() + ".png");
        embedBuilder.setTimestamp(Instant.from(ZonedDateTime.now()));
        String location = "World: " + Objects.requireNonNull(reported.getLocation().getWorld()).getName() + ", X: " + reported.getLocation().getBlockX() + ", Y: " + reported.getLocation().getBlockY() + ", Z: " + reported.getLocation().getBlockZ();
        embedBuilder.addField("Location", location, true);
        embedBuilder.addField("Game Mode", WordUtils.capitalizeFully(reported.getGameMode().name()), true);
        com.earth2me.essentials.User user = plugin.esb.getEssentialsUser(reported.getName());
        embedBuilder.addField("God Mode", WordUtils.capitalizeFully(String.valueOf(user.isGodModeEnabled())), true);
        if (user.getNickname() != null)
        {
            embedBuilder.addField("Nickname", user.getNickname(), true);
        }
        MessageEmbed embed = embedBuilder.build();
        Message message = channel.sendMessage(embed).complete();

        if (!ConfigEntry.DISCORD_REPORT_ARCHIVE_CHANNEL_ID.getString().isEmpty())
        {
            message.addReaction("\uD83D\uDCCB").complete();
        }
        return true;
    }

    // Do no ask why this is here. I spent two hours trying to make a simple thing work
    public class StartEvent
    {
        public void start()
        {
            messageChatChannel("**Server has started**", true);
        }
    }
}