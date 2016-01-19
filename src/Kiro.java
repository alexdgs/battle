import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;

import javax.swing.ImageIcon;


public class Kiro extends LazyHoming {
	
	static final int WIDTH = 20;
	static final int HEIGHT = 14;
	static final int TIME_LIVE = 1200;
	static final double SPEED = 0.8;
	static final double MAX_TURN_ANGLE = Math.PI/90.0;
	
	int timeLiving;
	
	static final Image[] IMG_KIRO = {
		(new ImageIcon("src/kiro1.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT),
		(new ImageIcon("src/kiro2.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT)
	};

	public Kiro(Game game, double x, double y, Unit owner, Tank target) {
		super(game, x, y, WIDTH, HEIGHT, owner, target);
		timeLiving = 0;
		speed = SPEED;
	}
	
	public void move() {		
		if(target != null && target.isFlagOn(HITTABLE) && targetIsEnemy()) {
			angleToTarget();
			angle = newAngle(lastAngle, angle, MAX_TURN_ANGLE);
			
			x += (speedX = speed*cos(angle));
			y += (speedY = speed*sin(angle));
			
			lastAngle = angle;
			
			if(hitTarget()) {
				hitLetal();
				target = getClosestTarget();
			}
		} else target = getClosestTarget();
		
		if(target == null) game.objectsToRemove.add(this);
		
		timeLiving++;
		if(timeLiving == TIME_LIVE) game.objectsToRemove.add(this);
	}
	
	@Override
	public void paint(Graphics2D g) {
		AffineTransform backup = g.getTransform();
		g.rotate(angle, x + halfWidth, y + halfHeight);
		if(game.isFlagOn(Game.BLINK_STATE)) g.drawImage(IMG_KIRO[0], (int)x, (int)y, null);
		else g.drawImage(IMG_KIRO[1], (int)x, (int)y, null);
		g.setTransform(backup);
	}
}
