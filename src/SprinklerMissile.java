import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;

import javax.swing.ImageIcon;


public class SprinklerMissile extends TargetProjectile {
	
	static final int WIDTH = 20;
	static final int HEIGHT = 10;
	
	static final int HALF_WIDTH = WIDTH/2;
	static final int HALF_HEIGHT = HEIGHT/2;
	
	static final int NUM_PROJECTILES = 5;
	static final int TIME_TO_START_LAUNCH = 90;
	static final int TIME_TO_LAUNCH_NEW = 40;
	static final double ACCELERATION = 0.005;
	
	double accelX;
	double accelY;
	int projectiles;
	int time;
	int gap;
	
	static final Image IMG_SPRINKLER = (new ImageIcon("src/sprinkler.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT);

	public SprinklerMissile(Game game, double x, double y, Unit owner, Tank target) {
		super(game, x, y, WIDTH, HEIGHT, owner, target);
		setAcceleration();
		speedX /= 10.0;
		speedY /= 10.0;
		time = 0;
		gap = 0;
	}
	
	public void setAcceleration() {
		accelX = ACCELERATION*cos(angle);
		accelY = ACCELERATION*sin(angle);
	}

	@Override
	public void move() {
		x += speedX;
		y += speedY;
		if(time >= TIME_TO_START_LAUNCH) {
			gap++;
			if(gap == TIME_TO_LAUNCH_NEW) {
				if(!target.isFlagOn(Unit.HITTABLE)) {
					target = getClosestTarget();
					if(target == null) {
						game.objectsToRemove.add(this);
						return;
					}
				}
				game.objectsToInsert.add(new HomingBall(game, x, y, owner, target, angle));
				projectiles++;
				gap = 0;
				if(projectiles == NUM_PROJECTILES) game.objectsToRemove.add(this);
			}
		}
		speedX += accelX;
		speedY += accelY;
		time++;
	}
	
	public void paint(Graphics2D g) {
		AffineTransform backup = g.getTransform();
		g.rotate(angle, x + HALF_WIDTH, y + HALF_HEIGHT);
		g.drawImage(IMG_SPRINKLER, (int)x, (int)y, null);
		g.setTransform(backup);
	}
}
