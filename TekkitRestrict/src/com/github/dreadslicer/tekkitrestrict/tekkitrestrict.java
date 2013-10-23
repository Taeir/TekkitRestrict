package com.github.dreadslicer.tekkitrestrict;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import net.minecraft.server.RedPowerLogic;
import net.minecraft.server.RedPowerMachine;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import nl.taico.tekkitrestrict.config.AdvancedConfig;
import nl.taico.tekkitrestrict.config.GeneralConfig;
import nl.taico.tekkitrestrict.config.HackDupeConfig;
import nl.taico.tekkitrestrict.config.ModModificationsConfig;
import nl.taico.tekkitrestrict.config.SafeZonesConfig;
import nl.taico.tekkitrestrict.config.TPerformanceConfig;

import com.github.dreadslicer.tekkitrestrict.Log.Warning;
import com.github.dreadslicer.tekkitrestrict.TRConfigCache.ChunkUnloader;
import com.github.dreadslicer.tekkitrestrict.TRConfigCache.Dupes;
import com.github.dreadslicer.tekkitrestrict.TRConfigCache.Global;
import com.github.dreadslicer.tekkitrestrict.TRConfigCache.Hacks;
import com.github.dreadslicer.tekkitrestrict.TRConfigCache.Listeners;
import com.github.dreadslicer.tekkitrestrict.TRConfigCache.SafeZones;
import com.github.dreadslicer.tekkitrestrict.TRConfigCache.Threads;
import com.github.dreadslicer.tekkitrestrict.Updater.UpdateResult;
import com.github.dreadslicer.tekkitrestrict.commands.TRCommandAlc;
import com.github.dreadslicer.tekkitrestrict.commands.TRCommandCheck;
import com.github.dreadslicer.tekkitrestrict.commands.TRCommandTPIC;
import com.github.dreadslicer.tekkitrestrict.commands.TRCommandTR;
import com.github.dreadslicer.tekkitrestrict.database.Database;
import com.github.dreadslicer.tekkitrestrict.eepatch.EEPSettings;
import com.github.dreadslicer.tekkitrestrict.lib.TRFileConfiguration;
import com.github.dreadslicer.tekkitrestrict.lib.YamlConfiguration;
import com.github.dreadslicer.tekkitrestrict.listeners.Assigner;
import com.github.dreadslicer.tekkitrestrict.objects.TRDupeSettings;
import com.github.dreadslicer.tekkitrestrict.objects.TREnums.ConfigFile;
import com.github.dreadslicer.tekkitrestrict.objects.TREnums.DBType;
import com.github.dreadslicer.tekkitrestrict.objects.TREnums.SSMode;
import com.github.dreadslicer.tekkitrestrict.objects.TRHackSettings;
import com.github.dreadslicer.tekkitrestrict.objects.TRItem;

public class tekkitrestrict extends JavaPlugin {
	
	private static tekkitrestrict instance;
	public static Logger log;
	public static TRFileConfiguration config;
	public static boolean EEEnabled = false;
	public static Boolean EEPatch = null;
	
	/** Indicates if tekkitrestrict is disabling. Threads use this to check if they should stop. */
	public static boolean disable = false;
	
	public static String version;
	public static double dbversion = 1.2;
	public static int dbworking = 0;
	
	public static DBType dbtype = DBType.Unknown;
	public static Database db;
	public static Updater updater = null;
	
	public static ExecutorService basfo = Executors.newCachedThreadPool();
	
	private static TRThread ttt = null;
	private static TRLogFilter filter = null;
	public static LinkedList<YamlConfiguration> configList = new LinkedList<YamlConfiguration>();
	
	/**
	 * Log a warning while the plugin is still loading.
	 * If you type /tr warnings, you will see the warnings again.
	 */
	public static void loadWarning(String warning){
		Warning.loadWarnings.add(warning);
		log.warning(warning);
	}
	
	@Override
	public void onLoad() {
		instance = this; //Set the instance
		log = getLogger(); //Set the logger
		version = getDescription().getVersion();
		Log.init();
		
		//#################### load Config ####################
		saveDefaultConfig(false); //Copy config files

		config = this.getConfigx(); //Load the configuration files
		double configVer = config.getDouble("ConfigVersion", 0.9);
		if (configVer < 1.1)
			UpdateConfigFiles.v09();//0 --> newest
		else if (configVer < 1.5) {
			AdvancedConfig.upgradeFile();
			GeneralConfig.upgradeFile();
			HackDupeConfig.upgradeOldHackFile();
			ModModificationsConfig.upgradeFile();
			SafeZonesConfig.upgradeFile();
			TPerformanceConfig.upgradeFile();
			reloadConfig();
		}
		
		loadConfigCache();
		//#####################################################
		
		
		//##################### load SQL ######################
		
		log.info("[DB] Loading Database...");
		if (!TRDB.loadDB()){
			loadWarning("[DB] Failed to load Database!");
		} else {
			if (dbtype == DBType.SQLite) {
				if (TRDB.initSQLite())
					log.info("[SQLite] SQLite Database loaded!");
				else {
					loadWarning("[SQLite] Failed to load SQLite Database!");
				}
			} else if (dbtype == DBType.MySQL) {
				if (TRDB.initMySQL()){
					log.info("[MySQL] Database connection established!");
				} else {
					loadWarning("[MySQL] Failed to connect to MySQL Database!");
				}
			} else {
				loadWarning("[DB] Unknown Database type set!");
			}
		}
		//#####################################################
		
		
		//###################### RPTimer ######################
		if (config.getBoolean("UseAutoRPTimer")){
			try {
				double value = config.getDouble("RPTimerMin", 0.2d);
				int ticks = (int) Math.round((value-0.1d) * 20d);
				RedPowerLogic.minInterval = ticks; // set minimum interval for logic timers...
				log.info("Set the RedPower Timer Min interval to " + value + " seconds.");
			} catch (Exception e) {
				loadWarning("Setting the RedPower Timer failed!");
			}
		}
		//#####################################################
		
		
		//###################### Patch CC #####################
		if (config.getBoolean("PatchComputerCraft", true)){
			PatchCC.start();
		}
		//#####################################################
		
		//BlockBreaker anti-dupe
		try {
			RedPowerMachine.breakerBlacklist.add(Integer.valueOf(-1 << 15 | 194));
			RedPowerMachine.deployerBlacklist.add(Integer.valueOf(0 << 15 | 6362));
			log.info("Patched BlockBreaker + Auto Crafting Table MK II dupe.");
			log.info("Patched Deployer + REP Crash Bug.");
		} catch (Exception ex){
			loadWarning("Unable to patch BlockBreaker + Auto Crafting Table MK II dupe!");
			loadWarning("Unable to patch Deployer + REP Crash Bug!");
		}
	}
	@Override
	public void onEnable() {
		ttt = new TRThread();
		try {
		Assigner.assign(); //Register the required listeners
		} catch (Exception ex){
			Log.Warning.load("A severe error occurred: Unable to start listeners!");
			Log.Exception(ex, true);
		}
		
		TRSafeZone.init();
		
		TRLimiter.init();

		getCommand("tekkitrestrict").setExecutor(new TRCommandTR());
		getCommand("openalc").setExecutor(new TRCommandAlc());
		getCommand("tpic").setExecutor(new TRCommandTPIC());
		getCommand("checklimits").setExecutor(new TRCommandCheck());

		// determine if EE2 is enabled by using pluginmanager
		PluginManager pm = this.getServer().getPluginManager();
		if (pm.isPluginEnabled("mod_EE"))
			tekkitrestrict.EEEnabled = true;
		else
			tekkitrestrict.EEEnabled = false;

		try {
			if (pm.isPluginEnabled("PermissionsEx")) {
				TRPermHandler.permEx = ru.tehkode.permissions.bukkit.PermissionsEx.getPermissionManager();
				log.info("PEX is enabled!");
			}
		} catch (Exception ex) {
			log.info("Linking with Pex Failed!");
			// Was not able to load permissionsEx
		}
		
		// Initiate noItem, Time-thread and our event listener
		try {
			reload(false, true);//Why reload here?
		} catch (Exception ex) {
			Log.Warning.load("An error occurred: Unable to load settings!");
			Log.Exception(ex, true);
		}
		
		try {
			ttt.init();
		} catch (Exception ex) {
			Log.Warning.load("An error occurred: Unable to start threads!");
			Log.Exception(ex, true);
		}
		
		try {
			initHeartBeat();
		} catch (Exception ex){
			Log.Warning.load("An error occurred: Unable to initiate Limiter correctly!");
			Log.Exception(ex, false);
		}
		
		log.info("Linking with EEPatch for extended functionality ...");
		if (linkEEPatch()){
			boolean success = true;
			EEPSettings.loadMaxCharge();
			EEPSettings.loadAllDisabledActions();
			try {
				Assigner.assignEEPatch();
				//TODO add more here
			} catch (Exception ex){
				success = false;
			}
			
			if (success)
				log.info("Linked with EEPatch!");
			else 
				log.warning("Linking with EEPatch Failed!");
		} else {
			log.info("EEPatch is not available. Extended functionality disabled.");
		}
		
		initMetrics();
		
		if (config.getBoolean("Auto-Update", true)){
			updater = new Updater(this, "tekkit-restrict", this.getFile(), Updater.UpdateType.DEFAULT, true);
		} else if (config.getBoolean("CheckForUpdateOnStartup", true)){
			updater = new Updater(this, "tekkit-restrict", this.getFile(), Updater.UpdateType.NO_DOWNLOAD, true);
			if (updater.getResult() == UpdateResult.UPDATE_AVAILABLE) log.info(ChatColor.GREEN + "There is an update available: " + updater.getLatestVersionString() + ". Use /tr admin update ingame to update.");
		}
		
		//##################### Log Filter ####################
		if (config.getBoolean("UseLogFilter", true)){
			Enumeration<String> cc = LogManager.getLogManager().getLoggerNames();
			filter = new TRLogFilter();
			while (cc.hasMoreElements()){
				Logger.getLogger(cc.nextElement()).setFilter(filter);
			}
		}
		//#####################################################
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				if (Warning.loadWarnings()){
					log.warning("There were some warnings while loading TekkitRestrict!");
					log.warning("Use /tr warnings load to view them again (in case you missed them).");
				}
			}
		});
		
		log.info("TekkitRestrict v" + version + " Enabled!");
		
		// TRThrottler.init();
	}
	@Override
	public void onDisable() {
		disable = true;
		
		ttt.disableItemThread.interrupt();
		ttt.entityRemoveThread.interrupt();
		ttt.gemArmorThread.interrupt();
		ttt.worldScrubThread.interrupt();
		ttt.saveThread.interrupt();
		//ttt.limitFlyThread.interrupt();
		
		try { Thread.sleep(1500); } catch (InterruptedException e) {} //Sleep for 1.5 seconds to allow the savethread to save.

		TRLogger.saveLogs();
		TRLogFilter.disable();
		Log.deinit();
		FileLog.closeAll();
		
		log.info("TekkitRestrict v " + version + " disabled!");
	}
	
	public static tekkitrestrict getInstance() {
		return instance;
	}
	
	private void initMetrics(){
		try {
			Metrics metrics = new Metrics(this);
			Metrics.Graph g = metrics.createGraph("TekkitRestrict Stats");
			
			g.addPlotter(new Metrics.Plotter("Total Safezones") {
				@Override
				public int getValue() {
					return TRSafeZone.zones.size();
				}
			});
			
			g.addPlotter(new Metrics.Plotter("Recipe blocks") {
				@Override
				public int getValue() {
					try{
						int size = 0;
						List<String> ssr = tekkitrestrict.config.getStringList("RecipeBlock");
						for (int i = 0; i < ssr.size(); i++) {
							List<TRItem> iss;
							try {
								iss = TRItemProcessor.processItemString(ssr.get(i));
							} catch (TRException e) {
								continue;
							}
							size += iss.size();
						}
						ssr = tekkitrestrict.config.getStringList("RecipeFurnaceBlock");
						for (int i = 0; i < ssr.size(); i++) {
							List<TRItem> iss;
							try {
								iss = TRItemProcessor.processItemString(ssr.get(i));
							} catch (TRException e) {
								continue;
							}
							size += iss.size();
						}
						return size;
					} catch(Exception ex){
						return 0;
					}
				}
			});
			
			g.addPlotter(new Metrics.Plotter("Disabled items") {
				@Override
				public int getValue() {
					try {
						return TRNoItem.getBannedItemsAmount();
					} catch(Exception ex){
						return 0;
					}
				}
			});
			metrics.start();
		} catch (IOException e) {
			Warning.load("Metrics failed to start.");
		}
	}
	
	public boolean linkEEPatch(){
		if (EEPatch != null) return EEPatch.booleanValue();
		try {
			Class.forName("ee.events.EEEvent");
			EEPatch = true;
			return true;
		} catch (ClassNotFoundException e) {
			EEPatch = false;
			return false;
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static void loadConfigCache(){
		Hacks.flys = new TRHackSettings();
		Hacks.flys.enable = config.getBoolean(ConfigFile.HackDupe, "Anti-Hacks.Fly.Enabled", true);
		Hacks.flys.kick = config.getBoolean(ConfigFile.HackDupe, "Anti-Hacks.Fly.Kick", true);
		Hacks.flys.broadcast = config.getBoolean(ConfigFile.HackDupe, "Anti-Hacks.Fly.Broadcast", true);
		Hacks.flys.tolerance = config.getInt(ConfigFile.HackDupe, "Anti-Hacks.Fly.Tolerance", 40);
		Hacks.flys.value = (int) Math.round(config.getDouble(ConfigFile.HackDupe, "Anti-Hacks.Fly.MinHeight", 3));
		Hacks.flys.useCommand = config.getBoolean(ConfigFile.HackDupe, "Anti-Hacks.Fly.ExecuteCommand.Enabled", false);
		Hacks.flys.command = config.getString(ConfigFile.HackDupe, "Anti-Hacks.Fly.ExecuteCommand.Command", "");
		Hacks.flys.triggerAfter = config.getInt(ConfigFile.HackDupe, "Anti-Hacks.Fly.ExecuteCommand.TriggerAfter", 1);
		
		Hacks.forcefields = new TRHackSettings();
		Hacks.forcefields.enable = config.getBoolean(ConfigFile.HackDupe, "Anti-Hacks.Forcefield.Enabled", true);
		Hacks.forcefields.kick = config.getBoolean(ConfigFile.HackDupe, "Anti-Hacks.Forcefield.Kick", true);
		Hacks.forcefields.broadcast = config.getBoolean(ConfigFile.HackDupe, "Anti-Hacks.Forcefield.Broadcast", true);
		Hacks.forcefields.tolerance = config.getInt(ConfigFile.HackDupe, "Anti-Hacks.Forcefield.Tolerance", 20);
		Hacks.forcefields.value = config.getDouble(ConfigFile.HackDupe, "Anti-Hacks.Forcefield.Angle", 40);
		Hacks.forcefields.useCommand = config.getBoolean(ConfigFile.HackDupe, "Anti-Hacks.Forcefield.ExecuteCommand.Enabled", false);
		Hacks.forcefields.command = config.getString(ConfigFile.HackDupe, "Anti-Hacks.Forcefield.ExecuteCommand.Command", "");
		Hacks.forcefields.triggerAfter = config.getInt(ConfigFile.HackDupe, "Anti-Hacks.Forcefield.ExecuteCommand.TriggerAfter", 1);
		
		Hacks.speeds = new TRHackSettings();
		Hacks.speeds.enable = config.getBoolean(ConfigFile.HackDupe, "Anti-Hacks.MoveSpeed.Enabled", true);
		Hacks.speeds.kick = config.getBoolean(ConfigFile.HackDupe, "Anti-Hacks.MoveSpeed.Kick", true);
		Hacks.speeds.broadcast = config.getBoolean(ConfigFile.HackDupe, "Anti-Hacks.MoveSpeed.Broadcast", true);
		Hacks.speeds.tolerance = config.getInt(ConfigFile.HackDupe, "Anti-Hacks.MoveSpeed.Tolerance", 30);
		Hacks.speeds.value = config.getDouble(ConfigFile.HackDupe, "Anti-Hacks.MoveSpeed.MaxMoveSpeed", 2.5d);
		Hacks.speeds.useCommand = config.getBoolean(ConfigFile.HackDupe, "Anti-Hacks.MoveSpeed.ExecuteCommand.Enabled", false);
		Hacks.speeds.command = config.getString(ConfigFile.HackDupe, "Anti-Hacks.MoveSpeed.ExecuteCommand.Command", "");
		Hacks.speeds.triggerAfter = config.getInt(ConfigFile.HackDupe, "Anti-Hacks.MoveSpeed.ExecuteCommand.TriggerAfter", 1);
		
		Hacks.broadcastFormat = config.getString(ConfigFile.HackDupe, "Anti-Hacks.BroadcastString", "{PLAYER} tried to {TYPE}-hack!"); //TODO add colors
		
		Dupes.alcBags = new TRDupeSettings();
		Dupes.alcBags.prevent = config.getBoolean(ConfigFile.HackDupe, "Anti-Dupes.AlchemyBagDupe.Prevent", true);
		Dupes.alcBags.broadcast = config.getBoolean(ConfigFile.HackDupe, "Anti-Dupes.AlchemyBagDupe.Broadcast", true);
		Dupes.alcBags.kick = config.getBoolean(ConfigFile.HackDupe, "Anti-Dupes.AlchemyBagDupe.Kick", false);
		Dupes.alcBags.useCommand = config.getBoolean(ConfigFile.HackDupe, "Anti-Dupes.AlchemyBagDupe.ExecuteCommand.Enabled", false);
		Dupes.alcBags.command = config.getString(ConfigFile.HackDupe, "Anti-Dupes.AlchemyBagDupe.ExecuteCommand.Command", "");
		Dupes.alcBags.triggerAfter = config.getInt(ConfigFile.HackDupe, "Anti-Dupes.AlchemyBagDupe.ExecuteCommand.TriggerAfter", 1);
		
		Dupes.rmFurnaces = new TRDupeSettings();
		Dupes.rmFurnaces.prevent = config.getBoolean(ConfigFile.HackDupe, "Anti-Dupes.RMFurnaceDupe.Prevent", true);
		Dupes.rmFurnaces.broadcast = config.getBoolean(ConfigFile.HackDupe, "Anti-Dupes.RMFurnaceDupe.Broadcast", true);
		Dupes.rmFurnaces.kick = config.getBoolean(ConfigFile.HackDupe, "Anti-Dupes.RMFurnaceDupe.Kick", false);
		Dupes.rmFurnaces.useCommand = config.getBoolean(ConfigFile.HackDupe, "Anti-Dupes.RMFurnaceDupe.ExecuteCommand.Enabled", false);
		Dupes.rmFurnaces.command = config.getString(ConfigFile.HackDupe, "Anti-Dupes.RMFurnaceDupe.ExecuteCommand.Command", "");
		Dupes.rmFurnaces.triggerAfter = config.getInt(ConfigFile.HackDupe, "Anti-Dupes.RMFurnaceDupe.ExecuteCommand.TriggerAfter", 1);
		
		Dupes.transmutes = new TRDupeSettings();
		Dupes.transmutes.prevent = config.getBoolean(ConfigFile.HackDupe, "Anti-Dupes.TransmuteDupe.Prevent", true);
		Dupes.transmutes.broadcast = config.getBoolean(ConfigFile.HackDupe, "Anti-Dupes.TransmuteDupe.Broadcast", true);
		Dupes.transmutes.kick = config.getBoolean(ConfigFile.HackDupe, "Anti-Dupes.TransmuteDupe.Kick", false);
		Dupes.transmutes.useCommand = config.getBoolean(ConfigFile.HackDupe, "Anti-Dupes.TransmuteDupe.ExecuteCommand.Enabled", false);
		Dupes.transmutes.command = config.getString(ConfigFile.HackDupe, "Anti-Dupes.TransmuteDupe.ExecuteCommand.Command", "");
		Dupes.transmutes.triggerAfter = config.getInt(ConfigFile.HackDupe, "Anti-Dupes.TransmuteDupe.ExecuteCommand.TriggerAfter", 1);
		
		Dupes.tankcarts = new TRDupeSettings();
		Dupes.tankcarts.prevent = config.getBoolean(ConfigFile.HackDupe, "Anti-Dupes.TankCartDupe.Prevent", true);
		Dupes.tankcarts.broadcast = config.getBoolean(ConfigFile.HackDupe, "Anti-Dupes.TankCartDupe.Broadcast", true);
		Dupes.tankcarts.kick = config.getBoolean(ConfigFile.HackDupe, "Anti-Dupes.TankCartDupe.Kick", false);
		Dupes.tankcarts.useCommand = config.getBoolean(ConfigFile.HackDupe, "Anti-Dupes.TankCartDupe.ExecuteCommand.Enabled", false);
		Dupes.tankcarts.command = config.getString(ConfigFile.HackDupe, "Anti-Dupes.TankCartDupe.ExecuteCommand.Command", "");
		Dupes.tankcarts.triggerAfter = config.getInt(ConfigFile.HackDupe, "Anti-Dupes.TankCartDupe.ExecuteCommand.TriggerAfter", 1);
		
		Dupes.tankcartGlitchs = new TRDupeSettings();
		Dupes.tankcartGlitchs.prevent = config.getBoolean(ConfigFile.HackDupe, "Anti-Dupes.TankCartGlitch.Prevent", true);
		Dupes.tankcartGlitchs.broadcast = config.getBoolean(ConfigFile.HackDupe, "Anti-Dupes.TankCartGlitch.Broadcast", true);
		Dupes.tankcartGlitchs.kick = config.getBoolean(ConfigFile.HackDupe, "Anti-Dupes.TankCartGlitch.Kick", false);
		Dupes.tankcartGlitchs.useCommand = config.getBoolean(ConfigFile.HackDupe, "Anti-Dupes.TankCartGlitch.ExecuteCommand.Enabled", false);
		Dupes.tankcartGlitchs.command = config.getString(ConfigFile.HackDupe, "Anti-Dupes.TankCartGlitch.ExecuteCommand.Command", "");
		Dupes.tankcartGlitchs.triggerAfter = config.getInt(ConfigFile.HackDupe, "Anti-Dupes.TankCartGlitch.ExecuteCommand.TriggerAfter", 1);
		
		Dupes.teleports = new TRDupeSettings();
		Dupes.teleports.prevent = config.getBoolean(ConfigFile.HackDupe, "Anti-Dupes.TeleportDupe.Prevent", true);
		Dupes.teleports.broadcast = config.getBoolean(ConfigFile.HackDupe, "Anti-Dupes.TeleportDupe.Broadcast", true);
		Dupes.teleports.kick = config.getBoolean(ConfigFile.HackDupe, "Anti-Dupes.TeleportDupe.Kick", false);
		Dupes.teleports.useCommand = config.getBoolean(ConfigFile.HackDupe, "Anti-Dupes.TeleportDupe.ExecuteCommand.Enabled", false);
		Dupes.teleports.command = config.getString(ConfigFile.HackDupe, "Anti-Dupes.TeleportDupe.ExecuteCommand.Command", "");
		Dupes.teleports.triggerAfter = config.getInt(ConfigFile.HackDupe, "Anti-Dupes.TeleportDupe.ExecuteCommand.TriggerAfter", 1);
		
		Dupes.pedestals = new TRDupeSettings();
		Dupes.pedestals.prevent = config.getBoolean(ConfigFile.HackDupe, "Anti-Dupes.PedestalEmcGen.Prevent", true);
		Dupes.pedestals.broadcast = config.getBoolean(ConfigFile.HackDupe, "Anti-Dupes.PedestalEmcGen.Broadcast", true);
		Dupes.pedestals.kick = config.getBoolean(ConfigFile.HackDupe, "Anti-Dupes.PedestalEmcGen.Kick", false);
		Dupes.pedestals.useCommand = config.getBoolean(ConfigFile.HackDupe, "Anti-Dupes.PedestalEmcGen.ExecuteCommand.Enabled", false);
		Dupes.pedestals.command = config.getString(ConfigFile.HackDupe, "Anti-Dupes.PedestalEmcGen.ExecuteCommand.Command", "");
		Dupes.pedestals.triggerAfter = config.getInt(ConfigFile.HackDupe, "Anti-Dupes.PedestalEmcGen.ExecuteCommand.TriggerAfter", 1);
		
		Dupes.broadcastFormat = config.getString(ConfigFile.HackDupe, "Anti-Dupes.BroadcastString", "{PLAYER} tried to dupe {ITEM} using {TYPE}!"); //TODO add colors
		
		Global.debug = config.getBoolean(ConfigFile.General, "ShowDebugMessages", false) ||
					   config.getBoolean(ConfigFile.Logging, "LogDebug", false);
		
		Global.kickFromConsole = config.getBoolean(ConfigFile.General, "KickFromConsole", false);
		//Global.useNewBanSystem = config.getBoolean("UseNewBannedItemsSystem", false);
		
		Listeners.UseBlockLimit = config.getBoolean(ConfigFile.General, "UseItemLimiter", true);
		Listeners.BlockCreativeContainer = config.getBoolean(ConfigFile.LimitedCreative, "LimitedCreativeNoContainer", true);
		Listeners.UseNoItem = config.getBoolean(ConfigFile.General, "UseNoItem", true);
		Listeners.UseLimitedCreative = config.getBoolean(ConfigFile.General, "UseLimitedCreative", true);
		Listeners.useNoCLickPerms = config.getBoolean(ConfigFile.DisableClick, "UseNoClickPermissions", false);
		
		TRConfigCache.LogFilter.replaceList = config.getStringList(ConfigFile.Logging, "LogFilter");
		TRConfigCache.LogFilter.splitLogs = config.getBoolean(ConfigFile.Logging, "SplitLogs", true);
		TRConfigCache.LogFilter.filterLogs = config.getBoolean(ConfigFile.Logging, "FilterLogs", true);
		TRConfigCache.LogFilter.logLocation = config.getString(ConfigFile.Logging, "SplitLogsLocation", "log");
		TRConfigCache.LogFilter.fileFormat = config.getString(ConfigFile.Logging, "FilenameFormat", "{TYPE}-{DAY}-{MONTH}-{YEAR}.log");
		TRConfigCache.LogFilter.logFormat = config.getString(ConfigFile.Logging, "LogStringFormat", "[{HOUR}:{MINUTE}:{SECOND}] {INFO}");
		
		Threads.gemArmorSpeed = config.getInt(ConfigFile.TPerformance, "GemArmorDThread", 120);
		Threads.inventorySpeed = config.getInt(ConfigFile.TPerformance, "InventoryThread", 400);
		Threads.saveSpeed = config.getInt(ConfigFile.TPerformance, "AutoSaveThreadSpeed", 11000);
		Threads.SSEntityRemoverSpeed = config.getInt(ConfigFile.TPerformance, "SSEntityRemoverThread", 350);
		Threads.worldCleanerSpeed = config.getInt(ConfigFile.TPerformance, "WorldCleanerThread", 15000);
		
		Threads.GAMovement = config.getBoolean(ConfigFile.ModModifications, "AllowGemArmorDefensive", true);
		Threads.GAOffensive = config.getBoolean(ConfigFile.ModModifications, "AllowGemArmorOffensive", false);
		
		Threads.SSDisableEntities = config.getBoolean(ConfigFile.SafeZones, "InSafeZones.DisableEntities", false);
		Threads.SSDechargeEE = config.getBoolean(ConfigFile.SafeZones, "InSafeZones.DechargeEE", true);
		Threads.SSDisableArcane = config.getBoolean(ConfigFile.SafeZones, "InSafeZones.DisableRingOfArcana", true);
		List<String> exempt = config.getStringList(ConfigFile.SafeZones, "InSafeZones.ExemptEntityTypes");
		Threads.SSClassBypasses = new ArrayList<Class>();
		for (String s : exempt){
			try {
				Class cl = Class.forName("org.bukkit.entity."+s);
				Threads.SSClassBypasses.add(cl);
			} catch (Exception ex){
				try {
					Class cl = Class.forName("org.bukkit.entity."+Character.toUpperCase(s.charAt(0)) + s.substring(1));
					Threads.SSClassBypasses.add(cl);
				} catch (Exception ex2){
					Warning.config("Invalid value in ExemptEntityTypes in SafeZones.config: cannot find class org.bukkit.entity."+ s + "!");
					continue;
				}
			}
		}
		
		Threads.RMDB = config.getBoolean(ConfigFile.DisableItems, "RemoveDisabledItemBlocks", false);
		//Threads.UseRPTimer = config.getBoolean(ConfigFile.General, "UseAutoRPTimer", false);
		Threads.ChangeDisabledItemsIntoId = config.getInt(ConfigFile.DisableItems, "ChangeDisabledItemsIntoId", 3);
		//Threads.RPTickTime = (int) Math.round((config.getDouble(ConfigFile.ModModifications, "RPTimerMin", 0.2)-0.1d) * 20d);
		
		SafeZones.UseSafeZones = config.getBoolean(ConfigFile.SafeZones, "UseSafeZones", true);
		SafeZones.UseFactions = config.getBoolean(ConfigFile.SafeZones, "SSEnabledPlugins.Factions", true);
		SafeZones.UseGP = config.getBoolean(ConfigFile.SafeZones, "SSEnabledPlugins.GriefPrevention", true);
		SafeZones.UsePS = config.getBoolean(ConfigFile.SafeZones, "SSEnabledPlugins.PreciousStones", true);
		SafeZones.UseTowny = config.getBoolean(ConfigFile.SafeZones, "SSEnabledPlugins.Towny", true);
		SafeZones.UseWG = config.getBoolean(ConfigFile.SafeZones, "SSEnabledPlugins.WorldGuard", true);
		SafeZones.GPMode = SSMode.parse(config.getString(ConfigFile.SafeZones, "GriefPreventionSafeZoneMethod", "admin"));
		SafeZones.WGMode = SSMode.parse(config.getString(ConfigFile.SafeZones, "WorldGuardSafeZoneMethod", "specific"));
		
		//SafeZones.SSPlugins = config.getStringList("SSEnabledPlugins");
		//SafeZones.SSDisableFly = config.getBoolean("SSDisableFlying", false);
		//SafeZones.allGPClaimsAreSafezone = config.getBoolean("AllGriefPreventionClaimsAreSafezones", false);
		//SafeZones.allowNormalUser = config.getBoolean("SSAllowNormalUsersToHaveSafeZones", true);
		
		ChunkUnloader.enabled = config.getBoolean(ConfigFile.TPerformance, "UseChunkUnloader", false);
		ChunkUnloader.maxChunks = config.getInt(ConfigFile.TPerformance, "MaxChunks", 3000);
		ChunkUnloader.maxChunksEnd = config.getInt(ConfigFile.TPerformance, "MaxChunks.TheEnd", 200);
		ChunkUnloader.maxChunksNether = config.getInt(ConfigFile.TPerformance, "MaxChunks.Nether", 400);
		ChunkUnloader.maxChunksNormal = config.getInt(ConfigFile.TPerformance, "MaxChunks.Normal", 4000);
		ChunkUnloader.maxChunksTotal = config.getInt(ConfigFile.TPerformance, "MaxChunks.Total", 4000);
		ChunkUnloader.unloadOrder = config.getInt(ConfigFile.TPerformance, "UnloadOrder", 0);
		ChunkUnloader.maxRadii = config.getInt(ConfigFile.TPerformance, "MaxRadii", 256);
	}

	/** Make the limiter expire limits every 32 ticks. */
	private static void initHeartBeat() {
		instance.getServer().getScheduler().scheduleAsyncRepeatingTask(instance, new Runnable() {
			@Override
			public void run() {
				TRLimiter.expireLimiters();
			}
		}, 60L, 32L);
	}
	
	public void reload(boolean listeners, boolean silent) {
		if (listeners){
			Assigner.unregisterAll();
		}
		this.reloadConfig();
		config = this.getConfigx();
		loadConfigCache();
		TRItemProcessor.reload();
		TRNoItem.reload(); //Banned items and limited creative.
		TRThread.reload(); // branches out
		TRListener.reload();
		TRLimiter.reload();
		TRLogger.reload();
		TRRecipeBlock.reload();
		TRLimitFlyThread.reload();
		TREMCSet.reload();
		if (linkEEPatch()){
			EEPSettings.loadAllDisabledActions();
			EEPSettings.loadMaxCharge();
			if (listeners)
				Assigner.assignEEPatch();
		}
		
		if (listeners)
			Assigner.assign();
		
		if (!silent) log.info("TekkitRestrict Reloaded!");
	}

	private TRFileConfiguration getConfigx() {
		if (configList.size() == 0) {
			reloadConfigOldHack();
		}
		return (new TRFileConfiguration());
	}

	@Override
	public void reloadConfig() {
		configList.clear();
		configList.add(reloadc("General.config.yml"));
		configList.add(reloadc("Advanced.config.yml"));
		configList.add(reloadc("ModModifications.config.yml"));
		configList.add(reloadc("DisableClick.config.yml"));
		configList.add(reloadc("DisableItems.config.yml"));
		configList.add(reloadc("HackDupe.config.yml"));
		configList.add(reloadc("LimitedCreative.config.yml"));
		configList.add(reloadc("Logging.config.yml"));
		configList.add(reloadc("TPerformance.config.yml"));
		configList.add(reloadc("GroupPermissions.config.yml"));
		configList.add(reloadc("SafeZones.config.yml"));
		configList.add(reloadc("Database.config.yml"));
		if (linkEEPatch()) configList.add(reloadc("EEPatch.config.yml"));
	}
	
	private void reloadConfigOldHack() {
		configList.clear();
		configList.add(reloadc("General.config.yml"));
		configList.add(reloadc("Advanced.config.yml"));
		configList.add(reloadc("ModModifications.config.yml"));
		configList.add(reloadc("DisableClick.config.yml"));
		configList.add(reloadc("DisableItems.config.yml"));
		File hackfile = new File("plugins"+File.separator+"tekkitrestrict"+File.separator+"Hack.config.yml");
		//File newhackfile = new File("plugins"+File.separator+"tekkitrestrict"+File.separator+"HackDupe.config.yml");
		if (hackfile.exists())
			configList.add(reloadc("Hack.config.yml"));
		else
			configList.add(reloadc("HackDupe.config.yml"));
		configList.add(reloadc("LimitedCreative.config.yml"));
		configList.add(reloadc("Logging.config.yml"));
		configList.add(reloadc("TPerformance.config.yml"));
		configList.add(reloadc("GroupPermissions.config.yml"));
		configList.add(reloadc("SafeZones.config.yml"));
		configList.add(reloadc("Database.config.yml"));
		if (linkEEPatch()) configList.add(reloadc("EEPatch.config.yml"));
	}

	private YamlConfiguration reloadc(String loc) {
		File cf = new File("plugins"+File.separator+"tekkitrestrict"+File.separator + loc);
		// tekkitrestrict.log.info(cf.getAbsolutePath());
		YamlConfiguration conf = YamlConfiguration.loadConfiguration(cf);
		// newConfig.loadFromString(s)
		InputStream defConfigStream = getResource(loc);
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			conf.setDefaults(defConfig);
			try {
				defConfigStream.close();
			} catch (IOException e) {
				loadWarning("Exception while trying to reload the config!");
				e.printStackTrace();
			}
		}
		return conf;
	}
	
	@Deprecated
	@Override
	public void saveDefaultConfig() {
		Level ll = log.getLevel();
		log.setLevel(Level.SEVERE);
		try {
			saveResource("General.config.yml", false);
		} catch (Exception e) {}
		try {
			saveResource("Advanced.config.yml", false);
		} catch (Exception e) {}
		try {
			saveResource("ModModifications.config.yml", false);
		} catch (Exception e) {}
		try {
			saveResource("DisableClick.config.yml", false);
		} catch (Exception e) {}
		try {
			saveResource("DisableItems.config.yml", false);
		} catch (Exception e) {}
		try {
			saveResource("HackDupe.config.yml", false);
		} catch (Exception e) {}
		try {
			saveResource("LimitedCreative.config.yml", false);
		} catch (Exception e) {}
		try {
			saveResource("Logging.config.yml", false);
		} catch (Exception e) {}
		try {
			saveResource("TPerformance.config.yml", false);
		} catch (Exception e) {}
		try {
			saveResource("GroupPermissions.config.yml", false);
		} catch (Exception e) {}
		try {
			saveResource("SafeZones.config.yml", false);
		} catch (Exception e) {}
		try {
			saveResource("Database.config.yml", false);
		} catch (Exception e) {}
		try {
			if (linkEEPatch()){
				saveResource("EEPatch.config.yml", false);
			}
		} catch (Exception e) {}
		log.setLevel(ll);
	}
	public void saveDefaultConfig(boolean force) {
		Level ll = log.getLevel();
		log.setLevel(Level.SEVERE);
		try {
			saveResource("General.config.yml", force);
		} catch (Exception e) {}
		try {
			saveResource("Advanced.config.yml", force);
		} catch (Exception e) {}
		try {
			saveResource("ModModifications.config.yml", force);
		} catch (Exception e) {}
		try {
			saveResource("DisableClick.config.yml", force);
		} catch (Exception e) {}
		try {
			saveResource("DisableItems.config.yml", force);
		} catch (Exception e) {}
		try {
			saveResource("HackDupe.config.yml", force);
		} catch (Exception e) {}
		try {
			saveResource("LimitedCreative.config.yml", force);
		} catch (Exception e) {}
		try {
			saveResource("Logging.config.yml", force);
		} catch (Exception e) {}
		try {
			saveResource("TPerformance.config.yml", force);
		} catch (Exception e) {}
		try {
			saveResource("GroupPermissions.config.yml", force);
		} catch (Exception e) {}
		try {
			saveResource("SafeZones.config.yml", force);
		} catch (Exception e) {}
		try {
			saveResource("Database.config.yml", force);
		} catch (Exception e) {}
		try {
			if (linkEEPatch()){
				saveResource("EEPatch.config.yml", force);
			}
		} catch (Exception e) {}
		log.setLevel(ll);
	}
	
	public void Update(){
		updater = new Updater(this, "tekkit-restrict", this.getFile(), Updater.UpdateType.DEFAULT, true);
	}
	
	public boolean backupConfig(String sourceString, String destString){
		try {
			File sourceFile = new File(sourceString);
			File destFile = new File(destString);
			if (!sourceFile.exists()) return false;
			
			if(!destFile.exists()) destFile.createNewFile();
			
			FileChannel source = null;
			FileChannel destination = null;

			try {
				source = new FileInputStream(sourceFile).getChannel();
				destination = new FileOutputStream(destFile).getChannel();
				destination.transferFrom(source, 0, source.size());
			} finally {
				if(source != null) source.close();
				if(destination != null) destination.close();
			}

			if(source != null) source.close();
			if(destination != null) destination.close();

		} catch (IOException ex){
			loadWarning("Cannot backup config: " + sourceString);
			return false;
		}
		return true;

	}

	public static String getFullVersion(){
		return getMajorVersion() + "." + getMinorVersion() + getExtraVersion();
	}
	public static String getMajorVersion(){
		return instance.getDescription().getVersion().split("\\D+")[0];
	}
	public static String getMinorVersion(){
		return instance.getDescription().getVersion().split("\\D+")[1];
	}
	public static String getExtraVersion(){
		String ver = instance.getDescription().getVersion().toLowerCase();
		if (ver.contains("beta")){
			String temp[] = ver.split(" ");
			if (temp.length >= 3 && temp[2].matches("\\d+")){
				return "b" + temp[2];
			} else {
				return "b1";
			}
		} else if (ver.contains("dev")){
			String temp[] = ver.split(" ");
			if (temp.length >= 3 && temp[2].matches("\\d+")){
				return "d" + temp[2];
			} else {
				return "d1";
			}
		} else {
			return "";
		}
	}
	
	public static boolean isBeta(){
		return getExtraVersion().contains("b");
	}
	public static boolean isDev(){
		return getExtraVersion().contains("d");
	}
}
