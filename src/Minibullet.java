import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;

import javax.swing.ImageIcon;


public class Minibullet extends Projectile {
	
	static final int WIDTH = 6;
	static final int HEIGHT = 6;
	
	static final int HALF_WIDTH = WIDTH/2;
	static final int HALF_HEIGHT = HEIGHT/2;
	
	static final double MAX_X = Game.MAX_X - WIDTH;
	static final double MAX_Y = Game.MAX_Y - HEIGHT;
	static final double SPEED = 5.0;
	
	double angle;
	
	static final Image imgMinibullet = (new ImageIcon("src/minibullet.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_FAST);
	
	public Minibullet(Game game, double x, double y, double angle, Unit owner) {
		super(game, x, y, WIDTH, HEIGHT, owner);
		this.angle = angle;
		speed = SPEED;
		maxX = MAX_X;
		maxY = MAX_Y;
		setSpeeds(angle);
		
		hasRemoveEffect = false;
	}
	
	@Override
	public void move() {
		moveStraight();
	}
	
	@Override
	public void doHit(Hittable h) {
		((Tank)h).hittedLittle(this, owner);
	}
	
	@Override
	public void paint(Graphics2D g) {
		AffineTransform backup = g.getTransform();
		g.rotate(angle, x + halfWidth, y + halfHeight);
		g.drawImage(imgMinibullet, (int)x, (int)y, null);
		g.setTransform(backup);
	}
}
