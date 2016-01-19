import java.awt.Image;

import javax.swing.ImageIcon;


public abstract class Projectile extends Unit implements Moveable {
	
	static final int DEFAULT_TIME_TO_REMOVE = 12;
	static final int NUM_FRAMES = 3;
	static final int TIME_TO_SHOW_FRAME_REMOVE = DEFAULT_TIME_TO_REMOVE/NUM_FRAMES;
	
	// Flags for Projectile
	static final int CAN_MOVE = 2;
	static final int STRENGTH = 3;
	
	static final int REMOVE_WIDTH = 40;
	static final int REMOVE_HEIGHT = 40;
	static final int HALF_REMOVE_WIDTH = REMOVE_WIDTH/2;
	static final int HALF_REMOVE_HEIGHT =  REMOVE_HEIGHT/2;
	
	static final int RANDOM_ANGLE = Integer.MAX_VALUE;
	
	Unit owner;
	int flags;
	int dir;
	
	double speed;
	double speedX;
	double speedY;
	double maxX;
	double maxY;
	
	int xImpact;
	int yImpact;
	int timeRemoving;
	
	boolean hasRemoveEffect;
	boolean movingStraight = false;
	
	static final Image[] imgRem = {
		(new ImageIcon("src/bullet_hit1.png")).getImage().getScaledInstance(REMOVE_WIDTH, REMOVE_HEIGHT, Image.SCALE_DEFAULT),
		(new ImageIcon("src/bullet_hit2.png")).getImage().getScaledInstance(REMOVE_WIDTH, REMOVE_HEIGHT, Image.SCALE_DEFAULT),
		(new ImageIcon("src/bullet_hit3.png")).getImage().getScaledInstance(REMOVE_WIDTH, REMOVE_HEIGHT, Image.SCALE_DEFAULT),
	};
	
	static final int[] remOrder = {0,1,2,0};
	
	public Projectile(Game game, double x, double y, int w, int h, Unit owner) {
		super(game, x, y, w, h, 0, owner.team);
		this.owner = owner;
		timeRemoving = 0;
	}
	
	public void moveStraight() {
		if(!inBounds()) return;
		
		x += speedX;
		y += speedY;
		
		for(Hittable h : game.hittables) {
			if(hit(h)) {
				if(h instanceof Tank){
					Unit t = (Unit)h;
					if(t.isFlagOn(HITTABLE) && t.team != owner.team && (owner.team == Unit.ENEMY_TEAM || t.team == Unit.ENEMY_TEAM)) {
						if(t.isFlagOn(Tank.VULNERABLE)) {
							doHit(h);
							if(hasRemoveEffect) setFlagOn(REMOVING);
						}
						remove();
						return;
					}
				} else if(h instanceof Projectile) {
					Projectile p = (Projectile)h;
					if(p.isFlagOn(HITTABLE) && p.owner.id != owner.id && p.owner.team != owner.team &&
							(p.owner.team == Unit.ENEMY_TEAM || owner.team == Unit.ENEMY_TEAM))
					{
						remove();
						p.remove();
						return;
					}
				} else {
					if(!afterHitGameObject(h)) return;
				}
			}
		}
	}
	
	public boolean afterHitGameObject(Hittable h) {
		GameObject go = (GameObject)h;
		if(go.isFlagOn(HITTABLE) && !(go.isFlagOn(ALLY_INMUNE) && owner.team == Unit.ALLY_TEAM)) {
			remove();
			h.hitted(this,owner);
			if(h instanceof Eagle) return false;
		}
		return true;
	}
	
	public boolean inBounds() {
		if(x + speedX < Game.MIN_X || x + speedX > maxX) {
			if(hasRemoveEffect) setFlagOn(REMOVING);
			remove();
			return false;
		}
		
		if(y + speedY < Game.MIN_Y || y + speedY > maxY) {
			if(hasRemoveEffect) setFlagOn(REMOVING);
			remove();
			return false;
		}
		return true;
	}
	
	public void setSpeeds(double angle) {
		if(angle == RANDOM_ANGLE) angle = randomAngle();
		
		speedX = speed * cos(angle);
		speedY = speed * sin(angle);
		//System.out.println(Math.cos(angle) + " " + Math.sin(angle));
	}
	
	public void remove() {
		if(!isFlagOn(REMOVING) || !hasRemoveEffect) game.objectsToRemove.add(this);
		else setRemovingCoordinates();
	}
	
	public boolean hit(Hittable h) {
		return h.getBounds().intersects(getBounds());
	}
	
	public void doHit(Hittable h) {
		h.hitted(null, owner);
	}
	
	public void setRemovingCoordinates() {
		xImpact = (int)x + halfWidth - HALF_REMOVE_WIDTH;
		yImpact = (int)y + halfHeight - HALF_REMOVE_HEIGHT;
	}
}
