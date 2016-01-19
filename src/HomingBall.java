import java.awt.Color;
import java.awt.Graphics2D;


public class HomingBall extends TargetProjectile {
	
	static final double MAX_POSITIVE_SPEED = 2.0;
	static final double MAX_NEGATIVE_SPEED = -2.0;
	static final double ACCELERATION = 0.02;
	
	static final int DIAMETER = 6;
	static final int DRAWABLE_DIAMETER = (int)(Math.sqrt(2*DIAMETER*DIAMETER));
	
	static final double MAX_X = Game.MAX_X - DIAMETER;
	static final double MAX_Y = Game.MAX_Y - DIAMETER;

	public HomingBall(Game game, double x, double y, Unit owner, Tank target) {
		super(game, x, y, DIAMETER, DIAMETER, owner, target);
	}
	
	public HomingBall(Game game, double x, double y, Unit owner, Tank target, double angle) {
		super(game, x, y, DIAMETER, DIAMETER, owner, target);
		this.angle = angle;
		maxX = MAX_X;
		maxY = MAX_Y;
		setSpeeds(this.angle);
		
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
			moveStraight();
			return;
		}
		
		if(target != null && target.isFlagOn(HITTABLE) && targetIsEnemy()) {
			double dx = x - (target.xTarget - halfWidth);
			double dy = y - (target.yTarget - halfHeight);
			
			if(dx > 0.0) speedX = Math.max(speedX - ACCELERATION, MAX_NEGATIVE_SPEED);
			else if(dx < 0.0) speedX = Math.min(speedX + ACCELERATION, MAX_POSITIVE_SPEED);
			
			if(dy > 0.0) speedY = Math.max(speedY - ACCELERATION, MAX_NEGATIVE_SPEED);
			else if(dy < 0.0) speedY = Math.min(speedY + ACCELERATION, MAX_POSITIVE_SPEED);
			
			x += speedX;
			y += speedY;
			
			if(hitTarget()) {
				doHit();
				setFlagOn(REMOVING);
			}
		} else movingStraight = true;
	}
	
	public void doHit() {
		target.hitted(this, owner);
	}
	
	public void paint(Graphics2D g) {
		if(isFlagOn(REMOVING)) {
			g.drawImage(imgRem[remOrder[timeRemoving/TIME_TO_SHOW_FRAME_REMOVE]], (int)xImpact, (int)yImpact, null);
			return;
		}
		
		g.setColor(Color.GREEN);
		g.fillOval((int)x, (int)y, DRAWABLE_DIAMETER, DRAWABLE_DIAMETER);
	}
}
