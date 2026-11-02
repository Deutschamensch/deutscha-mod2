package de.deutscha.mod;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public final class LegacyColors {
	private static final Pattern CODES = Pattern.compile("(?i)&[0-9a-fk-or.]|&#[0-9a-f]{6}");
	private static final Pattern HEX = Pattern.compile("(?i)&x(?:&[0-9a-f]){6}|&#([0-9a-f]{6})|<#([0-9a-f]{6})>|\\{#([0-9a-f]{6})\\}");

	private LegacyColors() {
	}

	public static String strip(String text) {
		return CODES.matcher(text).replaceAll("");
	}

	public static String collapsePastedGradient(String raw) {
		String text = raw.replace('§', '&');
		Matcher matcher = HEX.matcher(text);
		List<Integer> colors = new ArrayList<>();
		while (matcher.find()) {
			String hex;
			String whole = matcher.group();
			if (whole.regionMatches(true, 0, "&x", 0, 2)) {
				StringBuilder digits = new StringBuilder();
				for (int k = 3; k < whole.length(); k += 2) {
					digits.append(whole.charAt(k));
				}
				hex = digits.toString();
			} else if (matcher.group(1) != null) {
				hex = matcher.group(1);
			} else if (matcher.group(2) != null) {
				hex = matcher.group(2);
			} else {
				hex = matcher.group(3);
			}
			colors.add(Integer.parseInt(hex, 16));
		}
		if (colors.size() < 2) {
			return text;
		}
		int[] array = new int[colors.size()];
		for (int i = 0; i < array.length; i++) {
			array[i] = colors.get(i);
		}
		AnvilNameStore.setGradient(array);
		return "&." + HEX.matcher(text).replaceAll("");
	}

	public static String expandGradient(String text) {
		if (!text.contains("&.")) {
			return text;
		}
		int[] gradient = AnvilNameStore.gradient();
		StringBuilder out = new StringBuilder();
		int i = 0;
		while (i < text.length()) {
			if (!text.startsWith("&.", i)) {
				out.append(text.charAt(i));
				i++;
				continue;
			}
			i += 2;
			int end = i;
			int count = 0;
			while (end < text.length()) {
				char c = text.charAt(end);
				if (c == '&' && end + 1 < text.length()) {
					char next = Character.toLowerCase(text.charAt(end + 1));
					if (next == '.' || next == '#' || (next >= '0' && next <= '9') || (next >= 'a' && next <= 'f') || next == 'r') {
						break;
					}
					if (next >= 'k' && next <= 'o') {
						end += 2;
						continue;
					}
				}
				end += Character.charCount(text.codePointAt(end));
				count++;
			}
			int index = 0;
			int k = i;
			while (k < end) {
				char c = text.charAt(k);
				if (c == '&' && k + 1 < end) {
					char next = Character.toLowerCase(text.charAt(k + 1));
					if (next >= 'k' && next <= 'o') {
						out.append(c).append(text.charAt(k + 1));
						k += 2;
						continue;
					}
				}
				int codePoint = text.codePointAt(k);
				out.append(String.format("&#%06X", colorAt(gradient, index, count)));
				out.appendCodePoint(codePoint);
				index++;
				k += Character.charCount(codePoint);
			}
			i = end;
		}
		return out.toString();
	}

	public static int colorAt(int[] colors, int index, int count) {
		if (colors.length == 1 || count <= 1) {
			return colors[0];
		}
		float position = (float) index / (count - 1) * (colors.length - 1);
		int low = (int) Math.floor(position);
		int high = Math.min(low + 1, colors.length - 1);
		float fraction = position - low;
		int r = mix((colors[low] >> 16) & 0xFF, (colors[high] >> 16) & 0xFF, fraction);
		int g = mix((colors[low] >> 8) & 0xFF, (colors[high] >> 8) & 0xFF, fraction);
		int b = mix(colors[low] & 0xFF, colors[high] & 0xFF, fraction);
		return (r << 16) | (g << 8) | b;
	}

	private static int mix(int a, int b, float fraction) {
		return Math.round(a + (b - a) * fraction);
	}

	public static MutableComponent toComponent(String text) {
		MutableComponent result = Component.empty();
		Style style = Style.EMPTY;
		StringBuilder buffer = new StringBuilder();

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '&' && i + 1 < text.length()) {
				char next = text.charAt(i + 1);
				if (next == '#' && isHex(text, i + 2, 6)) {
					flush(result, buffer, style);
					style = style.withColor(TextColor.fromRgb(Integer.parseInt(text.substring(i + 2, i + 8), 16)));
					i += 7;
					continue;
				}
				if (next == '.') {
					i++;
					continue;
				}
				ChatFormatting format = ChatFormatting.getByCode(next);
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

	private static boolean isHex(String text, int start, int length) {
		if (start + length > text.length()) {
			return false;
		}
		for (int i = start; i < start + length; i++) {
			if (Character.digit(text.charAt(i), 16) < 0) {
				return false;
			}
		}
		return true;
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
