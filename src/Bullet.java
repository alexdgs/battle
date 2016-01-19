import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.ImageIcon;


public class Bullet extends Projectile implements Hittable {
	
	static final int WIDTH = 8;
	static final int HEIGHT = 8;
	
	static final int MAX_X = Game.MAX_X - WIDTH;
	static final int MAX_Y = Game.MAX_Y - WIDTH;
	
	static final int HALF_HEIGHT = HEIGHT/2;
	
	static final double SLOW = 3.25;
	static final double FAST = 5.5;
	
	static final int OPPOSITE = -1;
	
	int id;
	
	boolean removing;
	boolean removed;
	boolean stealed;
	int hitMetal;
	
	static final Image[] imgDir = {
			null,
			(new ImageIcon("src/bulletE.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT),
			(new ImageIcon("src/bulletN.png")).getImage().getScaledInstance(HEIGHT, WIDTH, Image.SCALE_DEFAULT),
			(new ImageIcon("src/bulletS.png")).getImage().getScaledInstance(HEIGHT, WIDTH, Image.SCALE_DEFAULT),
			(new ImageIcon("src/bulletW.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT)
	};
	
	public Bullet(Tank owner, int id, int dir, double x, double y, int w, int h) {
		super(owner.game,x,y,w,h,owner);
		this.id = id;
		this.dir = dir;
		if(owner instanceof PlayerTank) {
			if(owner.rank > PlayerTank.LEVEL_1) speed = FAST;
			else speed = SLOW;
			if(owner.rank > PlayerTank.LEVEL_3) setFlagOn(STRENGTH);
		} else {
			if(owner.rank == AITank.SNIPER) speed = FAST;
			else speed = SLOW;
		}
		if(owner.isFlagOn(Tank.FAST)) speed += (speed*0.5);
		setFlagOn(HITTABLE);
		setFlagOn(CAN_MOVE);
		removing = false;
		removed = false;
		stealed = false;
	}
	
	@Override
	public void move() {
		if(!removed) {
			if(removing) {
				if(timeRemoving == 0 && owner.team == Unit.PLAYER_TEAM && hitMetal == 1) game.audioPlayer.playSound(AudioPlayer.METAL_HIT);
				timeRemoving++;
				if(timeRemoving == DEFAULT_TIME_TO_REMOVE) remove();
			} else {
				if(owner.isFlagOn(Tank.SLOW)) toggleFlag(CAN_MOVE);
				else setFlagOn(CAN_MOVE);
				if(isFlagOn(CAN_MOVE)) {
					if(dir == LEFT) {
						x -= speed;
						if(x < Game.MIN_X) {
							x = Game.MIN_X;
							hitMetal = 1;
							hitted();
							return;
						}
					}
					else if(dir == RIGHT) {
						x += speed;
						if(x > MAX_X) {
							x = Game.MAX_X;
							hitMetal = 1;
							hitted();
							return;
						}
					}
					else if(dir == UP) {
						y -= speed;
						if(y < Game.MIN_Y) {
							y = Game.MIN_Y;
							hitMetal = 1;
							hitted();
							return;
						}
					} else {
						y += speed;
						if(y > MAX_Y) {
							y = Game.MAX_Y;
							hitMetal = 1;
							hitted();
							return;
						}
					}
					updateTargetPos();
					for(Hittable h : game.hittables) {
						if(hit(h)) {
							if(h instanceof Bullet) {
								Bullet b = (Bullet)h;
								if(b.isFlagOn(HITTABLE) && b.id != id && b.owner.id != owner.id && b.owner.team != owner.team &&
										(b.owner.team == Unit.ENEMY_TEAM || owner.team == Unit.ENEMY_TEAM))
								{
									remove();
									b.remove();
									return;
								}
							} else if(h instanceof Tank){
								Unit t = (Unit)h;
								if(t.isFlagOn(HITTABLE) && t.team != owner.team && (owner.team == Unit.ENEMY_TEAM || t.team == Unit.ENEMY_TEAM)) {
									if(t.isFlagOn(Tank.VULNERABLE)) {
										if(t.isFlagOn(Tank.HAS_BOUNCING_SHIELD)) {
											changeDir(OPPOSITE);
											owner.removeBullet();
											owner = t;
											stealed = true;
											return;
										} else {
											h.hitted(null,owner);
											hitted();
										}
									} else remove();
									return;
								}
							} else {
								GameObject go = (GameObject)h;
								if(go.isFlagOn(HITTABLE) && !(go.isFlagOn(ALLY_INMUNE) && owner.team == Unit.ALLY_TEAM)) {
									hitted();
									hitMetal = Math.max(h.hitted(this,owner), hitMetal);
									if(h instanceof Eagle) return;
								}
							}
						}
					}
				}
			}
		}
	}

	public int hitted() {
		if(!removing) {
			removing = true;
			setRemovingCoordinates();
		}
		return 0;
	}
	
	public void remove() {
		removed = true;
		game.objectsToRemove.add(this);
	}
	
	public void changeDir(int newDir) {
		if(newDir == OPPOSITE) {
			if(dir == LEFT) dir = RIGHT;
			else if(dir == RIGHT) dir = LEFT;
			else if(dir == UP) dir = DOWN;
			else if(dir == DOWN) dir = UP;
		} else dir = newDir;
	}
	
	public int hitted(Projectile projectile, Unit owner) {
		if(!removing) {
			removing = true;
			setRemovingCoordinates();
		}
		return 0;
	}
	
	public void hitLetal(Projectile projectile, Unit owner) {
		hitted(projectile, owner);
	}
	
	@Override
	public void paint(Graphics2D g) {
		if(removing) g.drawImage(imgRem[remOrder[timeRemoving/TIME_TO_SHOW_FRAME_REMOVE]], (int)xImpact, (int)yImpact, null);
		else {
			if(game.teams[owner.team].isFlagOn(TeamManager.SLOW_DOWN)) game.drawSlowEffect(g, imgDir[dir], (int)x, (int)y, dir);
			g.drawImage(imgDir[dir], (int)x, (int)y, null);
		}
	}
}
