package org.darkstorm.minecraft.bukkit.config;

import java.util.logging.Logger;

public interface ConfigLoader {
	public Config load(ConfigNode[] defaultNodes);

	public Config load(ConfigNode[] defaultNodes, Logger logger);

	public void save(Config config, ConfigNode[] nodes);

	public void save(Config config, ConfigNode[] nodes, Logger logger);
}
