package dev.jorel.commandapi.config;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public record VelocityConfigurationAdapter(YamlConfigurationLoader loader, CommentedConfigurationNode config, DefaultVelocityConfig defaultVelocityConfig) implements ConfigurationAdapter<ConfigurationNode> {

	public static VelocityConfigurationAdapter createMinimalInstance(YamlConfigurationLoader loader) {
		return new VelocityConfigurationAdapter(loader, null, null);
	}

	@Override
	public void setValue(String key, Object value) {
		try {
			node(key).set(value);
		} catch (SerializationException e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void setComment(String key, String[] comment) {
		node(key).comment(String.join("\n", comment));
	}

	@Override
	public Object getValue(String key) {
		try {
			return node(key).get(Object.class);
		} catch (SerializationException e) {
			e.printStackTrace(System.err);
			return null;
		}
	}

	@Override
	public String[] getComment(String key) {
		CommentedConfigOption<?> option = defaultVelocityConfig.getAllOptions().get(key);
		CommentedSection section = defaultVelocityConfig.getAllSections().get(key);
		return option != null ? option.comment() : section.comment();
	}

	@Override
	public Set<String> getKeys() {
		return nestedOptions(config);
	}

	@Override
	public boolean contains(String key) {
		return !node(key).virtual();
	}

	@Override
	public void tryCreateSection(String key) {
	}

	@Override
	public CommentedConfigurationNode config() {
		return config;
	}

	@Override
	public ConfigurationAdapter<ConfigurationNode> complete() {
		return this;
	}

	@Override
	public ConfigurationAdapter<ConfigurationNode> createNew() {
		return new VelocityConfigurationAdapter(loader, loader.createNode(), DefaultVelocityConfig.createDefault());
	}

	@Override
	public ConfigurationAdapter<ConfigurationNode> loadFromFile() throws IOException {
		return new VelocityConfigurationAdapter(loader, loader.load(), DefaultVelocityConfig.createDefault());
	}

	@Override
	public void saveToFile() throws IOException {
		loader.save(config);
	}

	@SuppressWarnings("ConfusingArgumentToVarargsMethod")
	private CommentedConfigurationNode node(String path) {
		return config.node(path.split("\\."));
	}

	private Set<String> nestedOptions(ConfigurationNode node) {
		Set<String> keys = new HashSet<>();
		for (Object key : node.childrenMap().keySet()) {
			ConfigurationNode nestedNode = node.childrenMap().get(key);
			if (nestedNode.childrenMap().isEmpty()) {
				keys.add((String) key);
			} else {
				for (String nestedKey : nestedOptions(nestedNode)) {
					keys.add(key + "." + nestedKey);
				}
			}
		}
		return keys;
	}

}
