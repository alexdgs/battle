import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;


public class BouncingBall extends StraightBall {
	
	static final double SPEED = 1.5;
	static final int DIAMETER = 7;
	static final int DRAWABLE_DIAMETER = (int)(Math.sqrt(2*DIAMETER*DIAMETER));
	static final int LIVE_TIME = 1500;
	static final int FADE_OUT_START_TIME = 1400;
	
	static final double MAX_X = Game.MAX_X - DIAMETER;
	static final double MAX_Y = Game.MAX_Y - DIAMETER;
	
	int time;

	public BouncingBall(Game game, double x, double y, double angle, Unit owner) {
		super(game, x, y, angle, owner);
		width = height = DIAMETER;
		speed = SPEED;
		setSpeeds(angle);
		time = 0;
	}
	
	@Override
	public void move() {
		if(!inBounds()) return;
		
		//moveStraight();
		
		x += speedX;
		y += speedY;
		
		for(Hittable h : game.hittables) {
			if(hit(h)) {
				if(h instanceof Tank){
					Tank t = (Tank)h;
					if(t.isFlagOn(HITTABLE) && t.team != owner.team && (owner.team == Unit.ENEMY_TEAM || t.team == Unit.ENEMY_TEAM)) {
						if(t.isFlagOn(Tank.VULNERABLE)) {
							t.hitLetal(null, owner);
						}
						remove();
						return;
					}
				}
			}
		}
		
		time++;
		if(time == LIVE_TIME) remove();
	}
	
	@Override
	public boolean inBounds() {
		if(x + speedX < Game.MIN_X || x + speedX > MAX_X) bounceX();
		if(y + speedY < Game.MIN_Y || y + speedY > MAX_Y) bounceY();
		return true;
	}
	
	public void bounceX() {
		speedX *= -1;
	}
	
	public void bounceY() {
		speedY *= -1;
	}
	
	@Override
	public void paint(Graphics2D g) {
		g.setColor(Color.PINK);
		if(time >= FADE_OUT_START_TIME)
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)((LIVE_TIME - time)/100.0)));
		g.fillOval((int)x, (int)y, DRAWABLE_DIAMETER, DRAWABLE_DIAMETER);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
	}
}
