package org.darkstorm.minecraft.bukkit.serverrestarter;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.*;

import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;
import org.darkstorm.minecraft.bukkit.config.*;
import org.darkstorm.minecraft.bukkit.serverrestarter.ServerRestartTask.Action;
import org.darkstorm.minecraft.bukkit.serverrestarter.ServerRestartTask.MessageAction;
import org.darkstorm.minecraft.bukkit.serverrestarter.ServerRestartTask.ScoreboardAction;
import org.darkstorm.minecraft.bukkit.serverrestarter.ServerRestartTask.SoundAction;

public class ServerRestarter extends JavaPlugin {
	private static final Pattern colorPattern = Pattern.compile("(?i)&[0-9A-FK-OR]");

	private static ServerRestarter instance;

	private Config config;

	private ServerRestartTask task;
	private int taskId;
	private List<Action> defaultActions;

	private String restartMessage;
	private boolean restarting;

	private Scoreboard scoreboard;
	private Objective objective;
	private String scoreboardTitle, scoreboardText, scoreboardScoreType;

	public ServerRestarter() {
		instance = this;
	}

	@Override
	public void onEnable() {
		if(!getDataFolder().exists())
			getDataFolder().mkdir();
		File configFile = new File(getDataFolder(), "config.yml");
		ConfigLoader configLoader = new YamlConfigLoader(configFile);

		if(!configFile.exists())
			config = configLoader.load(ServerRestarterConfigNodes.values());
		else
			config = configLoader.load(excludeMessages(ServerRestarterConfigNodes.values()));

		List<Action> defaultActions = new ArrayList<Action>();
		for(String node : config.getSubNodes("actions")) {
			String value = config.getString(node);
			try {
				String[] parts = value.split(":");
				if(parts[0].equals("message")) {
					String time = parts[1];
					if(!Config.isTimeValue(time)) {
						getLogger().warning("Action '" + node + "' has unknown time value '" + time + "'");
						continue;
					}
					String message = value.substring(("message:" + time + ":").length());
					defaultActions.add(new MessageAction(replaceColorCodes(message), Config.getTimeValue(time)));
				} else if(parts[0].equals("sound")) {
					String time = parts[1];
					if(!Config.isTimeValue(time)) {
						getLogger().warning("Action '" + node + "' has unknown time value '" + time + "'");
						continue;
					}
					Sound sound;
					try {
						sound = Sound.valueOf(parts[2]);
					} catch(Exception exception) {
						getLogger().warning("Action '" + node + "' has unknown sound '" + parts[2] + "'");
						continue;
					}
					defaultActions.add(new SoundAction(sound, Float.parseFloat(parts[3]), Float.parseFloat(parts[4]), Config.getTimeValue(time)));
				} else if(parts[0].equals("scoreboard")) {
					String time = parts[1];
					if(!Config.isTimeValue(time)) {
						getLogger().warning("Action '" + node + "' has unknown time value '" + time + "'");
						continue;
					}
					if(parts[2].equalsIgnoreCase("enable"))
						defaultActions.add(new ScoreboardAction(ScoreboardAction.Type.ENABLE, Config.getTimeValue(time)));
					else if(parts[2].equalsIgnoreCase("disable"))
						defaultActions.add(new ScoreboardAction(ScoreboardAction.Type.DISABLE, Config.getTimeValue(time)));
					else if(parts[2].equalsIgnoreCase("settitle")) {
						String title = value.substring(("scoreboard:" + time + ":settitle:").length());
						defaultActions.add(new ScoreboardAction(ScoreboardAction.Type.SET_TITLE, title, Config.getTimeValue(time)));
					} else if(parts[2].equalsIgnoreCase("setformat")) {
						String format = value.substring(("scoreboard:" + time + ":setformat:" + parts[3] + ":").length());
						defaultActions.add(new ScoreboardAction(ScoreboardAction.Type.SET_FORMAT, format, parts[3], Config.getTimeValue(time)));
					} else
						getLogger().warning("Action '" + node + "' has unknown scoreboard action '" + parts[2] + "'");
				} else
					getLogger().warning("Action '" + node + "' is has unknown type '" + parts[0] + "'");
			} catch(NumberFormatException exception) {
				getLogger().warning("Action '" + node + "' has a non-number where a number is expected");
			} catch(Exception exception) {
				getLogger().warning("Action '" + node + "' is formatted wrong");
			}
		}
		this.defaultActions = Collections.unmodifiableList(defaultActions);
		restartMessage = replaceColorCodes(config.getString(ServerRestarterConfigNodes.KICK_MESSAGE));
		resetTask(config.getTime(ServerRestarterConfigNodes.RESTART_TIME));

		if(config.getBoolean(ServerRestarterConfigNodes.CREATE_STATE_FILE)) {
			File file = new File(config.getString(ServerRestarterConfigNodes.STATE_FILE));
			if(file.isDirectory())
				getLogger().severe("Status file is a directory!");
			try {
				file.createNewFile();
			} catch(IOException exception) {
				getLogger().severe("Unable to create status file!");
				exception.printStackTrace();
			}
		}

		getLogger().info("ServerRestarter loaded.");
	}

	private synchronized void resetTask(long newTime) {
		disableScoreboard();
		if(task != null)
			getServer().getScheduler().cancelTask(taskId);
		task = new ServerRestartTask(this, newTime);
		for(Action action : defaultActions)
			task.register(action);
		taskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, task, 1, 1);
	}

	@Override
	public void onDisable() {
		if(!restarting) {
			if(config.getBoolean(ServerRestarterConfigNodes.CREATE_STATE_FILE)) {
				File file = new File(config.getString(ServerRestarterConfigNodes.STATE_FILE));
				if(file.isDirectory())
					getLogger().severe("Status file is a directory!");
				if(file.exists() && !file.delete())
					getLogger().severe("Unable to delete status file!");
			}
		}
		getLogger().info("ServerRestarter unloaded.");
	}

	private ConfigNode[] excludeMessages(ConfigNode[] nodes) {
		List<ConfigNode> newNodes = new ArrayList<ConfigNode>();
		for(ConfigNode node : nodes)
			if(!node.getNode().startsWith("actions."))
				newNodes.add(node);
		return newNodes.toArray(new ConfigNode[newNodes.size()]);
	}

	public void handleRestart() {
		restarting = true;
		if(config.getBoolean(ServerRestarterConfigNodes.CREATE_STATE_FILE)) {
			File file = new File(config.getString(ServerRestarterConfigNodes.STATE_FILE));
			if(file.isDirectory())
				getLogger().severe("Status file is a directory!");
			try {
				file.createNewFile();
			} catch(IOException exception) {
				getLogger().severe("Unable to create status file!");
				exception.printStackTrace();
			}
		}
		for(Player player : getServer().getOnlinePlayers())
			player.kickPlayer(replaceColorCodes(restartMessage));
		getServer().shutdown();
	}

	private String replaceColorCodes(String message) {
		Matcher matcher = colorPattern.matcher(message);
		while(matcher.find())
			message = message.substring(0, matcher.start()) + "\247" + message.substring(matcher.end() - 1);
		return message;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if(!command.getName().equalsIgnoreCase("restart") && !command.getName().equalsIgnoreCase("sr"))
			return false;
		if(!sender.isOp() && !sender.hasPermission("sr.restart")) {
			sender.sendMessage(ChatColor.RED + "You do not have permission to do this.");
			return true;
		}
		if(args.length > 0) {
			String message = null;
			if(Config.isTimeValue(args[0].toLowerCase())) {
				resetTask(Config.getTimeValue(args[0].toLowerCase()));
				sender.sendMessage(ChatColor.YELLOW + "Set restart time to " + args[0].toLowerCase() + ".");
				if(args.length > 1) {
					String[] parts = new String[args.length - 1];
					System.arraycopy(args, 1, parts, 0, parts.length);
					message = StringUtils.join(parts, ' ');
				}
			} else if(args.length == 1 && args[0].equalsIgnoreCase("reset")) {
				restartMessage = replaceColorCodes(config.getString(ServerRestarterConfigNodes.KICK_MESSAGE));
				resetTask(config.getTime(ServerRestarterConfigNodes.RESTART_TIME));
				sender.sendMessage(ChatColor.YELLOW + "Reset time and message to default setting!");
			} else
				message = StringUtils.join(args, ' ');
			if(message != null) {
				restartMessage = replaceColorCodes(message);
				sender.sendMessage(ChatColor.YELLOW + "Set restart message to '" + ChatColor.RESET + restartMessage + ChatColor.RESET + ChatColor.YELLOW + "'.");
			}
		} else
			handleRestart();
		return true;
	}

	public Config getConfiguration() {
		return config;
	}

	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	public String getScoreboardTitle() {
		return scoreboardTitle;
	}

	public void setScoreboardTitle(String scoreboardTitle) {
		if(scoreboardTitle == null)
			throw new NullPointerException();
		scoreboardTitle = replaceColorCodes(scoreboardTitle);
		this.scoreboardTitle = scoreboardTitle;
		if(scoreboard != null)
			objective.setDisplayName(scoreboardTitle);
	}

	public String getScoreboardText() {
		return scoreboardText;
	}

	public String getScoreboardScoreType() {
		return scoreboardScoreType;
	}

	public void setScoreboardFormat(String scoreboardText, String scoreboardScoreType) {
		if(scoreboardText == null || scoreboardScoreType == null)
			throw new NullPointerException();
		if(!scoreboardScoreType.matches("(?i)d|h|m|s|ms"))
			throw new IllegalArgumentException();
		String oldText = this.scoreboardText;
		scoreboardText = replaceColorCodes(scoreboardText);
		this.scoreboardText = scoreboardText;
		this.scoreboardScoreType = scoreboardScoreType;
		if(scoreboard != null && oldText != null) {
			OfflinePlayer target = getServer().getOfflinePlayer(oldText);
			scoreboard.resetScores(target);
			target = getServer().getOfflinePlayer(scoreboardText);
			objective.getScore(target).setScore((int) getTimeFromType(scoreboardScoreType));
		}
	}

	public void enableScoreboard() {
		if(scoreboardText == null || scoreboardScoreType == null)
			throw new NullPointerException("No scoreboard format specified");
		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		objective = scoreboard.registerNewObjective("restart_display", "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(scoreboardTitle);
		updateScoreboard();
	}

	public void disableScoreboard() {
		if(scoreboard == null)
			return;
		objective.unregister();
		scoreboard = null;
		objective = null;
	}

	public void updateScoreboard() {
		if(scoreboard == null)
			return;
		OfflinePlayer target = getServer().getOfflinePlayer(scoreboardText);
		objective.getScore(target).setScore((int) getTimeFromType(scoreboardScoreType));

		for(Player player : getServer().getOnlinePlayers())
			player.setScoreboard(scoreboard);
	}

	private long getTimeFromType(String type) {
		type = type.toLowerCase();
		long remaining = task.getRemainingTime();
		if(type.equals("d"))
			return TimeUnit.MILLISECONDS.toDays(remaining) + 1;
		else if(type.equals("h"))
			return TimeUnit.MILLISECONDS.toHours(remaining) + 1;
		else if(type.equals("m"))
			return TimeUnit.MILLISECONDS.toMinutes(remaining) + 1;
		else if(type.equals("s"))
			return TimeUnit.MILLISECONDS.toSeconds(remaining) + 1;
		else if(type.equals("ms"))
			return TimeUnit.MILLISECONDS.toMillis(remaining) + 1;
		else
			throw new IllegalArgumentException();
	}

	public static ServerRestarter getInstance() {
		return instance;
	}
}
