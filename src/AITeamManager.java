import java.awt.Image;
import java.util.ArrayList;

import javax.swing.ImageIcon;


public class AITeamManager extends TeamManager {
	
	static final int TIME_TO_SPAN_NEW_UNIT = 35;
	static final int[] TANKS_PER_TARGET = {2,1,0,0};
	
	static final int DEFAULT_STOCK = 1500;
	
	static final int WITH_SELECTIVE_SPAWNER = 0;
	static final int WITHOUT_SELECTIVE_SPAWNER = 1;
	
	static final int[][][] DEFAULT_INITIAL = {
		{
			null,
			{0, 0, 0, 0},
			{0, 0, 0, 0}
		},
		
		{
			null,
			{4, 5, 6, 7},
			{0, 0, 0, 0}
		}
		
	};
	
	static final int[][][] DEFAULT_MAX = {
		{
			null,
			{2, 4, 6, 8},
			{6, 4, 2, 0}
		},
		
		{
			null,
			{16, 18, 20, 24},
			{6, 4, 2, 0}
		}
		
	};
	
	static final int[][] SECONDS_TO_NEW_TANK = {
		{0, 120, 120},
		{0, 40, 120}
	};
	
	// Flags for AITeamManager
	static final int CAN_SPAN_NEW_UNIT = 10;
	
	// Event IDs
	static final int EVENT_SPAWN_UNIT = 10;
	static final int TIMER_SPAN_NEW_UNIT = 11;
	
	/*static final int[][][] TARGET_SLOTS = {
		null,
		
		{
			{0,1},
			{1,2},
			{3,0},
			{2,3}
		},
		
		{
			{0,1},
			{2,3},
			{6,7},
			{4,5}
		}
		
	};*/
	
	int currentSlot;
	int remaining;
	int currMaxTanks;
	int extraTanks = 0;
	int numTargets;
	int currMaxTanksForTime;
	int currMaxTanksForTargets = Integer.MAX_VALUE;
	ArrayList<Integer>[] targetSlots;
	boolean[] slotDisabled;
	int[][] extraSlots;
	boolean[] extraSlotEnabled;
	int op;
	
	Image[][][][] img;
	Image[] imgValue;
	SelectiveSpawner ss;
	
	public AITeamManager(Game game, int team, boolean selective) {
		super(game, team);
		slotDisabled = new boolean[slots.length];
		remaining = DEFAULT_STOCK;
		currentSlot = 0;
		op = game.mapReader.selectiveSpawningInfoAvailable ? WITH_SELECTIVE_SPAWNER : WITHOUT_SELECTIVE_SPAWNER;
		currMaxTanksForTime = DEFAULT_INITIAL[op][team][PlayerTeamManager.NUM_PLAYERS];
		setFlagOn(CAN_SPAN_NEW_UNIT);
		
		img = new Image[6][6][5][2];
		imgValue = new Image[6];
		
		for(int a = AITank.BASIC; a <= AITank.TURRET; a++) {
			for(int b = 1; b <= 5; b++) {
				for(int c = Game.RIGHT; c <= Game.LEFT; c++) {
					for(int d = 1; d <= 2; d++) {
						img[a][b][c][d-1] = (new ImageIcon("src/T" + a + "_L" + b + "_" + game.getCardinal(c) + d + ".png")).getImage().getScaledInstance(Tank.WIDTH, Tank.HEIGHT, Image.SCALE_DEFAULT);
					}
				}
			}
			imgValue[a] = (new ImageIcon("src/value" + a + ".png")).getImage().getScaledInstance(Tank.WIDTH, Tank.HEIGHT, Image.SCALE_DEFAULT);
		}
		
		if(game.mapReader.selectiveSpawningInfoAvailable) {
			this.targetSlots = game.mapReader.getSelectiveSpawnerInfo(team);
			numTargets = targetSlots.length;
			currMaxTanksForTargets = numTargets * TANKS_PER_TARGET[PlayerTeamManager.NUM_PLAYERS];
		}
		
		setMaxTanks();
		if(selective && game.mapReader.selectiveSpawningInfoAvailable) {
			ss = new SelectiveSpawner(this, PlayerTeamManager.NUM_PLAYERS);
		}
	}
	
	@Override
	public void eventInSpecificEvent(int eventID) {
		switch(eventID) {
		case TIMER_SPAN_NEW_UNIT:
			setFlagOff(CAN_SPAN_NEW_UNIT);
			break;
		}
	}

	@Override
	public void eventOutSpecificEvent(int eventID) {
		switch(eventID) {
		case TIMER_SPAN_NEW_UNIT:
			setFlagOn(CAN_SPAN_NEW_UNIT);
			break;
		}
	}
	
	@Override
	public void eventSecond(int second) {
		if(second % SECONDS_TO_NEW_TANK[op][team] == 0) {
			currMaxTanksForTime = Math.min(currMaxTanksForTime+1, DEFAULT_MAX[op][team][PlayerTeamManager.NUM_PLAYERS]);
			setMaxTanks();
		}
		if(ss != null) ss.second();
	}
	
	@Override
	public void step() {
		eventManager.processEvents();
		for(Tank ai : tanks) {
			ai.move();
			//((AITank)ai).setVisiblility();
		}
		
		if(ss != null) ss.step();
		
		if(isFlagOn(CAN_SPAN_NEW_UNIT) && ((remaining > 0 && tanks.size() < currMaxTanks) || extraTanks > 0)) {
			//checkCurrentSlot();
			orderTank();
			eventManager.scheduleTimedEvent(TIMER_SPAN_NEW_UNIT, TIME_TO_SPAN_NEW_UNIT);
			
			do {
				currentSlot = (currentSlot+1) % slots.length;
			} while(slotDisabled[currentSlot]);
			
			remaining--;
			if(extraTanks > 0) {
				remaining++;
				extraTanks--;
			}
		}
	}
	
	/**Create a new AITank for this team and enlist it.*/
	public void orderTank() {
		AITank tank = /*(Game.randomBoolean(0.2) ?
				new TurretTank(game, slots[currentSlot].x, slots[currentSlot].y, slots[currentSlot].defaultDir, idBase+remaining, team, AITank.RANDOM_HIT_POINTS) :*/
				new AITank(game, slots[currentSlot].x, slots[currentSlot].y, slots[currentSlot].defaultDir, idBase+remaining, team, AITank.RANDOM_TYPE, AITank.RANDOM_HIT_POINTS);
		tanks.add(tank);
		game.objectsToInsert.add(tank);
	}
	
	/**Create a new AITank for this team and enlist it. Use the specified slot number*/
	public void orderTank(int numSlot) {
		//checkSlot(numSlot);
		AITank tank = new AITank(game, slots[numSlot].x, slots[numSlot].y, slots[numSlot].defaultDir, idBase+remaining, team, AITank.RANDOM_TYPE, AITank.RANDOM_HIT_POINTS);
		tanks.add(tank);
		game.objectsToInsert.add(tank);
		eventManager.scheduleTimedEvent(TIMER_SPAN_NEW_UNIT, TIME_TO_SPAN_NEW_UNIT);
	}
	
	public void orderTankInExtraSlot(int numExtraSlot) {
		AITank tank = new AITank(game, extraSlots[numExtraSlot][Game.X_POS], extraSlots[numExtraSlot][Game.Y_POS], AITank.RANDOM_DIR, idBase+remaining, team, AITank.RANDOM_TYPE, AITank.RANDOM_HIT_POINTS);
		tanks.add(tank);
		game.objectsToInsert.add(tank);
	}
	
	/**
	 * Insert a Tank in this AITeamManager registry. Although the argument is a
	 * Tank, only AITanks are supported.
	 * 
	 * @param t Tank to insert in this AITeamManager. Must be an AITank
	 * */
	public void insertTank(Unit t) {
		if(t instanceof AITank) {
			AITank ai = (AITank)t;
			ai.powerUpHitPoints = 0;	// Allies shouldn't generate POWs
			ai.team = team;
			ai.setTeam();
			if(isFlagOn(FREEZED)) ai.setFlagOff(Tank.CAN_MOVE);
			else ai.setFlagOn(Tank.CAN_MOVE);
			if(isFlagOn(SLOW_DOWN)) ai.slow(true);
			else ai.slow(false);
			tanks.add(ai);
		}
	}
	
	/**
	 * Upgrade, in terms of health, all Tanks in this AITeamManager.
	 * 
	 * @return Number of Tanks successfully upgraded
	 * */
	public void maxUpgradeAll() {
		for(Tank t : tanks) t.maxUpgrade();
	}
	
	/**
	 * Set value of variable {@code extraTanks} to the value of the
	 * argument. Extra tanks neither reduce the amount of remaining
	 * Tanks of this AITeamManager nor obey the limit of maximum
	 * simultaneous Tanks for this AITeamManager.
	 * 
	 * @param r Number of extra Tanks to add.
	 * 
	 * */
	@Override
	public void orderExtras(int r) {
		extraTanks += r;
	}
	
	public void checkCurrentSlot() {
		checkSlot(currentSlot);
	}
	
	public void checkSlot(int numSlot) {
		for(double x = slots[numSlot].x; x < slots[numSlot].y + SLOT_WIDTH; x+=TerrainManager.GRID_SIZE) {
			for(double y = slots[numSlot].x; y < slots[numSlot].y + SLOT_HEIGHT; y+=TerrainManager.GRID_SIZE)
				if(game.terrainManager.valueOn((int)x, (int)y) != TerrainManager.EMPTY)
					game.terrainManager.remove((int)x, (int)y);
		}
	}
	
	public void setExtraSlots(ArrayList<Location> loc) {
		int n = loc.size();
		extraSlotEnabled = new boolean[n];
		extraSlots = new int[n][2];
		for(int i = 0; i < n; i++) {
			extraSlots[i][Game.X_POS] = (int)loc.get(i).x;
			extraSlots[i][Game.Y_POS] = (int)loc.get(i).y;
		}
		
		if(ss != null) ss.setExtraSlots(n);
	}
	
	public void setExtraSlotState(int slot, boolean state) {
		//System.out.println((state ? "Enabling" : "Disabling") + " slot " + slot);
		extraSlotEnabled[slot] = state;
	}
	
	public void setTargetState(int target, boolean state) {
		if(targetSlots == null) return;
		
		if(state) numTargets++;
		else numTargets--;
		
		if(numTargets > 0) {
			for(int slot : targetSlots[target]) {
				slotDisabled[slot] = !state;
			}
			currMaxTanksForTargets = numTargets * TANKS_PER_TARGET[PlayerTeamManager.NUM_PLAYERS];
			setMaxTanks();
		}
	}
	
	public void setMaxTanks() {
		currMaxTanks = Math.min(currMaxTanksForTargets, currMaxTanksForTime);
	}
	
	@Override
	public void healAll() {
		maxUpgradeAll();
	}
}
