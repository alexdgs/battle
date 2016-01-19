import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;

import javax.swing.ImageIcon;


public class EagleProtector extends LazyHoming {
	
	static final int WIDTH = 15;
	static final int HEIGHT = 15;
	static final int HALF_WIDTH = WIDTH/2;
	static final int HALF_HEIGHT = HEIGHT/2;
	static final double SPEED = 0.75;
	static final double MAX_TURN_ANGLE = Math.PI/90.0;
	static final int BLINK_TIME = 20;
	
	Eagle eagle;
	int blinkTime;
	boolean blinkOn = false;
	boolean returning = false;
	
	static final Image[] IMG_EAGLE_PROTECTOR = {
		(new ImageIcon("src/eagleP1.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_FAST),
		(new ImageIcon("src/eagleP2.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_FAST)
	};

	public EagleProtector(Game game, double x, double y, double angle, Eagle owner, Tank target) {
		super(game, x, y, WIDTH, HEIGHT, owner, target);
		eagle = owner;
		speed = SPEED;
		this.lastAngle = angle;
		blinkTime = (int)(Math.random()*BLINK_TIME);
	}
	
	public void move() {
		blinkTime++;
		if(blinkTime == BLINK_TIME) {
			blinkOn = !blinkOn;
			blinkTime = 0;
		}
		
		for(Hittable h : game.hittables) {
			if(hit(h)) {
				if(h instanceof Bullet) {
					Bullet b = (Bullet)h;
					if(b.isFlagOn(HITTABLE) && b.owner.team == Unit.ENEMY_TEAM) {
						game.objectsToRemove.add(this);
						b.remove();
						return;
					}
				}
			}
		}
		
		if(returning) {
			returnToEagle();
			return;
		}
		
		if(target != null && target.isFlagOn(HITTABLE) && targetIsEnemy() && target.target != null && target.target instanceof Eagle) {
			angleToTarget();
			angle = newAngle(lastAngle, angle, MAX_TURN_ANGLE);
			
			x += speed*cos(angle);
			y += speed*sin(angle);
			
			lastAngle = angle;
			
			if(hitTarget()) {
				hit();
				game.objectsToRemove.add(this);
			}
		} else returning = true;
	}
	
	public void returnToEagle() {
		angle = angleTo(eagle);
		angle = newAngle(lastAngle, angle, MAX_TURN_ANGLE);
		
		x += speed*cos(angle);
		y += speed*sin(angle);
		
		lastAngle = angle;
		
		if(hit(eagle)) {
			game.objectsToRemove.add(this);
			eagle.numProtectors++;
			eagle.protectorBar.update(eagle.numProtectors);
			eagle.checkProtectorState();
		}
	}
	
	@Override
	public void paint(Graphics2D g) {
		AffineTransform backup = g.getTransform();
		g.rotate(angle, x + HALF_WIDTH, y + HALF_HEIGHT);
		if(blinkOn) g.drawImage(IMG_EAGLE_PROTECTOR[0], (int)x, (int)y, null);
		else g.drawImage(IMG_EAGLE_PROTECTOR[1], (int)x, (int)y, null);
		g.setTransform(backup);
	}
	
}
