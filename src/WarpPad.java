import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.ImageIcon;


public class WarpPad extends GameObject {
	
	static final int WIDTH = 40;
	static final int HEIGHT = 40;

	double destX;
	double destY;
	
	boolean active = false;
	
	static final Image IMG_WARP_UNACTIVE = (new ImageIcon("src/warp_unactive.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT);
	
	static final Image[] IMG_WARP = {
		(new ImageIcon("src/warp1.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT),
		(new ImageIcon("src/warp2.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT)
	};
	
	public WarpPad(Game game, double x, double y) {
		super(game, x, y, WIDTH, HEIGHT);
	}
	
	public WarpPad(Game game, double x, double y, double x2, double y2) {
		super(game, x, y, WIDTH, HEIGHT);
		destX = x2;
		destY = y2;
	}
	
	public void setDestiny(double x, double y) {
		destX = x;
		destY = y;
	}
	
	public void step() {
		active = false;
		for(Tank t : game.playerManager.tanks) {
			if(t.isFlagOn(Tank.CAN_MOVE) && getBounds().intersects(t.getBounds())) {
				((PlayerTank)t).warpPad = this;
				active = true;
			}
		}
	}
	
	public void warp(Unit u) {
		u.x = destX;
		u.y = destY;
		u.updateTargetPos();
	}
	
	public void paint(Graphics2D g) {
		if(active) {
			if(game.isFlagOn(Game.BLINK_STATE)) g.drawImage(IMG_WARP[0], (int)x, (int)y, null);
			else g.drawImage(IMG_WARP[1], (int)x, (int)y, null);
		} else g.drawImage(IMG_WARP_UNACTIVE, (int)x, (int)y, null);
	}
}
