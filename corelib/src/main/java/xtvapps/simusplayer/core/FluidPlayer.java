package xtvapps.simusplayer.core;

import java.io.File;
import java.io.IOException;

import xtvapps.simusplayer.core.audio.AudioRenderer;

public class FluidPlayer extends MediaPlayer {
	private static final String LOGTAG = FluidPlayer.class.getSimpleName();
	private File soundFontFile;

	static {
		System.loadLibrary("simusplayer-corelib");
	}
	private FluidMidiThread midiThread;

	public FluidPlayer(WaveDevice waveDevice) {
		super(waveDevice);
	}
	
	public void setSoundFont(File soundFontFile) {
		boolean wasPlaying = isPlaying;
		if (isPlaying) {
			shutdown();
			waitForStop();
		}
		this.soundFontFile = soundFontFile;
		if (wasPlaying) {
			restart();
		}
	}

	@Override
	public void onInit() {
	}
	
	@Override
	public void onRelease() {
	}
	
	@Override
	public AudioRenderer onPrepare(File songFile) {
		fluidInit(waveDevice.freq);
		fluidLoadSoundFontFile(soundFontFile.getAbsolutePath());

		AudioRenderer audioRenderer = new AudioRenderer() {

			@Override
			public void fillBuffer(byte[] buffer) {
				fluidFillBuffer(buffer);
				hasEnded = midiThread.hasEnded();
			}
		};
		try {
			midiThread = new FluidMidiThread(songFile.getAbsolutePath());
			midiThread.start();
			return audioRenderer;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void onFinish() {
		midiThread.shutdown();
		try {
			midiThread.join();
		} catch (InterruptedException e) {}
		
		fluidRelease();

		midiThread = null;
	}

	@Override
	public void setPause(boolean pause) {
		if (midiThread!=null) midiThread.setPaused(pause);
		super.setPause(pause);
	}

	public native boolean fluidInit(int freq);
	public native void    fluidLoadSoundFontFile(String path);
	public native void    fluidRelease();
	public native int     fluidFillBuffer(byte[] buffer);
	
	public static native void fluidSendEventNote(int type, int channel, int note, int velocity);
	public static native void fluidSendEventController(int channel, int param, int value);
	public static native void fluidSendEventChange(int type, int channel, int value);
	public static native void fluidSendEventPitchBend(int channel, int value);
	public static native void fluidSendEventSysex(int sysex[]);

	@Override
	public void doForward() {
		// TODO Auto-generated method stub
	}

	@Override
	public void doRewind() {
		// TODO Auto-generated method stub
	}

	@Override
	public int[][] getNotes() {
		return midiThread!=null ? midiThread.getNotes() : null;
	}
	
}
