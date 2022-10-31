package xtvapps.simusplayer.midi;

public interface MidiSequencer {
	public void reset();
	public void sendEvent(MidiEvent event);
	public void finish(long tick);
}
