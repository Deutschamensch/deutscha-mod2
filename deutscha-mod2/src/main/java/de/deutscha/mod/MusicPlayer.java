package de.deutscha.mod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public final class MusicPlayer {
	private static final SoundEvent TRACK = SoundEvent.createVariableRangeEvent(
		Identifier.fromNamespaceAndPath("deutschamod", "little_adventures")
	);

	private static LoopingTrack current;

	private MusicPlayer() {
	}

	public static boolean isPlaying() {
		return current != null && Minecraft.getInstance().getSoundManager().isActive(current);
	}

	public static void toggle() {
		SoundManager soundManager = Minecraft.getInstance().getSoundManager();
		if (isPlaying()) {
			soundManager.stop(current);
			current = null;
			return;
		}
		current = new LoopingTrack();
		soundManager.play(current);
	}

	private static final class LoopingTrack extends AbstractSoundInstance {
		private LoopingTrack() {
			super(TRACK, SoundSource.MUSIC, RandomSource.create());
			this.looping = true;
			this.relative = true;
			this.attenuation = SoundInstance.Attenuation.NONE;
			this.volume = 0.6F;
		}
	}
}
