package de.deutscha.mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

public final class RainbowText {
	private static final String PREFIX = "Owner | ";
	private static final int[] NAME_GRADIENT = {0xAA0000, 0xFF5555};
	private static final int MAX_ANVIL_KEY_LENGTH = 60;

	private record Glyph(int position, Style style, int codePoint) {
	}

	private RainbowText() {
	}

	public static FormattedCharSequence apply(FormattedCharSequence original) {
		return sink -> {
			List<Glyph> glyphs = new ArrayList<>();
			original.accept((position, style, codePoint) -> {
				glyphs.add(new Glyph(position, style, codePoint));
				return true;
			});

			List<Glyph> result = anvilNames(glyphs);
			if (result == null) {
				result = ownerName(glyphs);
			}
			for (Glyph glyph : result) {
				if (!sink.accept(glyph.position(), glyph.style(), glyph.codePoint())) {
					return false;
				}
			}
			return true;
		};
	}

	private static List<Glyph> anvilNames(List<Glyph> glyphs) {
		if (glyphs.isEmpty() || glyphs.size() > MAX_ANVIL_KEY_LENGTH) {
			return null;
		}
		StringBuilder plain = new StringBuilder();
		for (Glyph glyph : glyphs) {
			plain.appendCodePoint(glyph.codePoint());
		}
		String colored = AnvilNameStore.lookup(plain.toString());
		if (colored == null) {
			return null;
		}
		List<Glyph> out = new ArrayList<>();
		Component component = LegacyColors.toComponent(colored);
		component.visit((style, content) -> {
			content.codePoints().forEach(codePoint -> out.add(new Glyph(out.size(), style, codePoint)));
			return Optional.empty();
		}, Style.EMPTY);
		return out;
	}

	private static List<Glyph> ownerName(List<Glyph> glyphs) {
		int[] name = Minecraft.getInstance().getUser().getName().codePoints().toArray();
		int length = name.length;
		if (length == 0) {
			return glyphs;
		}
		List<Glyph> out = new ArrayList<>();
		int i = 0;
		while (i < glyphs.size()) {
			if (!matchesAt(glyphs, i, name)) {
				out.add(glyphs.get(i));
				i++;
				continue;
			}
			boolean hasPrefix = endsWithPrefix(glyphs, i);
			int prefixLength = hasPrefix ? 0 : PREFIX.length();
			int total = prefixLength + length;
			Glyph first = glyphs.get(i);
			int index = 0;
			for (int k = 0; k < prefixLength; k++) {
				out.add(styled(first.position(), first.style(), PREFIX.codePointAt(k), index++, total));
			}
			for (int k = 0; k < length; k++) {
				out.add(styled(first.position() + k, glyphs.get(i + k).style(), glyphs.get(i + k).codePoint(), index++, total));
			}
			i += length;
		}
		return out;
	}

	private static Glyph styled(int position, Style base, int codePoint, int index, int total) {
		Style style = base.withBold(true).withColor(LegacyColors.colorAt(NAME_GRADIENT, index, total));
		return new Glyph(position, style, codePoint);
	}

	private static boolean matchesAt(List<Glyph> glyphs, int start, int[] name) {
		int length = name.length;
		if (start + length > glyphs.size()) {
			return false;
		}
		if (start > 0 && isNameChar(glyphs.get(start - 1).codePoint())) {
			return false;
		}
		if (start + length < glyphs.size() && isNameChar(glyphs.get(start + length).codePoint())) {
			return false;
		}
		for (int k = 0; k < length; k++) {
			if (Character.toLowerCase(glyphs.get(start + k).codePoint()) != Character.toLowerCase(name[k])) {
				return false;
			}
		}
		return true;
	}

	private static boolean endsWithPrefix(List<Glyph> glyphs, int start) {
		int length = PREFIX.length();
		if (start < length) {
			return false;
		}
		for (int k = 0; k < length; k++) {
			if (glyphs.get(start - length + k).codePoint() != PREFIX.charAt(k)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isNameChar(int codePoint) {
		return Character.isLetterOrDigit(codePoint) || codePoint == '_';
	}

	public static int hsvToRgb(float hue) {
		float h = hue * 6.0F;
		int sector = (int) Math.floor(h);
		float f = h - sector;
		float q = 1.0F - f;
		float r;
		float g;
		float b;
		switch (sector % 6) {
			case 0 -> {
				r = 1.0F;
				g = f;
				b = 0.0F;
			}
			case 1 -> {
				r = q;
				g = 1.0F;
				b = 0.0F;
			}
			case 2 -> {
				r = 0.0F;
				g = 1.0F;
				b = f;
			}
			case 3 -> {
				r = 0.0F;
				g = q;
				b = 1.0F;
			}
			case 4 -> {
				r = f;
				g = 0.0F;
				b = 1.0F;
			}
			default -> {
				r = 1.0F;
				g = 0.0F;
				b = q;
			}
		}
		return (Math.round(r * 255.0F) << 16) | (Math.round(g * 255.0F) << 8) | Math.round(b * 255.0F);
	}
}
