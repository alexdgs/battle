
public abstract class TargetProjectile extends Projectile {
	
	Tank target;
	double angle;
	
	public TargetProjectile(Game game, double x, double y, int w, int h, Unit owner, Tank target) {
		super(game, x, y, w, h, owner);
		this.target = target;
		this.target.setFlagOn(Tank.TARGETED_BY_HOMING);
		angle = owner.angleTo(target);
		setSpeeds(angle);
	}
	
	public boolean hitTarget() {
		return target.getBounds().intersects(getBounds());
	}
	
	public void hitLetal() {
		target.hitLetal(null, owner);
		setFlagOn(REMOVING);
	}
	
	public void hit() {
		target.hitted(null, owner);
		setFlagOn(REMOVING);
	}
	
	public boolean targetIsEnemy() {
		return target.team == Unit.ENEMY_TEAM;
	}
}
