package de.deutscha.mod;

import java.util.regex.Pattern;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class LegacyColors {
	private static final Pattern CODES = Pattern.compile("(?i)&[0-9a-fk-or]");

	private LegacyColors() {
	}

	public static String strip(String text) {
		return CODES.matcher(text).replaceAll("");
	}

	public static MutableComponent toComponent(String text) {
		MutableComponent result = Component.empty();
		Style style = Style.EMPTY;
		StringBuilder buffer = new StringBuilder();

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '&' && i + 1 < text.length()) {
				ChatFormatting format = ChatFormatting.getByCode(text.charAt(i + 1));
				if (format != null) {
					flush(result, buffer, style);
					if (format == ChatFormatting.RESET) {
						style = Style.EMPTY;
					} else if (isStyleFormat(format)) {
						style = style.applyFormat(format);
					} else {
						style = Style.EMPTY.applyFormat(format);
					}
					i++;
					continue;
				}
			}
			buffer.append(c);
		}
		flush(result, buffer, style);
		return result;
	}

	private static boolean isStyleFormat(ChatFormatting format) {
		return format == ChatFormatting.OBFUSCATED
			|| format == ChatFormatting.BOLD
			|| format == ChatFormatting.STRIKETHROUGH
			|| format == ChatFormatting.UNDERLINE
			|| format == ChatFormatting.ITALIC;
	}

	private static void flush(MutableComponent target, StringBuilder buffer, Style style) {
		if (buffer.isEmpty()) {
			return;
		}
		target.append(Component.literal(buffer.toString()).setStyle(style));
		buffer.setLength(0);
	}
}
