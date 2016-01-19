import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.util.ArrayList;

import javax.swing.ImageIcon;


public class Laser extends Projectile {
	
	static final int AMPLITUDE = 8;
	static final int IMPACT_RADIUS = 30;
	
	static final int HALF_AMPLITUDE = AMPLITUDE/2;
	static final int HALF_IMPACT_RADIUS = IMPACT_RADIUS/2;
	
	static final int LIVE_TIME = 50;
	
	int originX;
	int originY;
	int impactX;
	int impactY;
	double impactHaloX;
	double impactHaloY;
	int timeLiving;
	
	Image imgImpact = (new ImageIcon("src/laserImpact.png")).getImage().getScaledInstance(IMPACT_RADIUS, IMPACT_RADIUS, Image.SCALE_DEFAULT);
	
	public Laser(Game game, Unit owner, int dir, double x, double y, int w, int h, double xo, double yo) {
		super(game, x, y, w, h, owner);
		this.dir = dir;
		originX = (int)xo;
		originY = (int)yo;
		
		if(this.dir == LEFT || this.dir == RIGHT) {
			impactY = originY;
			if(this.dir == LEFT) impactX = Game.MIN_X;
			else impactX = Game.MAX_X;
		} else {
			impactX = originX;
			if(this.dir == UP) impactY = Game.MIN_Y;
			else impactY = Game.MAX_Y;
		}
		setImpactHaloPos();
		timeLiving = 0;
	}
	
	@Override
	public void move() {
		if(timeLiving == 0) {
			ArrayList<Hittable> targets = new ArrayList<Hittable>();
			boolean containsTank = false;
			double minDistToImpactPoint = Double.POSITIVE_INFINITY;
			
			for(Hittable h : game.hittables) {
				if(hit(h)) {
					GameObject go = (GameObject) h;
					double distToImpactPoint = 0;
					switch(dir) {
					case LEFT:
						distToImpactPoint = originX - (go.x + go.width);
						break;
					case RIGHT:
						distToImpactPoint = go.x - originX;
						break;
					case UP:
						distToImpactPoint = originY - (go.y + go.height);
						break;
					case DOWN:
						distToImpactPoint = go.y - originY;
					}
					
					if(distToImpactPoint <= minDistToImpactPoint) {
						
						if(h instanceof Bullet) {
							Bullet b = (Bullet)h;
							if(b.isFlagOn(HITTABLE) && b.owner.id != owner.id && b.owner.team != owner.team &&
									(b.owner.team == Unit.ENEMY_TEAM || owner.team == Unit.ENEMY_TEAM))
								{
									targets.add(h);
								}
						} else if(h instanceof Tank){
							Unit t = (Unit)h;
							if(t.isFlagOn(HITTABLE) && t.team != owner.team && (owner.team == Unit.ENEMY_TEAM || t.team == Unit.ENEMY_TEAM)) {
								if(t.isFlagOn(Tank.VULNERABLE)) {
									
									if(distToImpactPoint < minDistToImpactPoint) {
										targets.clear();
										minDistToImpactPoint = distToImpactPoint;
										containsTank = false;
									}
									
									if(!containsTank) {
										targets.add(h);
										containsTank = true;
									}
								}
							}
						} else {
							if(((GameObject)h).isFlagOn(HITTABLE)) {
								if(distToImpactPoint < minDistToImpactPoint) {
									targets.clear();
									minDistToImpactPoint = distToImpactPoint;
									containsTank = false;
								}
								
								targets.add(h);
							}
						}
					}
				}
			}
			
			if(targets.size() > 0) {
				int dist = (int)minDistToImpactPoint;
				switch(dir) {
				case LEFT:
					x = impactX = originX - dist;
					//width = originX - impactX;
					break;
				case RIGHT:
					impactX = originX + dist;
					//width = impactX - originX;
					break;
				case UP:
					impactY = originY - dist;
					break;
				case DOWN:
					impactY = originY + dist;
				}
				
				for(Hittable h : targets) {
					if(h instanceof Bullet) {
						((Bullet)h).remove();
					} else h.hitted(this, owner);
				}
				
				setImpactHaloPos();
			}
			
			/*if(target != null) {
				int dist = (int)minDistToImpactPoint;
				switch(dir) {
				case LEFT:
					impactX = originX - dist;
					break;
				case RIGHT:
					impactX = originX + dist;
					break;
				case UP:
					impactY = originY - dist;
					break;
				case DOWN:
					impactY = originY + dist;
				}
				setImpactHaloPos();
				target.hitted(this, owner);
			}*/
		}
		
		if(timeLiving == LIVE_TIME) game.objectsToRemove.add(this);
		else timeLiving++;
	}
	
	public void setImpactHaloPos() {
		impactHaloX = impactX - HALF_IMPACT_RADIUS + 1;
		impactHaloY = impactY - HALF_IMPACT_RADIUS + 1;
	}
	
	@Override
	public void paint(Graphics2D g) {
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)(LIVE_TIME - timeLiving)/LIVE_TIME));
		g.setColor(Color.CYAN);
		Stroke backup = g.getStroke();
		g.setStroke(new BasicStroke(AMPLITUDE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
		g.drawLine(originX, originY, impactX, impactY);
		g.setStroke(backup);
		g.drawImage(imgImpact, (int)impactHaloX, (int)impactHaloY, null);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		//g.drawRect((int)x, (int)y, width, height);
	}
}
