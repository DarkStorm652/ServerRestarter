package org.darkstorm.minecraft.bukkit.config;

import java.util.List;

public interface ConfigNode {
	public String getNode();

	public String getDefaultValue();

	public List<String> getComments();

	public boolean isValid(String value);
}
