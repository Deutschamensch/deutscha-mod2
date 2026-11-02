package de.deutscha.mod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ServerData;

public final class PauseButtons {
	public static final int SIZE = 20;
	private static final int GREEN = 0xFF30E060;
	private static final int RED = 0xFFE03030;
	private static final int ICON_SIZE = 2;

	private static final String[] PACK_ICON = {
		"########",
		"#......#",
		"########",
		"#..##..#",
		"#..##..#",
		"#......#",
		"#......#",
		"########"
	};
	private static final String[] NOTE_ICON = {
		"...#####",
		"...#...#",
		"...#...#",
		"...#...#",
		"...#...#",
		".###.###",
		"####.###",
		".##..##."
	};

	private static boolean serverPackEnabled = true;

	private PauseButtons() {
	}

	public static int serverPackX(int screenWidth) {
		return screenWidth - 2 * SIZE - 12;
	}

	public static int musicX(int screenWidth) {
		return screenWidth - SIZE - 6;
	}

	public static int y(int screenHeight) {
		return screenHeight - SIZE - 6;
	}

	public static void toggleServerPack() {
		Minecraft minecraft = Minecraft.getInstance();
		ServerData server = minecraft.getCurrentServer();
		if (serverPackEnabled) {
			serverPackEnabled = false;
			if (server != null) {
				server.setResourcePackStatus(ServerData.ServerPackStatus.DISABLED);
			}
			minecraft.getDownloadedPackSource().popAll();
			minecraft.reloadResourcePacks();
		} else {
			serverPackEnabled = true;
			if (server != null) {
				server.setResourcePackStatus(ServerData.ServerPackStatus.ENABLED);
			}
		}
	}

	public static void draw(GuiGraphicsExtractor graphics, int screenWidth, int screenHeight) {
		drawButton(graphics, serverPackX(screenWidth), y(screenHeight), PACK_ICON, serverPackEnabled);
		drawButton(graphics, musicX(screenWidth), y(screenHeight), NOTE_ICON, MusicPlayer.isPlaying());
	}

	private static void drawButton(GuiGraphicsExtractor graphics, int x, int y, String[] icon, boolean active) {
		int color = active ? GREEN : RED;
		graphics.fill(x - 1, y - 1, x + SIZE + 1, y, color);
		graphics.fill(x - 1, y + SIZE, x + SIZE + 1, y + SIZE + 1, color);
		graphics.fill(x - 1, y, x, y + SIZE, color);
		graphics.fill(x + SIZE, y, x + SIZE + 1, y + SIZE, color);

		int offset = (SIZE - icon.length * ICON_SIZE) / 2;
		for (int row = 0; row < icon.length; row++) {
			for (int column = 0; column < icon[row].length(); column++) {
				if (icon[row].charAt(column) == '#') {
					int px = x + offset + column * ICON_SIZE;
					int py = y + offset + row * ICON_SIZE;
					graphics.fill(px, py, px + ICON_SIZE, py + ICON_SIZE, 0xFFFFFFFF);
				}
			}
		}
	}
}
