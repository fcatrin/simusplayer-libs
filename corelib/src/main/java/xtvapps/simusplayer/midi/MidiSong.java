package xtvapps.simusplayer.midi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import fts.core.Log;
import xtvapps.simusplayer.midi.MidiEvent.EventType;

public class MidiSong {
	private static final String LOGTAG = MidiSong.class.getSimpleName();
	
	private static final int SYSEX_META_TRACK_NAME = 0x03;
	private static final String CHAR_ENCODING = "ASCII";
	
	List<MidiTrack> tracks;
	boolean useSmpteTiming;
	long tempo;
	long ppq;

	public List<MidiTrack> getTracks() {
		return tracks;
	}

	public long getTempo() {
		return tempo;
	}

	public long getPpq() {
		return ppq;
	}

	public static MidiSong load(SimpleStream is) throws IOException {
		long id = readId(is);
		if (id == makeId("MThd")) {
			return loadSmf(is);
		} else if (id == makeId("RIFF")) {
			return loadRiff(is);
		}
		throw new InvalidFormatException();
	}

	private static MidiSong loadRiff(SimpleStream is) throws IOException {
		/* skip file length */
		read32le(is);

		/* check file type ("RMID" = RIFF MIDI) */
		long id = readId(is);
		if (id != makeId("RMID")) {
			throw new IOException("Invalid format");
		}

		do {
			id = readId(is);
			long len = read32le(is);
			
			if (id == makeId("data")) break;
			is.skip(len+1);
		} while(true);
		
		id = readId(is);
		if (id != makeId("MThd")) {
			throw new IOException("Invalid format");
		}
		
		return loadSmf(is);
	}

	private static MidiSong loadSmf(SimpleStream is) throws IOException {
		long headerLen = readInt(is, 4);
		if (headerLen < 6) {
			throw new InvalidFormatException();
		}

		long type = readInt(is, 2);
		if (type != 0 && type != 1) {
			String msg = String.format("Type %d format is not supported", type);
			throw new InvalidFormatException(msg);
		}

		long nTracks = readInt(is, 2);
		if (nTracks < 1 || nTracks > 1000) {
			String msg = String.format("Invalid number of tracks: %d", nTracks);
			throw new InvalidFormatException(msg);
		}
		
		long timeDivision = readInt(is, 2);
		if (timeDivision < 0) {
			String msg = String.format("Invalid time division: %d", timeDivision);
			throw new InvalidFormatException(msg);
		}

		MidiSong song = new MidiSong();
		song.useSmpteTiming = (timeDivision & 0x8000) != 0;
		
		if (!song.useSmpteTiming) {
			/* time_division is ticks per quarter */
			song.tempo = 500000; /* default: 120 bpm */
			song.ppq   = timeDivision;
			Log.d(LOGTAG, String.format("smpt_timing off ppq %d", song.ppq));
		} else {
			/* upper byte is negative frames per second */
			int fps = (int)(0x80 - ((timeDivision >> 8) & 0x7f));
			/* lower byte is ticks per frame */
			timeDivision &= 0xff;
			/* now pretend that we have quarter-note based timing */
			switch (fps) {
			case 24:
				song.tempo = 500000;
				song.ppq   = 12 * timeDivision;
				break;
			case 25:
				song.tempo = 400000;
				song.ppq   = 10 * timeDivision;
				break;
			case 29: /* 30 drop-frame */
				song.tempo = 100000000;
				song.ppq   = 2997 * timeDivision;
				break;
			case 30:
				song.tempo = 500000;
				song.ppq   = 15 * timeDivision;
				break;
			default:
				Log.e(LOGTAG, String.format("Invalid number of SMPTE frames per second (%d)", fps));
			}
			Log.d(LOGTAG, String.format("smpt_timing on ppq %d tempo %d division %d", song.ppq, song.tempo, fps));
		}
		
		song.tracks = new ArrayList<MidiTrack>();
		long len = 0;
		for(int i = 0; i < nTracks; i++) {
			do {
				long id = readId(is);
				len = readInt(is, 4);
				if (len < 0 || len >= 0x10000000) {
					String msg = String.format("invalid chunk length %d", len);
					throw new InvalidFormatException(msg);
				}

				if (id == makeId("MTrk")) {
					break;
				}
				is.skip(len);
			} while (true);
			
			song.tracks.add(readTrack(is, song, is.getOffset() + len));
		}
		return song;
	}

	private static MidiTrack readTrack(SimpleStream is, MidiSong song, long trackEnd) throws IOException {
		MidiTrack track = new MidiTrack();
		int  port = 0;
		long tick = 0;
		
		int c, cmd, lastCmd = 0;
		int d0, d1, d2;
		
		while (is.getOffset() < trackEnd) {
			long deltaTicks = readVar(is);
			tick += deltaTicks;
			
			c = is.readByte();
			
			if ((c & 0x80) != 0) {
				/* have command */
				cmd = c;
				if (cmd < 0xf0)	lastCmd = cmd;
			} else {
				/* running status */
				is.unreadByte();
				cmd = lastCmd;
			}
			
			MidiEvent event = null;
			switch (cmd >> 4) {
			case 0x8: /* channel msg with 2 parameter bytes */
			case 0x9:
			case 0xa:
			case 0xb:
			case 0xe:
				event = new MidiEvent();
				event.setType(MidiEvent.eventMap[cmd >> 4]);
				event.setPort(port);
				event.setTick(tick);
				
				d0 = cmd & 0x0f;
				d1 = is.readByte() & 0x7f;
				d2 = is.readByte() & 0x7f;
				event.setData(new int[] {d0, d1, d2});
				break;
				
			case 0xc: /* channel msg with 1 parameter byte */
			case 0xd:
				event = new MidiEvent();
				event.setType(MidiEvent.eventMap[cmd >> 4]);
				event.setPort(port);
				event.setTick(tick);
				d0 = cmd & 0x0f;
				d1 = is.readByte() & 0x7f;
				event.setData(new int[] {d0, d1});
				break;
			case 0xf:
				switch (cmd) {
				case 0xf0: /* sysex */
				case 0xf7: /* continued sysex, or escaped commands */
					long len = readVar(is);

					event = new MidiEvent();
					event.setType(EventType.SYSEX);
					event.setPort(port);
					event.setTick(tick);
					
					int sysex[];
					if (cmd == 0xf0) {
						sysex = new int[(int)len+1];
						sysex[0] = 0xf0;
						c = 1;
					} else {
						sysex = new int[(int)len];
						c = 0;
					}
					for (; c < len; ++c)
						sysex[c] = is.readByte();
					event.setSysex(sysex);
					break;
				case 0xff: /* meta event */
					c = is.readByte();
					len = readVar(is);

					switch (c) {
					case SYSEX_META_TRACK_NAME:
						track.setName(readString(is, (int)len));
						break;
					case 0x21:	 /* port number */
						port = (int)is.readByte(); // Port Count is only available in the target system % port_count;
						is.skip(len-1);
						break;

					case 0x2f: /* end of track */
						track.setEndTick(tick);
						is.setOffset((int)trackEnd);
						return track;

					case 0x51: /* tempo */
						if (len < 3)
							throw new InvalidFormatException(String.format("Invalid tempo len %d", len));
						
						if (song.useSmpteTiming) {
							/* SMPTE timing doesn't change */
							is.skip(len);
						} else {
							event = new MidiEvent();
							event.setType(EventType.TEMPO);
							event.setPort(port);
							event.setTick(tick);
							
							long tempo = is.readByte() << 16;
							tempo |= is.readByte() << 8;
							tempo |= is.readByte();
							event.setTempo(tempo);

							is.skip(len - 3);
						}
						break;

					default: /* ignore all other meta events */
						is.skip(len);
						break;
					}
				}
			}
			if (event!=null) track.addEvent(event);
		}
		return track;
	}
	
	// binary level read functions
	
	private static long read32le(SimpleStream is) throws IOException {
		long value = is.readByte();
		value += is.readByte() << 8;
		value += is.readByte() << 16;
		value += is.readByte() << 24;
		return value;
	}
	
	private static long readId(SimpleStream is) throws IOException {
		return read32le(is);
	}
	
	private static long makeId(String id) {
		try {
			byte chars[] = id.getBytes(CHAR_ENCODING);
			long value = chars[0];
			value += chars[1] << 8;
			value += chars[2] << 16;
			value += chars[3] << 24;
			return value;
		} catch (UnsupportedEncodingException e) {
			// should never happen
			e.printStackTrace();
			return 0;
		}
	}
	
	private static long readInt(SimpleStream is, int size) throws IOException {
		long value = 0;
		do {
			int c = is.readByte();
			value = value << 8 | c;
		} while (--size > 0);
		return value;
	}
	
	public static long readVar(SimpleStream is) throws IOException {
		long value = 0;
		int i = 0;

		int c;
		do {
			c = is.readByte();
			value = (value << 7) | (c & 0x7f);
		} while ((c & 0x80) != 0 && i++ < 4);
		return value;
	}
	
	public static String readString(SimpleStream is, int len) throws IOException {
		byte buffer[] = new byte[len];
		for(int i=0; i<len; i++) {
			buffer[i] = (byte)is.readByte();
		}
		return new String(buffer, CHAR_ENCODING);
	}

}
