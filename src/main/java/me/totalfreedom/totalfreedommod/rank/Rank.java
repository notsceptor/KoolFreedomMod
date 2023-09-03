package me.totalfreedom.totalfreedommod.rank;

import net.md_5.bungee.api.ChatColor;

public enum Rank implements Displayable
{
    IMPOSTOR("an", "Impostor", Type.PLAYER, "Imp", "Impostors", ChatColor.YELLOW, null, false, false),
    NON_OP("a", "Non-Op", Type.PLAYER, "", "Non-Ops", ChatColor.WHITE, null, false, false),
    OP("an", "Operator", Type.PLAYER, "OP", "Operators", ChatColor.GREEN, null, false, false),
    SUPER("a", "Super Admin", Type.ADMIN, "SA", "Super Admins", ChatColor.AQUA, org.bukkit.ChatColor.AQUA, true, true),
    SENIOR("a", "Senior Admin", Type.ADMIN, "SrA", "Senior Administrators", ChatColor.GOLD, org.bukkit.ChatColor.GOLD, true, true),
    CONSOLE("the", "CONSOLE", Type.ADMIN_CONSOLE, "CONSOLE", "Console", ChatColor.BLUE, null, false, false);

    private final Type type;

    private final String name;

    private final String abbr;
    private final String plural;
    private final String article;

    private final String tag;

    private final String coloredTag;

    private final ChatColor color;

    private final org.bukkit.ChatColor teamColor;

    private final boolean hasTeam;

    private final boolean hasDefaultLoginMessage;

    Rank(String article, String name, Type type, String abbr, String plural, ChatColor color, org.bukkit.ChatColor teamColor, Boolean hasTeam, Boolean hasDefaultLoginMessage)
    {
        this.type = type;
        this.name = name;
        this.abbr = abbr;
        this.plural = plural;
        this.article = article;
        this.tag = abbr.isEmpty() ? "" : "[" + abbr + "]";
        this.coloredTag = abbr.isEmpty() ? "" : ChatColor.DARK_GRAY + "[" + color + abbr + ChatColor.DARK_GRAY + "]" + color;
        this.color = color;
        this.teamColor = teamColor;
        this.hasTeam = hasTeam;
        this.hasDefaultLoginMessage = hasDefaultLoginMessage;
    }

    public static Rank findRank(String string)
    {
        try
        {
            return Rank.valueOf(string.toUpperCase());
        }
        catch (Exception ignored)
        {
        }

        return Rank.NON_OP;
    }

    @Override
    public String getColoredName()
    {
        return color + name;
    }

    @Override
    public String getColoredLoginMessage()
    {
        return article + " " + color + name;
    }

    @Override
    public boolean hasTeam()
    {
        return hasTeam;
    }

    @Override
    public boolean hasDefaultLoginMessage()
    {
        return hasDefaultLoginMessage;
    }

    @Override
    public String getAbbr()
    {
        return abbr;
    }

    public String getPlural()
    {
        return plural;
    }

    public boolean isConsole()
    {
        return getType() == Type.ADMIN_CONSOLE;
    }

    public int getLevel()
    {
        return ordinal();
    }

    public boolean isAtLeast(Rank rank)
    {
        if (getLevel() < rank.getLevel())
        {
            return false;
        }

        if (!hasConsoleVariant() || !rank.hasConsoleVariant())
        {
            return true;
        }

        assert getConsoleVariant() != null;
        assert rank.getConsoleVariant() != null;
        return getConsoleVariant().getLevel() >= rank.getConsoleVariant().getLevel();
    }

    public boolean isAdmin()
    {
        return getType() == Type.ADMIN || getType() == Type.ADMIN_CONSOLE;
    }

    public boolean hasConsoleVariant()
    {
        return getConsoleVariant() != null;
    }

    public Rank getConsoleVariant()
    {
        switch (this)
        {
            case SUPER:
            case CONSOLE:
                return CONSOLE;
            case SENIOR:
            default:
                return null;
        }
    }

    public Type getType()
    {
        return type;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getArticle()
    {
        return article;
    }

    @Override
    public String getTag()
    {
        return tag;
    }

    @Override
    public String getColoredTag()
    {
        return coloredTag;
    }

    @Override
    public ChatColor getColor()
    {
        return color;
    }

    @Override
    public org.bukkit.ChatColor getTeamColor()
    {
        return teamColor;
    }

    public boolean isHasTeam()
    {
        return hasTeam;
    }

    public boolean isHasDefaultLoginMessage()
    {
        return hasDefaultLoginMessage;
    }

    public enum Type
    {
        PLAYER,
        ADMIN,
        ADMIN_CONSOLE;

        public boolean isAdmin()
        {
            return this != PLAYER;
        }
    }
}