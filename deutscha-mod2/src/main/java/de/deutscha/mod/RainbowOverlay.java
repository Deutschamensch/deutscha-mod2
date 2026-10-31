package de.deutscha.mod;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class RainbowOverlay {
	private static final int ALPHA = 0x3A000000;
	private static final int STRIP_WIDTH = 3;
	private static final long CYCLE_MILLIS = 4000L;

	private static boolean enabled;

	private RainbowOverlay() {
	}

	public static void toggle() {
		enabled = !enabled;
	}

	public static void draw(GuiGraphicsExtractor graphics) {
		if (!enabled) {
			return;
		}
		int width = graphics.guiWidth();
		int height = graphics.guiHeight();
		float base = (System.currentTimeMillis() % CYCLE_MILLIS) / (float) CYCLE_MILLIS;
		for (int x = 0; x < width; x += STRIP_WIDTH) {
			float hue = base + x / (float) width;
			hue -= (float) Math.floor(hue);
			graphics.fill(x, 0, Math.min(x + STRIP_WIDTH, width), height, ALPHA | RainbowText.hsvToRgb(hue));
		}
	}
}
