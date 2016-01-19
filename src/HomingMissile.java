import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;

import javax.swing.ImageIcon;


public class HomingMissile extends TargetProjectile {
	
	static final int WIDTH = 10;
	static final int HEIGHT = 8;
	static final double SPEED = 1.0;
	
	static final double MAX_X = Game.MAX_X - WIDTH;
	static final double MAX_Y = Game.MAX_Y - WIDTH;
	
	double dx;
	double dy;
	
	static final Image IMG_HOMING = (new ImageIcon("src/homing.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT);
	Image imgHoming;
	
	public HomingMissile(Game game, double x, double y, Unit owner, Tank target) {
		super(game,x,y,WIDTH,HEIGHT,owner,target);
		speed = SPEED;
		maxX = MAX_X;
		maxY = MAX_Y;
		imgHoming = IMG_HOMING;
		
		hasRemoveEffect = true;
	}
	
	public HomingMissile(Game game, double x, double y, int w, int h, Unit owner, Tank target) {
		super(game,x,y,w,h,owner,target);
		this.speed = SPEED;
		maxX = MAX_X;
		maxY = MAX_Y;
		imgHoming = IMG_HOMING;
		
		hasRemoveEffect = true;
	}
	
	public void move() {
		if(isFlagOn(REMOVING)) {
			timeRemoving++;
			if(timeRemoving == DEFAULT_TIME_TO_REMOVE) {
				setFlagOff(REMOVING);
				remove();
			}
			return;
		} else if(movingStraight) {
			//System.out.println("homing moving straight");
			moveStraight();
			return;
		}
		
		if(target != null && target.isFlagOn(HITTABLE) && targetIsEnemy()) {
			angleToTarget();
			x += (speedX = speed*cos(angle));
			y += (speedY = speed*sin(angle));
			
			if(hitTarget()) {
				hitLetal();
			}
		} else {
			//System.out.println(speedX + " " + speedY);
			movingStraight = true;
		}
	}
	
	public void angleToTarget() {
		dx = target.xTarget-x;
		dy = target.yTarget-y;
		
		angle = Math.atan(dy/dx);
		if(dx < 0.0)
			angle = angle + Math.PI;
	}
	
	public void update(Tank target) {
		this.target = target;
	}
	
	public void paint(Graphics2D g) {
		if(isFlagOn(REMOVING)) {
			g.drawImage(imgRem[remOrder[timeRemoving/TIME_TO_SHOW_FRAME_REMOVE]], (int)xImpact, (int)yImpact, null);
			return;
		}
		
		AffineTransform backup = g.getTransform();
		g.rotate(angle, x + halfWidth, y + halfHeight);
		g.drawImage(imgHoming, (int)x, (int)y, null);
		g.setTransform(backup);
	}
}
