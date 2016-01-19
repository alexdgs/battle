import java.awt.Color;
import java.awt.Graphics2D;


public class StraightBall extends Projectile {
	
	static final double SPEED = 4.0;
	static final int DIAMETER = 5;
	static final int DRAWABLE_DIAMETER = (int)(Math.sqrt(2*DIAMETER*DIAMETER));
	
	static final double MAX_X = Game.MAX_X - DIAMETER;
	static final double MAX_Y = Game.MAX_Y - DIAMETER;
	
	Color color = Color.WHITE;
	
	public StraightBall(Game game, double x, double y, double angle, Unit owner) {
		super(game, x, y, DIAMETER, DIAMETER, owner);
		speed = SPEED;
		maxX = MAX_X;
		maxY = MAX_Y;
		setSpeeds(angle);
		hasRemoveEffect = true;
	}
	
	public StraightBall(Game game, double x, double y, double angle, Unit owner, double speed) {
		this(game, x, y, angle, owner);
		this.speed = speed;
		setSpeeds(angle);
		color = Color.RED;
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
		}
		moveStraight();
	}
	
	@Override
	public void paint(Graphics2D g) {
		if(isFlagOn(REMOVING)) {
			g.drawImage(imgRem[remOrder[timeRemoving/TIME_TO_SHOW_FRAME_REMOVE]], (int)xImpact, (int)yImpact, null);
			return;
		}
		g.setColor(color);
		g.fillOval((int)x, (int)y, DRAWABLE_DIAMETER, DRAWABLE_DIAMETER);
	}
}
