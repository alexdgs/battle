import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;

import javax.swing.ImageIcon;


public class LaserTurret extends Turret {
	
	static final int AMPLITUDE = 6;
	static final int DEFAULT_TIME_TO_FIRE = 40;
	static final int MIN_TIME_TO_FIRE = 20;
	static final int SIGHT_RANGE_VERY_SHORT = 140;
	static final int TIME_DRAW = 20;
	static final int INIT_POWER = 30;
	
	boolean draw;
	int timeRemDraw;
	int power;
	Bar powerBar;
	static final int powerBarWidth = 4;
	static final Color color = Color.ORANGE;
	
	Image imgLaserTurret = (new ImageIcon("src/laser_turret.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_FAST);
	
	public LaserTurret(Unit owner) {
		super(owner);
		sight = SIGHT_RANGE_VERY_SHORT;
		draw = false;
		timeRemDraw = 0;
		maxTimeToFire = DEFAULT_TIME_TO_FIRE;
		power = INIT_POWER;
		
		powerBar = new Bar(this, 0, 0, WIDTH, powerBarWidth, power, INIT_POWER, color, true, Bar.TYPE_POWER);
	}
	
	@Override
	public void move() {
		if(power == 0) {
			((Tank)owner).turret = null;
			game.objectsToRemove.add(this);
			return;
		}
		
		x = owner.x + adjX;
		y = owner.y + adjY;
		updateTargetPos();
		target = owner.selectClosestThreat(sight);
		
		if(target != null) maxTimeToFire = (target instanceof Tank ? DEFAULT_TIME_TO_FIRE : MIN_TIME_TO_FIRE);
		if(timeToFire >= maxTimeToFire) {
			if(target != null && Math.abs(lastAngle-angle) < MIN_AIM_ANGLE) {
				((Hittable)target).hitted(null, owner);
				timeToFire = 0;
				timeRemDraw = TIME_DRAW;
				draw = true;
				setPower(--power);
				
			}
		} else timeToFire++;
		
		if(draw) {
			timeRemDraw--;
			if(timeRemDraw == 0) draw = false;
		}
	}
	
	public void setPower(int power) {
		this.power = power;
		powerBar.update(power);
	}
	
	public void fire() {
		game.objectsToInsert.add(new StraightBall(game, x + DESPL_BALL_X, y + DESPL_BALL_Y, lastAngle, owner));
	}
	
	@Override
	public void paint(Graphics2D g) {
		g.drawImage(imgLaserTurret, (int)x, (int)y, null);
		
		if(draw && target != null) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)timeRemDraw/TIME_DRAW));
			g.setColor(color);
			Stroke backup = g.getStroke();
			g.setStroke(new BasicStroke(AMPLITUDE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
			g.drawLine((int)xTarget, (int)yTarget, (int)target.xTarget, (int)target.yTarget);
			g.setStroke(backup);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		}
		
		powerBar.paint(g);
	}
}
