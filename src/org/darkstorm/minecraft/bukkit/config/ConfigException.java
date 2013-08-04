package org.darkstorm.minecraft.bukkit.config;

public class ConfigException extends RuntimeException {
	private static final long serialVersionUID = -5239403530994638132L;

	public ConfigException() {
	}

	public ConfigException(String reason) {
		super(reason);
	}

	public ConfigException(Throwable cause) {
		super(cause);
	}

	public ConfigException(String reason, Throwable cause) {
		super(reason, cause);
	}
}
