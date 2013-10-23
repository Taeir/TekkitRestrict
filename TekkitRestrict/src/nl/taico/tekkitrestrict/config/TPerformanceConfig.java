package nl.taico.tekkitrestrict.config;

import java.util.ArrayList;

public class TPerformanceConfig extends TRConfig {
	public static ArrayList<String> defaultContents(boolean extra){
		ArrayList<String> tbr = new ArrayList<String>();
		tbr.add("##################################################################################");
		tbr.add("## Configuration file for TekkitRestrict                                        ##");
		tbr.add("## Authors: Taeir, DreadEnd (aka DreadSlicer)                                   ##");
		tbr.add("## BukkitDev: http://dev.bukkit.org/server-mods/tekkit-restrict/                ##");
		tbr.add("## Please ask questions/report issues on the BukkitDev page.                    ##");
		tbr.add("##################################################################################");
		tbr.add("");
		tbr.add("##################################################################################");
		tbr.add("################################# ChunkUnloader ##################################");
		tbr.add("##################################################################################");
		tbr.add("# WARNING: Use with caution. If you turn this on, it MAY lag your server.");
		tbr.add("# This will be very useful for preventing your server from getting over 30,000");
		tbr.add("# chunks.");
		tbr.add("#");
		tbr.add("# There is a bug with the Normal Tekkit server in which the server gains \"Trash\"");
		tbr.add("# in the memory from disposing of chunks. I recommend keeping your MaxChunks");
		tbr.add("# around 3000-4000 to prevent this from disabling your server over time.");
		tbr.add("# (MCPCSpout does not have this problem)");
		tbr.add("#");
		tbr.add("# If you lower it below 1000, you have a chance of lagging your server by player");
		tbr.add("# movements.");
		tbr.add("UseChunkUnloader: true");
		if (extra) tbr.add("#:-;-:# UseChunkUnloader");
		tbr.add("");
		tbr.add("# The maximal amount of chunks loaded at a time. If this amount is exceeded,");
		tbr.add("# chunks will be forcefully unloaded (even if they have chunk loaders) to clear");
		tbr.add("# RAM.");
		tbr.add("# The more players you have on the server, the higher you should set this to.");
		tbr.add("MaxChunks:");
		tbr.add("    # Max number of chunks for the End.");
		tbr.add("    # Recommended: 100-400 (the End is usually not a often visited place)");
		tbr.add("    # Default: 200");
		tbr.add("    TheEnd: 200");
		if (extra) tbr.add("#:-;-:# MaxChunks.TheEnd");
		tbr.add("    ");
		tbr.add("    # Max number of chunks for the Nether.");
		tbr.add("    # Recommended: 300-600 (depends on how many players live in the Nether)");
		tbr.add("    # Default: 400");
		tbr.add("    Nether: 400");
		if (extra) tbr.add("#:-;-:# MaxChunks.Nether");
		tbr.add("    ");
		tbr.add("    # Max number of chunks for normal type worlds.");
		tbr.add("    # Recommended: (Server RAM in GB)*700 to (Server RAM in GB)*1100");
		tbr.add("    # Default: 4000");
		tbr.add("    Normal: 4000");
		if (extra) tbr.add("#:-;-:# MaxChunks.Normal");
		tbr.add("    ");
		tbr.add("    # Max number of chunks loaded in total (all worlds)");
		tbr.add("    # If this number is exceeded, the UnloadOrder will come in effect.");
		tbr.add("    # Recommended: (Server RAM in GB)*700 to (Server RAM in GB)*1100");
		tbr.add("    # Default: 4000");
		tbr.add("    Total: 4000");
		if (extra) tbr.add("#:-;-:# MaxChunks.Total");
		tbr.add("");
		tbr.add("# UnloadOrder");
		tbr.add("# The order in which chunks from worlds will be unloaded when the total number");
		tbr.add("# of chunks is exceeded.");
		tbr.add("# 0 - The End, Nether, Normal worlds    (default)");
		tbr.add("# 1 - Nether, The End, Normal worlds");
		tbr.add("#");
		tbr.add("# 2 - Normal worlds, The End, Nether    (not recommended)");
		tbr.add("# 3 - The End, Normal worlds, Nether    (not recommended)");
		tbr.add("#");
		tbr.add("# 4 - Nether, Normal worlds, The End    (not recommended)");
		tbr.add("# 5 - Normal worlds, Nether, The End    (not recommended)");
		tbr.add("#");
		tbr.add("# 2 and 3 are not recommended unless your main world is a Nether world.");
		tbr.add("# 4 and 5 are not recommended unless your main world is an End world.");
		tbr.add("#");
		tbr.add("# It is recommended to put the least used world first.");
		tbr.add("UnloadOrder: 0");
		if (extra) tbr.add("#:-;-:# UnloadOrder");
		tbr.add("");
		tbr.add("# The maximal amount of chunks loaded per player.");
		tbr.add("# If MaxChunksPerPlayer is 100 and you have 10 players on your server, the max");
		tbr.add("# number of chunks will be 1000.");
		tbr.add("#");
		tbr.add("# NOTE: This feature has not been implemented (yet).");
		tbr.add("# MaxChunksPerPlayer: 100");
		tbr.add("");
		tbr.add("# The radius of blocks that should stay loaded around a player.");
		tbr.add("# The ChunkUnloader will not unload chunks that are within this radius to a");
		tbr.add("# player.");
		tbr.add("# Minecraft's default is 256 blocks (16 chunks).");
		tbr.add("# ");
		tbr.add("# Recommended: 128-256");
		tbr.add("# Default: 256");
		tbr.add("MaxRadii: 256");
		//if (extra) tbr.add("#:-;-:# MaxRadii"); IMPORTANT: This is commented to switch to new default settings
		tbr.add("");
		tbr.add("##################################################################################");
		tbr.add("############################ Threading Configuration #############################");
		tbr.add("##################################################################################");
		tbr.add("");
		tbr.add("# Do not edit these values unless you know what you are doing.");
		tbr.add("# These numbers are in Milliseconds. (1000 milliseconds = 1 second)");
		tbr.add("# NOTE: the faster you set this to, your server is more likely to lag out.");
		tbr.add("");
		tbr.add("# The amount of time the thread that disables GemArmor powers (see");
		tbr.add("# ModModifications config) sleeps for.");
		tbr.add("# If you set this too high, players might be able to use their powers by spamming.");
		tbr.add("# If you set this too low, it might lag the server.");
		tbr.add("#");
		tbr.add("# Recommended: [100-200]");
		tbr.add("# Default: 120");
		tbr.add("GemArmorDThread: 120");
		if (extra) tbr.add("#:-;-:# GemArmorDThread");
		tbr.add("");
		tbr.add("# The amount of time the thread that removes entities in SafeZones powers (see");
		tbr.add("# SafeZones config) sleeps for.");
		tbr.add("# If removing entities from a SafeZones is not something that is very important");
		tbr.add("# for your server, you can raise this to 1000-2000 (1-2 seconds)");
		tbr.add("#");
		tbr.add("# Recommended: [350-2000]");
		tbr.add("# Default: 350");
		tbr.add("SSEntityRemoverThread: 350");
		if (extra) tbr.add("#:-;-:# SSEntityRemoverThread");
		tbr.add("");
		tbr.add("# The amount of time the inventory thread sleeps for.");
		tbr.add("# The inventory thread takes care of removing DisabledItems,");
		tbr.add("# Decharging EE Tools and Applying the Max EU values.");
		tbr.add("#");
		tbr.add("# Recommended: [250-500]");
		tbr.add("# Default: 400");
		tbr.add("InventoryThread: 400");
		if (extra) tbr.add("#:-;-:# InventoryThread");
		tbr.add("");
		tbr.add("# The amount of time the WorldCleanerThread sleeps for.");
		tbr.add("# This thread takes care of the ChunkUnloader and the WorldScrubber");
		tbr.add("#");
		tbr.add("# Recommended: [30000-90000]");
		tbr.add("# Default: 60000");
		tbr.add("WorldCleanerThread: 60000");
		//if (extra) tbr.add("#:-;-:# WorldCleanerThread"); IMPORTANT: This is commented to switch to new default settings
		tbr.add("");
		tbr.add("# The amount of time the AutoSave Thread sleeps for.");
		tbr.add("# This thread makes sure that all information is correctly saved to the database.");
		tbr.add("# In case of a crash, you will only lose any data that has been modified within");
		tbr.add("# the sleep time of this thread. (e.g. if you set this to 11000, you will only");
		tbr.add("# lose data changed within 11 seconds before the crash)");
		tbr.add("#");
		tbr.add("# Recommended: [10000-30000]");
		tbr.add("# Default: 10000");
		tbr.add("AutoSaveThreadSpeed: 10000");
		//if (extra) tbr.add("#:-;-:# AutoSaveThreadSpeed"); IMPORTANT: This is commented to switch to new default settings
		tbr.add("");
		tbr.add("# Runs a thread for each player upon the Inventory Thread tick.");
		tbr.add("# (May take up some processor if you have a ton of players)");
		tbr.add("#");
		tbr.add("# NOTE: currently unused");
		tbr.add("# ThrottleInventoryThread: false");
		tbr.add("");
		tbr.add("##################################################################################");
		tbr.add("##################################################################################");
		tbr.add("##################################################################################");
		tbr.add("");
		tbr.add("");
		tbr.add("");
		tbr.add("##	Inventory thread enables:    [DisableItems, CreativeDisable, MaxEU, EEDecharger, EEMaxCharger (~198-5000x)]");
		tbr.add("##	WorldCleaner thread enables: [ChunkUnloader, DisabledItemBlockRemover (~40-Xx)]");
		tbr.add("##	AutoSave thread enables:     [SaveToSQL, BlockLimiter RemoveBlk (~8+x)]");
		tbr.add("");
		tbr.add("##################################################################################");
		
		return tbr;
	}
	
	public static void upgradeFile(){
		upgradeFile("TPerformance", convertDefaults(defaultContents(true)));
	}
}
