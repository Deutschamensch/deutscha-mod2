package de.deutscha.mod;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

public final class RainbowText {
	private static final long CYCLE_MILLIS = 3000L;
	private static final float HUE_STEP_PER_LETTER = 0.07F;

	private record Glyph(int position, Style style, int codePoint) {
	}

	private RainbowText() {
	}

	public static FormattedCharSequence apply(FormattedCharSequence original) {
		return sink -> {
			String name = Minecraft.getInstance().getUser().getName();
			List<Glyph> glyphs = new ArrayList<>();
			original.accept((position, style, codePoint) -> {
				glyphs.add(new Glyph(position, style, codePoint));
				return true;
			});

			int[] nameCodePoints = name.codePoints().toArray();
			boolean[] rainbow = new boolean[glyphs.size()];
			int[] letterIndex = new int[glyphs.size()];
			markName(glyphs, nameCodePoints, rainbow, letterIndex);

			float base = (System.currentTimeMillis() % CYCLE_MILLIS) / (float) CYCLE_MILLIS;
			for (int i = 0; i < glyphs.size(); i++) {
				Glyph glyph = glyphs.get(i);
				Style style = glyph.style();
				if (rainbow[i]) {
					float hue = base - letterIndex[i] * HUE_STEP_PER_LETTER;
					style = style.withColor(hsvToRgb(hue - (float) Math.floor(hue)));
				}
				if (!sink.accept(glyph.position(), style, glyph.codePoint())) {
					return false;
				}
			}
			return true;
		};
	}

	private static void markName(List<Glyph> glyphs, int[] name, boolean[] rainbow, int[] letterIndex) {
		int length = name.length;
		if (length == 0) {
			return;
		}
		for (int start = 0; start + length <= glyphs.size(); start++) {
			if (start > 0 && isNameChar(glyphs.get(start - 1).codePoint())) {
				continue;
			}
			if (start + length < glyphs.size() && isNameChar(glyphs.get(start + length).codePoint())) {
				continue;
			}
			boolean matches = true;
			for (int k = 0; k < length; k++) {
				int a = Character.toLowerCase(glyphs.get(start + k).codePoint());
				int b = Character.toLowerCase(name[k]);
				if (a != b) {
					matches = false;
					break;
				}
			}
			if (matches) {
				for (int k = 0; k < length; k++) {
					rainbow[start + k] = true;
					letterIndex[start + k] = k;
				}
				start += length - 1;
			}
		}
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
