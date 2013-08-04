package org.darkstorm.minecraft.bukkit.serverrestarter;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.bukkit.*;
import org.bukkit.entity.Player;

public class ServerRestartTask implements Runnable {
	private final ServerRestarter plugin;
	private final List<Action> actions = new ArrayList<Action>();
	private final long startTime, restartTime;

	public ServerRestartTask(ServerRestarter plugin, long restartTime) {
		this.plugin = plugin;
		this.restartTime = restartTime;
		startTime = System.currentTimeMillis();
	}

	public synchronized void register(Action action) {
		if(action instanceof OrderDependentAction || !action.activate(getRemainingTime()))
			actions.add(action);
	}

	@Override
	public synchronized void run() {
		long timeLeft = getRemainingTime();
		Iterator<Action> actions = this.actions.iterator();
		List<OrderDependentAction> orderedActions = new ArrayList<OrderDependentAction>();
		while(actions.hasNext()) {
			Action action = actions.next();
			if(action.activate(timeLeft)) {
				if(!(action instanceof OrderDependentAction)) {
					action.perform();
					actions.remove();
				} else
					orderedActions.add((OrderDependentAction) action);
			}
		}
		if(!orderedActions.isEmpty()) {
			Collections.sort(orderedActions, new OrderDependentActionComparator());
			for(OrderDependentAction action : orderedActions) {
				action.perform();
				this.actions.remove(action);
			}
		}
		plugin.updateScoreboard();
		if(timeLeft <= 0)
			plugin.handleRestart();
	}

	public long getRemainingTime() {
		return restartTime - (System.currentTimeMillis() - startTime);
	}

	public static interface Action {
		public boolean activate(long timeLeft);

		public void perform();
	}

	public static interface OrderDependentAction extends Action, Comparable<OrderDependentAction> {
		public long getActivationTime();

		@Override
		public int compareTo(OrderDependentAction action);
	}

	private static final class OrderDependentActionComparator implements Comparator<OrderDependentAction> {
		@Override
		public int compare(OrderDependentAction action1, OrderDependentAction action2) {
			return action1.compareTo(action2);
		}
	}

	public static class MessageAction implements Action {
		private final String message;
		private final long time;

		public MessageAction(String message, long time) {
			this.message = message;
			this.time = time;
		}

		@Override
		public boolean activate(long timeLeft) {
			return timeLeft < time;
		}

		@Override
		public void perform() {
			Bukkit.getServer().broadcastMessage(message);
		}

		@Override
		public String toString() {
			return "message:" + fromTime(time) + ":" + message;
		}
	}

	public static class SoundAction implements Action {
		private final Sound sound;
		private final float volume, pitch;
		private final long time;

		public SoundAction(Sound sound, float volume, float pitch, long time) {
			this.sound = sound;
			this.volume = volume;
			this.pitch = pitch;
			this.time = time;
		}

		@Override
		public boolean activate(long timeLeft) {
			return timeLeft < time;
		}

		@Override
		public void perform() {
			for(Player player : Bukkit.getServer().getOnlinePlayers())
				player.playSound(player.getLocation(), sound, volume, pitch);
		}

		@Override
		public String toString() {
			return "sound:" + fromTime(time) + ":" + sound.name() + ":" + volume + ":" + pitch;
		}
	}

	public static class ScoreboardAction implements OrderDependentAction {
		public enum Type {
			ENABLE,
			DISABLE,
			SET_TITLE,
			SET_FORMAT
		}

		private final Type type;
		private final String argument, secondArgument;
		private final long time;

		public ScoreboardAction(Type type, long time) {
			this(type, null, null, time);

		}

		public ScoreboardAction(Type type, String title, long time) {
			this(type, title, null, time);
		}

		public ScoreboardAction(Type type, String text, String scoreType, long time) {
			if(text == null && scoreType == null) {
				if(type != Type.ENABLE && type != Type.DISABLE)
					throw new UnsupportedOperationException();
			} else if(text != null && scoreType == null) {
				if(type != Type.SET_TITLE)
					throw new UnsupportedOperationException();
			} else if(text != null && scoreType != null) {
				if(type != Type.SET_FORMAT)
					throw new UnsupportedOperationException();
				if(!scoreType.matches("(?i)d|h|m|s|ms"))
					throw new IllegalArgumentException();
			} else
				throw new IllegalArgumentException();

			this.type = type;
			argument = text;
			secondArgument = scoreType;
			this.time = time;
		}

		@Override
		public boolean activate(long timeLeft) {
			return timeLeft < time;
		}

		@Override
		public void perform() {
			ServerRestarter plugin = ServerRestarter.getInstance();
			switch(type) {
			case ENABLE:
				plugin.enableScoreboard();
				break;
			case DISABLE:
				plugin.disableScoreboard();
				break;
			case SET_TITLE:
				plugin.setScoreboardTitle(argument);
				break;
			case SET_FORMAT:
				plugin.setScoreboardFormat(argument, secondArgument);
				break;
			default:
			}
		}

		@Override
		public long getActivationTime() {
			return time;
		}

		@Override
		public int compareTo(OrderDependentAction action) {
			if(time == action.getActivationTime() && action instanceof ScoreboardAction)
				if((type == Type.SET_TITLE || type == Type.SET_FORMAT) && ((ScoreboardAction) action).type == Type.ENABLE)
					return -1;
			return -compare(time, action.getActivationTime());
		}

		@Override
		public String toString() {
			switch(type) {
			case ENABLE:
			case DISABLE:
				return "scoreboard:" + fromTime(time) + ":enable";
			case SET_TITLE:
				return "scoreboard:" + fromTime(time) + ":settitle:" + argument;
			case SET_FORMAT:
				return "scoreboard:" + fromTime(time) + ":setformat:" + secondArgument + ":" + argument;
			default:
				return "scoreboard:" + fromTime(time) + ":unknown:" + argument + ":" + secondArgument;
			}
		}
	}

	private static String fromTime(long time) {
		int days = (int) TimeUnit.MILLISECONDS.toDays(time);
		int hours = (int) TimeUnit.MILLISECONDS.toHours(time - TimeUnit.DAYS.toMillis(days));
		int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(time - TimeUnit.DAYS.toMillis(days) - TimeUnit.HOURS.toMillis(hours));
		int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(time - TimeUnit.DAYS.toMillis(days) - TimeUnit.HOURS.toMillis(hours) - TimeUnit.MINUTES.toMillis(minutes));
		int milliseconds = (int) (time - TimeUnit.DAYS.toMillis(days) - TimeUnit.HOURS.toMillis(hours) - TimeUnit.MINUTES.toMillis(minutes) - TimeUnit.SECONDS.toMillis(seconds));
		StringBuilder builder = new StringBuilder();
		if(days > 0)
			builder.append(days).append('d');
		if(hours > 0)
			builder.append(hours).append('h');
		if(minutes > 0)
			builder.append(minutes).append('m');
		if(seconds > 0)
			builder.append(seconds).append('s');
		if(milliseconds > 0 || builder.length() == 0)
			builder.append(milliseconds).append("ms");
		return builder.toString();
	}

	private static int compare(long x, long y) {
		return (x < y) ? -1 : ((x == y) ? 0 : 1);
	}
}
