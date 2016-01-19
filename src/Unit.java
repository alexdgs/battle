import java.util.PriorityQueue;


public class Unit extends GameObject {
	
	static final int PLAYER_TEAM = 0;
	static final int ENEMY_TEAM = 1;
	static final int ALLY_TEAM = 2;
	
	static final double MAX_ANGLE_DEV_MINIBULLET = Math.PI/90.0;
	
	int id;
	int team;
	int score;
	
	public Unit(Game game, double x, double y, int w, int h, int id, int team) {
		super(game, x, y, w, h);
		this.id = id;
		this.team = team;
		score = 0;
	}
	
	public void score() {
		score++;
	}
	
	public void removeBullet() {
		
	}
	
	public PriorityQueue<Tank> selectTargets(int n, TankComparator comp) {
		PriorityQueue<Tank> targets = new PriorityQueue<Tank>(comp);
		
		int num;
		if(n == PowerUp.HALF_TEAM) {
			num = countHittableEnemyTanks();
			num = (num % 2 == 0 ? num : num+1);
			num /= 2;
		} else if(n == PowerUp.FULL_TEAM) {
			num = Integer.MAX_VALUE;
		} else num = n;
		
		for(TeamManager tm : game.teams[team].enemyTeams) {
			if(tm.tanks.size() > 0) {
				for(Tank t : tm.tanks) {
					if(t.isFlagOn(HITTABLE)) {
						t.distTotarget = distTo(t);
						targets.add(t);
						if(targets.size() > num) targets.poll();
					}
				}
			}
		}
		return targets;
	}
	
	public PriorityQueue<Tank> selectTargets(int n, TankComparator comp, double dist) {
		PriorityQueue<Tank> targets = new PriorityQueue<Tank>(comp);
		
		int num;
		if(n == PowerUp.HALF_TEAM) {
			num = countHittableEnemyTanks();
			num = (num % 2 == 0 ? num : num+1);
			num /= 2;
		} else if(n == PowerUp.FULL_TEAM) {
			num = Integer.MAX_VALUE;
		} else num = n;
		
		for(TeamManager tm : game.teams[team].enemyTeams) {
			for(Tank t : tm.tanks) {
				if(t.isFlagOn(HITTABLE)) {
					t.distTotarget = distTo(t);
					if(t.distTotarget <= dist) {
						targets.add(t);
						if(targets.size() > num) targets.poll();
					}
				}
			}
		}
		return targets;
	}
	
	public Unit selectClosestThreat(double dist) {
		Unit target = null;
		double minDist = Double.POSITIVE_INFINITY;
		
		for(Hittable h : game.hittables) {
			double newDist = distTo((GameObject) h);
			if(newDist <= dist) {
				if(h instanceof Bullet) {
					Bullet b = (Bullet)h;
					if(b.isFlagOn(HITTABLE) && b.id != id && b.owner.id != id && b.owner.team != team &&
							(b.owner.team == Unit.ENEMY_TEAM || team == Unit.ENEMY_TEAM))
					{
						if(newDist < minDist || (target != null && target instanceof Tank)) {
							target = b;
							minDist = newDist;
						}
						continue;
					}
				} else if(h instanceof Tank && !(target != null && target instanceof Bullet)){
					Unit t = (Unit)h;
					if(t.isFlagOn(HITTABLE) && t.team != team && (team == Unit.ENEMY_TEAM || t.team == Unit.ENEMY_TEAM)) {
						if(newDist < minDist) {
							target = t;
							minDist = newDist;
						}
						continue;
					}
				} 
			}
			
		}
		return target;
	}
	
	public Tank getClosestTarget() {
		return selectTargets(1, new TankComparator(TankComparator.CLOSEST)).poll();
	}
	
	public int countHittableEnemyTanks() {
		int num = 0;
		for(TeamManager tm : game.teams[team].enemyTeams) {
			for(Unit t : tm.tanks)
				if(t.isFlagOn(HITTABLE)) num++;
		}
		return num;
	}
	
	public void lowHealth(boolean b) {
		
	}
	
	public double randomDev() {
		double dev = Math.random()*MAX_ANGLE_DEV_MINIBULLET;
		double sign = Math.random();
		if(sign < 0.5) dev *= -1;
		return dev;
	}
}
