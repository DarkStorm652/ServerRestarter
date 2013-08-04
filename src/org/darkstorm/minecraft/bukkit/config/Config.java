package org.darkstorm.minecraft.bukkit.config;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.*;

public final class Config {
	private static final Pattern timePattern = Pattern.compile("(?i)([0-9]+d)?([0-9]+h)?([0-9]+m)?([0-9]+s)?([0-9]+ms)?");

	private final Map<String, String> config;

	public Config(Map<String, String> config) {
		if(config == null)
			throw new NullPointerException();
		for(String key : config.keySet())
			if(key == null)
				throw new IllegalArgumentException("Null key in config");
			else if(config.get(key) == null)
				throw new IllegalArgumentException("Null value in config");
		this.config = Collections.unmodifiableMap(new HashMap<String, String>(config));
	}

	public String getString(ConfigNode node) {
		if(node == null)
			throw new NullPointerException();
		if(node.getNode() == null)
			throw new IllegalArgumentException("ConfigNode node is null");
		String value = getString(node.getNode());
		if(value == null || !node.isValid(value))
			return node.getDefaultValue();
		return value;
	}

	public boolean has(ConfigNode node) {
		if(node == null)
			throw new NullPointerException();
		if(node.getNode() == null)
			throw new IllegalArgumentException("ConfigNode node is null");
		String value = getString(node.getNode());
		return value != null && node.isValid(value);
	}

	public boolean getBoolean(ConfigNode node) {
		return getBooleanValue(getString(node));
	}

	public boolean isBoolean(ConfigNode node) {
		if(!has(node))
			return false;
		return isBooleanValue(getString(node));
	}

	public int getInt(ConfigNode node) {
		return getIntValue(getString(node));
	}

	public boolean isInt(ConfigNode node) {
		if(!has(node))
			return false;
		return isIntValue(getString(node));
	}

	public long getLong(ConfigNode node) {
		return getLongValue(getString(node));
	}

	public boolean isLong(ConfigNode node) {
		if(!has(node))
			return false;
		return isLongValue(getString(node));
	}

	public double getDouble(ConfigNode node) {
		return getDoubleValue(getString(node));
	}

	public boolean isDouble(ConfigNode node) {
		if(!has(node))
			return false;
		return isDoubleValue(getString(node));
	}

	public float getFloat(ConfigNode node) {
		return getFloatValue(getString(node));
	}

	public boolean isFloat(ConfigNode node) {
		if(!has(node))
			return false;
		return isFloatValue(getString(node));
	}

	public List<String> getStringList(ConfigNode node) {
		return getStringListValue(getString(node));
	}

	public long getTime(ConfigNode node) {
		return getTimeValue(getString(node));
	}

	public boolean isTime(ConfigNode node) {
		if(!has(node))
			return false;
		return isTimeValue(getString(node));
	}

	public String getString(String node) {
		if(node == null)
			throw new NullPointerException();
		return config.get(node.toLowerCase());
	}

	public boolean has(String node) {
		if(node == null)
			throw new NullPointerException();
		return config.get(node) != null;
	}

	public List<String> getSubNodes(String node) {
		if(node == null)
			throw new NullPointerException();
		node = node.concat(".");
		List<String> subNodes = new ArrayList<String>();
		for(String subNode : config.keySet())
			if(subNode.startsWith(node) && !subNode.substring(node.length()).isEmpty())
				subNodes.add(subNode);
		return Collections.unmodifiableList(subNodes);
	}

	public boolean getBoolean(String node) {
		String value = getString(node);
		return getBooleanValue(value);
	}

	public static boolean getBooleanValue(String value) {
		if(value != null)
			if(value.equals("true"))
				return true;
			else if(value.equals("false"))
				return false;
		throw new IllegalArgumentException("Invalid value");
	}

	public boolean isBoolean(String node) {
		if(!has(node))
			return false;
		String value = getString(node);
		return isBooleanValue(value);
	}

	public static boolean isBooleanValue(String value) {
		return value.equals("true") || value.equals("false");
	}

	public int getInt(String node) {
		String value = getString(node);
		return getIntValue(value);
	}

	public static int getIntValue(String value) {
		if(value != null) {
			try {
				return Integer.parseInt(value);
			} catch(NumberFormatException exception) {}
		}
		throw new IllegalArgumentException("Invalid value");
	}

	public boolean isInt(String node) {
		if(!has(node))
			return false;
		String value = getString(node);
		return isIntValue(value);
	}

	public static boolean isIntValue(String value) {
		try {
			Integer.parseInt(value);
			return true;
		} catch(NumberFormatException exception) {
			return false;
		}
	}

	public long getLong(String node) {
		String value = getString(node);
		return getLongValue(value);
	}

	public static long getLongValue(String value) {
		if(value != null) {
			try {
				return Long.parseLong(value);
			} catch(NumberFormatException exception) {}
		}
		throw new IllegalArgumentException("Invalid value");
	}

	public boolean isLong(String node) {
		if(!has(node))
			return false;
		String value = getString(node);
		return isLongValue(value);
	}

	public static boolean isLongValue(String value) {
		try {
			Long.parseLong(value);
			return true;
		} catch(NumberFormatException exception) {
			return false;
		}
	}

	public double getDouble(String node) {
		String value = getString(node);
		return getDoubleValue(value);
	}

	public static double getDoubleValue(String value) {
		if(value != null) {
			try {
				return Double.parseDouble(value);
			} catch(NumberFormatException exception) {}
		}
		throw new IllegalArgumentException("Invalid value");
	}

	public boolean isDouble(String node) {
		if(!has(node))
			return false;
		String value = getString(node);
		return isDoubleValue(value);
	}

	public static boolean isDoubleValue(String value) {
		try {
			Double.parseDouble(value);
			return true;
		} catch(NumberFormatException exception) {
			return false;
		}
	}

	public float getFloat(String node) {
		String value = getString(node);
		return getFloatValue(value);
	}

	public static float getFloatValue(String value) {
		if(value != null) {
			try {
				return Float.parseFloat(value);
			} catch(NumberFormatException exception) {}
		}
		throw new IllegalArgumentException("Invalid value");
	}

	public boolean isFloat(String node) {
		if(!has(node))
			return false;
		String value = getString(node);
		return isFloatValue(value);
	}

	public static boolean isFloatValue(String value) {
		try {
			Float.parseFloat(value);
			return true;
		} catch(NumberFormatException exception) {
			return false;
		}
	}

	public List<String> getStringList(String node) {
		String nodeValue = getString(node);
		return getStringListValue(nodeValue);
	}

	public static List<String> getStringListValue(String nodeValue) {
		List<String> values = new ArrayList<String>();
		for(String part : nodeValue.split(",")) {
			String value = part.trim();
			if(value.isEmpty())
				continue;
			values.add(value);
		}
		return Collections.unmodifiableList(values);
	}

	public long getTime(String node) {
		String value = getString(node);
		return getTimeValue(value);
	}

	public static long getTimeValue(String value) {
		Matcher matcher = timePattern.matcher(value);
		if(!matcher.matches())
			throw new IllegalArgumentException("Invalid value");
		long millis = 0;
		millis += toMillis(matcher.group(1), TimeUnit.DAYS);
		millis += toMillis(matcher.group(2), TimeUnit.HOURS);
		millis += toMillis(matcher.group(3), TimeUnit.MINUTES);
		millis += toMillis(matcher.group(4), TimeUnit.SECONDS);
		millis += toMillis(matcher.group(5), TimeUnit.MILLISECONDS);
		return millis;
	}

	private static long toMillis(String value, TimeUnit unit) {
		if(value != null && !value.isEmpty()) {
			Matcher matcher = Pattern.compile("[0-9]+").matcher(value);
			if(!matcher.find())
				return 0;
			return unit.toMillis(Integer.parseInt(matcher.group()));
		}
		return 0;
	}

	public boolean isTime(String node) {
		if(!has(node))
			return false;
		String value = getString(node);
		return isTimeValue(value);
	}

	public static boolean isTimeValue(String value) {
		return timePattern.matcher(value).matches();
	}
}
