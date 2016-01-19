import java.awt.Image;

import javax.swing.ImageIcon;


public class LazyHoming extends HomingMissile {
	
	static final double SPEED = 1.8;
	static final double MAX_TURN_ANGLE = Math.PI/345.0;
	
	double lastAngle;
	double difAngle;
	
	static final Image IMG_HOMING_LAZY = (new ImageIcon("src/homingLazy.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT);
	
	public LazyHoming(Game game, double x, double y, Unit owner, Tank target) {
		super(game, x, y, owner, target);
		speed = SPEED;
		//dx = this.target.xTarget - this.x;
		//dy = this.target.yTarget - this.y;
		lastAngle = angleTo(target) + Math.PI;
		if(lastAngle > THREE_HALVES_PI) lastAngle -= TWICE_PI;
		imgHoming = IMG_HOMING_LAZY;
	}
	
	public LazyHoming(Game game, double x, double y, int w, int h, Unit owner, Tank target) {
		super(game, x, y, w, h, owner, target);
		speed = SPEED;
		//dx = this.target.xTarget - this.x;
		//dy = this.target.yTarget - this.y;
		lastAngle = angleTo(target);
	}
	
	@Override
	public void move() {
		if(isFlagOn(REMOVING)) {
			timeRemoving++;
			if(timeRemoving == DEFAULT_TIME_TO_REMOVE) {
				setFlagOff(REMOVING);
				remove();
			}
			return;
		} else if(movingStraight) {
			moveStraight();
			return;
		}
		
		if(target != null && target.isFlagOn(HITTABLE) && targetIsEnemy()) {
			angleToTarget();
			angle = newAngle(lastAngle, angle, MAX_TURN_ANGLE);
			
			x += (speedX = speed*cos(angle));
			y += (speedY = speed*sin(angle));
			
			lastAngle = angle;
			
			if(hitTarget()) {
				hitLetal();
			}
		} else movingStraight = true;
	}

}
