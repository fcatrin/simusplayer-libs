package xtvapps.simusplayer.core.widgets;

import fts.core.Widget;
import fts.core.Window;
import fts.events.PaintEvent;
import fts.graphics.Canvas;
import fts.graphics.Color;
import fts.graphics.Point;
import xtvapps.simusplayer.core.lcd.LcdChar;

public class WaveWidget extends Widget {

	int wave[];
	private Color waveLinesColor;
	
	public WaveWidget(Window window) {
		super(window);
		setClickable(true);
	}

	public void setWave(int wave[]) {
		this.wave = wave;
	}
	
	public void setWaveLinesColor(Color waveLinesColor) {
		this.waveLinesColor = waveLinesColor;
	}

	@Override
	public void redraw() {
	}

	@Override
	protected void onPaint(PaintEvent e) {
		Canvas canvas = e.canvas;
		if (background != null) {
			background.setBounds(bounds);
			background.draw(canvas);
		}

		if (wave == null) return;
		canvas.setColor(waveLinesColor);
		int px = bounds.x;
		int py = bounds.y + bounds.height / 2;
		
		float steps = (float)wave.length / bounds.width;
		
		for(int x=0; x<bounds.width && px < bounds.x + bounds.width; x++) {
			float sample = wave[(int)(x * steps)] / 65536f;
			int h = (int)(bounds.height * sample);
			canvas.drawFilledRect(px, py - h, LcdChar.pixel_size, h);
			px += LcdChar.pixel_size + LcdChar.pixel_spacing;
		}
	}

	@Override
	public Point getContentSize(int width, int height) {
		return new Point(0, 0);
	}

}
