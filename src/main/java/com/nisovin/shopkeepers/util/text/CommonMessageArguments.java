package com.nisovin.shopkeepers.util.text;

import java.util.Map;

import com.nisovin.shopkeepers.util.Validate;

class CommonMessageArguments {

	static class MapMessageArguments implements MessageArguments {

		private final Map<String, ?> arguments;

		public MapMessageArguments(Map<String, ?> arguments) {
			Validate.notNull(arguments, "arguments is null");
			this.arguments = arguments;
		}

		@Override
		public Object get(String key) {
			return arguments.get(key);
		}
	}

	static class CombinedMessageArguments implements MessageArguments {

		private final MessageArguments first;
		private final MessageArguments second;

		public CombinedMessageArguments(MessageArguments first, MessageArguments second) {
			Validate.notNull(first, "first is null");
			Validate.notNull(second, "second is null");
			this.first = first;
			this.second = second;
		}

		@Override
		public Object get(String key) {
			Object argument = first.get(key);
			return (argument != null) ? argument : second.get(key);
		}
	}

	static class PrefixedMessageArguments implements MessageArguments {

		private final MessageArguments arguments;
		private final String keyPrefix;

		public PrefixedMessageArguments(MessageArguments arguments, String keyPrefix) {
			Validate.notNull(arguments, "arguments is null");
			Validate.notNull(keyPrefix, "keyPrefix is null");
			this.arguments = arguments;
			this.keyPrefix = keyPrefix;
		}

		@Override
		public Object get(String key) {
			if (!key.startsWith(keyPrefix)) return null;
			String suffixKey = key.substring(keyPrefix.length());
			return arguments.get(suffixKey);
		}
	}

	private CommonMessageArguments() {
	}
}
