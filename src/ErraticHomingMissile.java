import java.awt.Image;

import javax.swing.ImageIcon;


public class ErraticHomingMissile extends LazyHoming {
	
	static final double SPEED = 1.6;
	static final double MAX_TURN_ANGLE = Math.PI/60.0;
	static final int TIME_ERRATIC = 110;
	static final int MIN_TIME_SINGLE_ERRATIC_TURN = 5;
	static final int ADD_TIME_SINGLE_ERRATIC_TURN = 35;
	
	boolean movingErratic = true;
	int time = 0;
	int timeSingleTurn = 0;
	int maxTimeSingleTurn = 0;
	double turnAngle = MAX_TURN_ANGLE;
	
	static final Image IMG_ERRATIC_HOMING = (new ImageIcon("src/erraticHoming.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT);
	
	public ErraticHomingMissile(Game game, double x, double y, Unit owner, Tank target) {
		super(game, x, y, owner, target);
		//removable = false;
		speed = SPEED;
		imgHoming = IMG_ERRATIC_HOMING;
	}
	
	@Override
	public void move() {
		if(isFlagOn(REMOVING)) {
			timeRemoving++;
			//System.out.println(timeRemoving);
			if(timeRemoving == DEFAULT_TIME_TO_REMOVE) {
				setFlagOff(REMOVING);
				remove();
			}
			return;
		} else if(movingErratic) {
			moveErratic();
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
				hit();
			}
		} else movingStraight = true;
	}
	
	public void moveErratic() {
		if(timeSingleTurn == maxTimeSingleTurn) {
			//System.out.println("Changing angle dir");
			turnAngle *= -1.0;
			timeSingleTurn = 0;
			maxTimeSingleTurn = (int)(Math.random()*ADD_TIME_SINGLE_ERRATIC_TURN) + MIN_TIME_SINGLE_ERRATIC_TURN;
		} else {
			timeSingleTurn++;
		}
		
		angle = sumAngles(lastAngle, turnAngle);
		//System.out.println(Math.toDegrees(angle));
		x += (speedX = speed*cos(angle));
		y += (speedY = speed*sin(angle));
		//System.out.println(x + " " + y);
		lastAngle = angle;
		
		if(time == TIME_ERRATIC) {
			//System.out.println("Erratic false");
			movingErratic = false;
		}
		else time++;
	}
}
