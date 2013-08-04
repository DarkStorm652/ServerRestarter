package org.darkstorm.minecraft.bukkit.serverrestarter;

import java.util.*;

import org.darkstorm.minecraft.bukkit.config.ConfigNode;

public enum ServerRestarterConfigNodes implements ConfigNode {
	RESTART_TIME("restart_time", "3h", "Time before restarting the server.", "Format is 0d0h0m0s0ms."),
	KICK_MESSAGE("kick_message", "Server is restarting! Come back in a minute or two!"),
	STATE_FILE("state_file", "auto_restart", "ServerRestarter will create this file in the base server directory", "every time the plugin is enabled, and will", "delete it upon being disabled given that the server is not restarting.", "By not deleting it until being disabled, it guarantees that a server crash will cause a restart (as the plugin is not disabled in a crash).", "This option is useful for scripts to detect if the server shut down for a restart or crash."),
	CREATE_STATE_FILE("create_state_file", "true"),
	ACTIONS_MESSAGE_5M("actions.message_5m", "message:5m:&aServer will restart in 5 minutes...", "You may specify as many of these as you want. Name does not matter; value is the action.", "Currently accepted formats are:", "  message:<time>:<message>", "  sound:<time>:<sound>:<volume>:<pitch>", "scoreboard:<time>:enable|disable|settitle:<title>|setformat:<d|h|m|s|ms>:<text>", "List of sounds can be found at http://jd.bukkit.org/rb/apidocs/org/bukkit/Sound.html."),
	ACTIONS_SCOREBOARD_5M_TITLE("actions.scoreboard_5m_title", "scoreboard:5m1ms:settitle:Restart Time"),
	ACTIONS_SCOREBOARD_5M_FORMAT("actions.scoreboard_5m_format", "scoreboard:5m1ms:setformat:m:&aMinutes left:"),
	ACTIONS_SCOREBOARD_5M_ENABLE("actions.scoreboard_5m_enable", "scoreboard:5m:enable"),
	ACTIONS_SOUND_5M("actions.sound_5m", "sound:5m:NOTE_PIANO:1.0:3.0"),
	ACTIONS_MESSAGE_2M("actions.message_2m", "message:2m:&aServer will restart in 2 minutes..."),
	ACTIONS_MESSAGE_1M("actions.message_1m", "message:1m:&aServer will restart in 1 minute..."),
	ACTIONS_SOUND_1M("actions.sound_1m", "sound:1m:NOTE_PIANO:1.0:2.0"),
	ACTIONS_SOUND2_1M("actions.sound2_1m", "sound:1m:NOTE_BASS_GUITAR:1.0:2.0"),
	ACTIONS_SCOREBOARD_1M_FORMAT("actions.scoreboard_1m_format", "scoreboard:1m:setformat:s:&eSeconds left:"),
	ACTIONS_MESSAGE_30S("actions.message_30s", "message:30s:&aServer will restart in 30 seconds..."),
	ACTIONS_SOUND_30S("actions.sound_30s", "sound:30s:NOTE_PIANO:1.0:1.0"),
	ACTIONS_SCOREBOARD_30S_FORMAT("actions.scoreboard_30s_format", "scoreboard:30s:setformat:s:&6Seconds left:"),
	ACTIONS_MESSAGE_15S("actions.message_15s", "message:15s:&aServer will restart in 15 seconds..."),
	ACTIONS_SOUND_15S("actions.sound_15s", "sound:15s:NOTE_PIANO:1.0:1.0"),
	ACTIONS_SCOREBOARD_15S_FORMAT("actions.scoreboard_15s_format", "scoreboard:15s:setformat:s:&cSeconds left:"),
	ACTIONS_MESSAGE_10S("actions.message_10s", "message:10s:&aServer will restart in 10 seconds..."),
	ACTIONS_SOUND_10S("actions.sound_10s", "sound:10s:NOTE_PIANO:1.0:1.0"),
	ACTIONS_MESSAGE_5S("actions.message_5s", "message:5s:&aServer will restart in 5 seconds..."),
	ACTIONS_SCOREBOARD_5S_FORMAT("actions.scoreboard_5s_format", "scoreboard:5s:setformat:s:&4Seconds left:"),
	ACTIONS_SOUND_5S("actions.sound_5s", "sound:5s:NOTE_PIANO:1.0:1.0"),
	ACTIONS_SOUND_5S300MS("actions.sound_5s300ms", "sound:5s300ms:NOTE_PIANO:1.0:1.0"),
	ACTIONS_MESSAGE_4S("actions.message_4s", "message:4s:&aServer will restart in 4 seconds..."),
	ACTIONS_SOUND_4S("actions.sound_4s", "sound:4s:NOTE_PIANO:1.0:1.0"),
	ACTIONS_SOUND_4S300MS("actions.sound_4s300ms", "sound:4s300ms:NOTE_PIANO:1.0:1.0"),
	ACTIONS_MESSAGE_3S("actions.message_3s", "message:3s:&aServer will restart in 3 seconds..."),
	ACTIONS_SOUND_3S("actions.sound_3s", "sound:3s:NOTE_PIANO:1.0:1.0"),
	ACTIONS_SOUND_3S300MS("actions.sound_3s300ms", "sound:3s300ms:NOTE_PIANO:1.0:1.0"),
	ACTIONS_MESSAGE_2S("actions.message_2s", "message:2s:&aServer will restart in 2 seconds..."),
	ACTIONS_SOUND_2S("actions.sound_2s", "sound:2s:NOTE_PIANO:1.0:1.0"),
	ACTIONS_SOUND_2S300MS("actions.sound_2s300ms", "sound:2s300ms:NOTE_PIANO:1.0:1.0"),
	ACTIONS_MESSAGE_1S("actions.message_1s", "message:1s:&aServer will restart in 1 second..."),
	ACTIONS_SOUND_1S("actions.sound_1s", "sound:1s:NOTE_PIANO:1.0:1.0"),
	ACTIONS_SOUND_1S300MS("actions.sound_1s300ms", "sound:1s300ms:NOTE_PIANO:1.0:1.0"),
	ACTIONS_SOUND_1S600MS("actions.sound_1s600ms", "sound:1s600ms:NOTE_PIANO:1.0:1.0"), ;

	private final String node, defaultValue;
	private final List<String> comments;

	ServerRestarterConfigNodes(String node, String defaultValue, String... comments) {
		this.node = node;
		this.defaultValue = defaultValue;
		this.comments = Collections.unmodifiableList(Arrays.asList(comments));
	}

	@Override
	public String getNode() {
		return node;
	}

	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

	@Override
	public List<String> getComments() {
		return comments;
	}

	@Override
	public boolean isValid(String value) {
		return true;
	}
}
