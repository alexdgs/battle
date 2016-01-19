import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.ImageIcon;


public class Smoke implements Moveable {

	static final int WIDTH = 40;
	static final int HEIGHT = 40;
	static final int MAX_TIME = 120;
	static final int FRAME_TIME = 25;
	static final float MAX_ALPHA = 0.9f;
	
	boolean active = true;
	int x;
	int y;
	int time = 0;
	int timeFrame = 0;
	int frame = 0;
	float alpha;
	
	PlayerTank owner;
	
	static final Image[] IMG_SMOKE = {
			(new ImageIcon("src/smoke1.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_FAST),
			(new ImageIcon("src/smoke2.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_FAST),
			(new ImageIcon("src/smoke3.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_FAST),
			(new ImageIcon("src/smoke4.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_FAST)
	};
	
	static final int MAX_FRAME = IMG_SMOKE.length-1;
	
	public Smoke(PlayerTank owner, int x, int y) {
		this.owner = owner;
		this.x = x;
		this.y = y;
	}

	@Override
	public void move() {
		if(time == MAX_TIME) {
			active = false;
			return;
		} else time++;
		
		if(timeFrame == FRAME_TIME) {
			 if(frame < MAX_FRAME) frame++;
			 timeFrame = 0;
		} else timeFrame++;
		
		alpha = MAX_ALPHA*(1.0f-(time/(float)MAX_TIME));
	}
	
	public void paint(Graphics2D g) {
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		g.drawImage(IMG_SMOKE[frame], x, y, null);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
	}
}
