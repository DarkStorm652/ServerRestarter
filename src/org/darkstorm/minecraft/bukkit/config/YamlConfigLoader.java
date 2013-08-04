package org.darkstorm.minecraft.bukkit.config;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import org.bukkit.configuration.InvalidConfigurationException;

public class YamlConfigLoader implements ConfigLoader {
	private final File file;

	public YamlConfigLoader(File file) {
		if(file == null)
			throw new NullPointerException();
		this.file = file;
	}

	@Override
	public Config load(ConfigNode[] defaultNodes) {
		return load(defaultNodes, null);
	}

	@Override
	public Config load(ConfigNode[] defaultNodes, Logger logger) {
		CommentedYamlConfiguration configuration = new CommentedYamlConfiguration();
		try {
			if(!file.exists())
				saveDefaults(configuration, defaultNodes);
			configuration.load(file);
		} catch(FileNotFoundException exception) {
			if(logger != null)
				logger.info("Error: configuration file not found");
		} catch(IOException exception) {
			if(logger != null)
				logger.info("Error: unable to load configuration");
		} catch(InvalidConfigurationException exception) {
			if(logger != null)
				logger.info("Error: syntax error in configuration");
		}
		Map<String, String> nodes = new HashMap<String, String>();
		Map<String, Object> configValues = configuration.getValues(true);
		for(String node : configValues.keySet()) {
			Object value = configValues.get(node);
			if(value instanceof List<?>) {
				List<?> list = (List<?>) value;
				StringBuilder builder = new StringBuilder();
				for(Object o : list) {
					if(builder.length() > 0)
						builder.append(',');
					builder.append(o);
				}
				value = builder;
			}
			nodes.put(node.toLowerCase(), value.toString());
		}
		for(ConfigNode node : defaultNodes) {
			String path = node.getNode();
			if(path == null)
				throw new NullPointerException();
			String value = nodes.get(node.getNode());
			if(value == null) {
				if(logger != null)
					logger.info("Error: config node '" + node.getNode() + "' not found");
				value = node.getDefaultValue();
			} else if(!node.isValid(value.toString())) {
				if(logger != null)
					logger.info("Error: invalid config node '" + node.getNode() + "'");
				value = node.getDefaultValue();
			}
			nodes.put(path, value.toString());
		}
		return new Config(nodes);
	}

	@Override
	public void save(Config config, ConfigNode[] nodes) {
		save(config, nodes, null);
	}

	@Override
	public void save(Config config, ConfigNode[] nodes, Logger logger) {
		CommentedYamlConfiguration configuration = new CommentedYamlConfiguration();
		for(ConfigNode node : nodes) {
			configuration.set(node.getNode(), config.getString(node));
			List<String> comments = node.getComments();
			if(!comments.isEmpty())
				configuration.addComment(node.getNode(), comments.toArray(new String[comments.size()]));
		}
		try {
			configuration.save(file);
		} catch(IOException exception) {
			if(logger != null)
				logger.info("Error: unable to save configuration");
		}
	}

	private void saveDefaults(CommentedYamlConfiguration configuration, ConfigNode[] defaultNodes) throws IOException {
		for(ConfigNode node : defaultNodes) {
			configuration.set(node.getNode(), node.getDefaultValue());
			List<String> comments = node.getComments();
			if(!comments.isEmpty())
				configuration.addComment(node.getNode(), comments.toArray(new String[comments.size()]));
		}
		configuration.save(file);
	}
}
