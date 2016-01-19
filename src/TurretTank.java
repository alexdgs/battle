
public class TurretTank extends AITank {
	
	static final int HIT_POINTS = 5;
	static final double SIGHT_RANGE = 200;
	static final double FIRE_RANGE = 200;
	static final double MIN_DISTANCE = 160;
	
	public TurretTank(Game game, int x, int y, int initDir, int id, int team, int hitPoints) {
		super(game, x, y, initDir, id, team, TURRET, hitPoints);
		//setType(TURRET);
		setFlagOff(CAN_FIRE);
		timerManager.removeTimer(TIMER_FIRE);
		turret = new SlowTurret(this);
		turret.sight = FIRE_RANGE;
	}
	
	@Override
	public int getOptimalMove(double dx, double dy) {
		int next = 0;
		double adx = Math.abs(dx);
		double ady = Math.abs(dy);
		if((isFlagOn(INVERSE_PREF))) {
			if(ady < MIN_ALLIG_TO_TARGET) {
				if(dx > 0.0) next = LEFT;
				else next = RIGHT;
				
				//if(distTo(target) <= MIN_DISTANCE) setFlagOff(WALKING);
			} else {
				if(dy > 0.0) next = UP;
				else next = DOWN;
			}
			
			if(distTo(target) <= MIN_DISTANCE) setFlagOff(WALKING);
		} else {
			if(adx < MIN_ALLIG_TO_TARGET) {
				if(dy > 0.0) next = UP;
				else next = DOWN;
				
				//if(distTo(target) <= MIN_DISTANCE) setFlagOff(WALKING);
			} else {
				if(dx > 0.0) next = LEFT;
				else next = RIGHT;
			}
			
			if(distTo(target) <= MIN_DISTANCE) setFlagOff(WALKING);
		}
		return next;
	}
}
