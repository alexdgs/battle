import java.awt.Color;
import java.awt.Graphics2D;


public class BigHomingBall extends HomingBall {
	
	static final double MAX_POSITIVE_SPEED = 1.5;
	static final double MAX_NEGATIVE_SPEED = -1.5;
	static final double ACCELERATION = 0.015;
	
	static final int DIAMETER = 7;
	static final int DRAWABLE_DIAMETER = (int)(Math.sqrt(2*DIAMETER*DIAMETER));
	static final double MAX_X = Game.MAX_X - DIAMETER;
	static final double MAX_Y = Game.MAX_Y - DIAMETER;

	public BigHomingBall(Game game, double x, double y, Unit owner, Tank target) {
		super(game, x, y, owner, target);
		maxX = MAX_X;
		maxY = MAX_Y;
	}
	
	@Override
	public void doHit() {
		target.hitLetal(this, owner);
	}
	
	@Override
	public void paint(Graphics2D g) {
		if(isFlagOn(REMOVING)) {
			g.drawImage(imgRem[remOrder[timeRemoving/TIME_TO_SHOW_FRAME_REMOVE]], (int)xImpact, (int)yImpact, null);
			return;
		}
		
		g.setColor(Color.MAGENTA);
		g.fillOval((int)x, (int)y, DRAWABLE_DIAMETER, DRAWABLE_DIAMETER);
	}

}
