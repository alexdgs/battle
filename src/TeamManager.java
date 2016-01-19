import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;


public abstract class TeamManager implements EventDriven {
	
	static final int SLOT_WIDTH = Tank.WIDTH;
	static final int SLOT_HEIGHT = Tank.HEIGHT;
	
	static final int SLOT_WIDTH_IN_CELLS = SLOT_WIDTH / TerrainManager.GRID_SIZE;
	static final int SLOT_HEIGHT_IN_CELLS = SLOT_HEIGHT / TerrainManager.GRID_SIZE;
	
	// Flags for TeamManager
	static final int FREEZED = 1;
	static final int SLOW_DOWN = 2;
	static final int STEP_SLOW = 3;
	
	// Events for 
	static final int TIMER_FREEZED = 1;
	static final int TIMER_SLOW_DOWN = 2;
	
	static final int[][][] SLOTS = {
		{
			{Game.MIN_X + 20, Game.CENTER_Y, Game.RIGHT},
			{Tank.MAX_X - 20, Game.CENTER_Y, Game.LEFT},
			{Game.CENTER_X, Game.MIN_Y + 20, Game.DOWN},
			{Game.CENTER_X, Tank.MAX_Y - 20, Game.UP}
		},
		
		{
			{Game.CENTER_X - 80, Game.CENTER_Y, Game.LEFT},
			{Game.CENTER_X, Game.CENTER_Y - 80, Game.UP},
			{Game.CENTER_X + 80, Game.CENTER_Y, Game.RIGHT},
			{Game.CENTER_X, Game.CENTER_Y + 80, Game.DOWN}
		},
		
		{
			{Game.MIN_X, Game.MIN_Y + 80, Game.DOWN},
			{Game.MIN_X + 80, Game.MIN_Y, Game.RIGHT},
			{Tank.MAX_X - 80, Game.MIN_Y, Game.LEFT},
			{Tank.MAX_X, Game.MIN_Y + 80, Game.DOWN},
			{Tank.MAX_X, Tank.MAX_Y - 80, Game.UP},
			{Tank.MAX_X - 80, Tank.MAX_Y, Game.LEFT},
			{Game.MIN_X + 80, Tank.MAX_Y, Game.RIGHT},
			{Game.MIN_X, Tank.MAX_Y - 80, Game.UP}
			
		}
	};
	
	Deque<Tank> tanks;
	
	ArrayList<TeamManager> allyTeams;
	ArrayList<TeamManager> enemyTeams;
	
	EventManager eventManager;
	
	Slot[] slots;
	int team;
	int idBase;
	int flags;
	Game game;
	
	public TeamManager(Game game,  int team) {
		this.game = game;
		this.team = team;
		this.slots = game.mapReader.getSlots(team);
		idBase = team*10000;
		tanks = new LinkedList<Tank>();
		
		eventManager = new EventManager(this);
		
		allyTeams = new ArrayList<TeamManager>();
		enemyTeams = new ArrayList<TeamManager>();
	}
	
	@Override
	public void eventIn(int eventID) {
		switch(eventID) {
		case TIMER_FREEZED:
			freeze(true);
			break;
		case TIMER_SLOW_DOWN:
			slowDown(true);
			break;
		default:
			eventInSpecificEvent(eventID);
		}
	}

	@Override
	public void eventOut(int eventID) {
		switch(eventID) {
		case TIMER_FREEZED:
			freeze(false);
			break;
		case SLOW_DOWN:
			slowDown(false);
			break;
		default:
			eventOutSpecificEvent(eventID);	
		}
	}
	
	public void eventInSpecificEvent(int eventID) { /* Defined if necessary in subclasses */ }
	
	public void eventOutSpecificEvent(int eventID) { /* Defined if necessary in subclasses */ }
	
	public void eventSecond(int second) { /* Not used in TeamManager for now */ }
	
	public void step() { /* Defined in subclasses */ }
	
	public void addAllyTeam(TeamManager team) {
		allyTeams.add(team);
	}
	
	public void addEnemyTeam(TeamManager team) {
		enemyTeams.add(team);
	}
	
	public void destroyTanks() {
		for(Tank t : tanks) {
			t.destroy();
		}
	}
	
	public void freeze(boolean b) {
		if(b) {
			if(isFlagOn(SLOW_DOWN)) {
				slowDown(false);
				eventManager.removeTimer(TIMER_SLOW_DOWN);
			}
			for(Unit t : tanks) t.setFlagOff(Tank.CAN_MOVE);
			setFlagOn(FREEZED);
		} else {
			for(Unit t : tanks) t.setFlagOn(Tank.CAN_MOVE);
			setFlagOff(FREEZED);
		}
		
	}
	
	public void slowDown(boolean b) {
		if(b) {
			if(isFlagOn(FREEZED)) {
				freeze(false);
				eventManager.removeTimer(TIMER_FREEZED);
			}
			for(Tank t : tanks) t.slow(true);
			setFlagOn(SLOW_DOWN);
		} else {
			for(Tank t : tanks) t.slow(false);
			setFlagOff(SLOW_DOWN);
		}
	}
	
	public void freezeEnemies(int time) {
		for(TeamManager tm : enemyTeams) {
			tm.eventManager.scheduleTimedEvent(TIMER_FREEZED, time);
		}
	}
	
	public void slowEnemies(int time) {
		for(TeamManager tm : enemyTeams) {
			tm.eventManager.scheduleTimedEvent(TIMER_SLOW_DOWN, time);
		}
	} 
	
	public void destroyEnemies() {
		for(TeamManager tm : enemyTeams) {
			tm.destroyTanks();
		}
	}
	
	public void fullHealth() {
		healAll();
		for(TeamManager tm : allyTeams) tm.healAll();
		if(team == Unit.PLAYER_TEAM && game.isFlagOn(Game.EAGLE)) {
			for(Eagle e : game.eagles) e.fullHealth();
		}
	}
	
	public void healAll() { /* Defined in subclasses */ }
	
	public void setTarget(Unit target) {
		for(Tank t : tanks) 
			t.target = target;
	}
	
	public void setTarget() {
		if(!isFlagOn(FREEZED)) {
			ArrayList<Tank> list = new ArrayList<Tank>();
			for(TeamManager e : enemyTeams) {
				list.addAll(e.tanks);
			}
			
			for(Tank t : this.tanks) {
				if(!t.isFlagOn(Tank.SPANNING) && !t.isFlagOn(Tank.REMOVING)) t.setTarget(list);
			}
		} else {
			for(Tank t : this.tanks) {
				t.target = null;
			}
		}
	}
	
	public void orderReinforcements(int r) {
		if(team == Unit.PLAYER_TEAM && game.teams[Unit.ALLY_TEAM] != null)
			game.teams[Unit.ALLY_TEAM].orderExtras(r);
		else
			orderExtras(r);
	}
	
	public void orderExtras(int r) { /* Defined in subclasses */ }
	
	public boolean isFlagOn(int flag) {
		return (flags & (1 << flag)) != 0;
	}
	
	public void setFlagOn(int flag) {
		flags  |= (1 << flag);
	}
	
	public void setFlagOff(int flag) {
		flags  &= ~(1 << flag);
	}
	
	public void toggleFlag(int flag) {
		flags ^= (1 << flag);
	}
}
