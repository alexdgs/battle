
public class SlowTurret extends Turret {
	
	static final int TIME_TO_FIRE = 153;
	static final double MAX_TURN_ANGLE_TURRET = Math.PI / 180.0;
	static final double BALL_SPEED = 2.0;
	
	public SlowTurret(Unit owner) {
		super(owner);
		maxTurnAngle = MAX_TURN_ANGLE_TURRET;
		maxTimeToFire = TIME_TO_FIRE;
	}
	
	@Override
	public GameObject getTarget() {
		return ((Tank)owner).target;
	}

	@Override
	public void fire() {
		//System.out.println("Firing ball");
		game.objectsToInsert.add(new StraightBall(game, x + DESPL_BALL_X, y + DESPL_BALL_Y, lastAngle, owner, BALL_SPEED));
		game.audioPlayer.playSound(AudioPlayer.BALL_FIRE);
	}
}
