package xtvapps.simusplayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import fts.android.AndroidUtils;
import fts.android.FtsActivity;
import fts.core.Log;
import fts.core.Utils;
import fts.core.Window;
import xtvapps.simusplayer.core.ModPlayer;
import xtvapps.simusplayer.core.ModPlayer.ModInfo;
import xtvapps.simusplayer.core.lcd.LcdScreenWidget;
import xtvapps.simusplayer.core.lcd.LcdSegmentWidget;
import xtvapps.simusplayer.core.widgets.WaveContainer;

public class MainActivity extends FtsActivity {
	private static final String LOGTAG = MainActivity.class.getSimpleName();

	private WaveContainer waveContainer;
	private LcdSegmentWidget lcdTime;
	private LcdSegmentWidget lcdLength;
	private LcdSegmentWidget lcdPosition;
	private LcdSegmentWidget lcdTempo;

	private static ModPlayer modPlayer;
	private static ModInfo modInfo;

	private static List<String> songs = new ArrayList<String>();
	private static int currentSong = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		try {
			songs.add(new File(getFilesDir(), "test/elimination.mod").getCanonicalPath());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		Window window = getFtsWindow();
		
		waveContainer = (WaveContainer)window.findWidget("waves");
		
		lcdTime     = (LcdSegmentWidget)window.findWidget("lcdTime");
		lcdLength   = (LcdSegmentWidget)window.findWidget("lcdLength");
		lcdPosition = (LcdSegmentWidget)window.findWidget("lcdPosition");
		lcdTempo    = (LcdSegmentWidget)window.findWidget("lcdTempo");
		
		try {
			AndroidUtils.unpackAssets(this, "test", getFilesDir());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		modPlayer = new ModPlayer(new AndroidWaveDevice(44100, 1024));
		modPlayer.setModPlayerListener(new ModPlayer.ModPlayerListener() {
			
			@Override
			public void onStart() {
				modInfo = modPlayer.getModInfo();
				System.out.println("name: " + modInfo.modName);
				System.out.println("format: " + modInfo.modFormat);
				System.out.println("tracks: " + modInfo.tracks);
				System.out.println("patterns: " + modInfo.patterns);
				System.out.println("samples: " + modInfo.samples);
				System.out.println("speed: " + modInfo.speed);
				System.out.println("bpm: " + modInfo.bpm);
				for(int i=0; i<modInfo.samples; i++) {
					System.out.println(String.format("%02X : %s", i, modPlayer.xmpGetSampleName(i)));
				}
				
				/*
				String modName = Utils.isEmptyString(modInfo.modName) ? "" : modInfo.modName;
				
				LcdSegmentWidget lcdModName = (LcdSegmentWidget)rootView.findWidget("lcdModName");
				lcdModName.setText(toFirstLetterUppercase(modName));
				waveContainer.setWaves(modInfo.tracks);
				*/
			}
			
			@Override
			public void onEnd() {
				Log.d(LOGTAG, "player ends");
			}
		});
	}

	@Override
	protected String getRootLayout() {
		return "modplayer";
	}
	
	private static void play() {
		play(songs.get(currentSong));
	}
	
	private static void play(final String fileName) {
		Thread t = new Thread() {
			public void run() {
				try {
					modPlayer.play(fileName);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};

		t.start();
	}
	
	private static void playPrev() {
		modPlayer.stop();
		modPlayer.waitForStop();
		currentSong--;
		if (currentSong<0) currentSong = songs.size()-1;
		
		play();
	}

	private static void playNext() {
		modPlayer.stop();
		modPlayer.waitForStop();
		currentSong++;
		if (currentSong>=songs.size()) currentSong = 0;
		
		play();
	}

	@Override
	protected void onStart() {
		super.onStart();
		play();
	}

	@Override
	protected void onStop() {
		super.onStop();
		modPlayer.stop();
		modPlayer.waitForStop();
	}
	
	
}
