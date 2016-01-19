import java.util.ArrayList;


public class SelectiveSpawner {
	
	static final int INIT_TIME_TO_SPAWN_SWARM = 30;
	static final int[] DEFAULT_MIN_TIME = {16, 18, 20, 22};
	static final int[] INIT_TANKS_PER_SWARM = {4, 5, 6, 8};
	static final int[] MAX_TANKS_PER_SWARM = {12, 16, 20, 24};
	
	static final int[] TIME_TRY_EXTRA_SLOT_SWARM = {45, 40, 35, 30};
	static final int[] TANKS_PER_EXTRA_SLOT = {2, 4, 6, 8};
	
	int seconds;
	int secondsNextSwarm;
	int numPlayers;
	int currNumTanks;
	int remTanks;
	int currTarget;
	int currSlot;
	ArrayList<Integer>[] linkedSlots;
	AITeamManager aiteam;
	
	boolean extraSlots;
	int numExtraSlots;
	int secondsExtraSlot[];
	int timerExtraSlot[];
	int remTanksExtraSlot[];
	
	public SelectiveSpawner(AITeamManager aiteam, int numPlayers) {
		this.aiteam = aiteam;
		linkedSlots = aiteam.targetSlots;
		this.numPlayers = numPlayers;
		secondsNextSwarm = INIT_TIME_TO_SPAWN_SWARM;
		seconds = 0;
		currNumTanks = INIT_TANKS_PER_SWARM[numPlayers];
		remTanks = 0;
		currTarget = 0;
		currSlot = 0;
	}
	
	public void step() {
		if(aiteam.game.isFlagOn(Game.GAME_OVER) || aiteam.game.isFlagOn(Game.WIN)) return;
		
		if(remTanks > 0) {
			if(aiteam.isFlagOn(AITeamManager.CAN_SPAN_NEW_UNIT)) {
				aiteam.orderTank(linkedSlots[currTarget].get(currSlot));
				aiteam.remaining--;
				currSlot = (currSlot+1) % linkedSlots[currTarget].size();
				remTanks--;
			}
		}
		
		if(extraSlots) {
			for(int i = 0; i < numExtraSlots; i++) {
				if(timerExtraSlot[i] == AITeamManager.TIME_TO_SPAN_NEW_UNIT) {
					if(remTanksExtraSlot[i] > 0) {
						aiteam.orderTankInExtraSlot(i);
						remTanksExtraSlot[i]--;
					}
				} else timerExtraSlot[i]++;
			}
		}
	}
	
	public void second() {
		seconds++;
		if(seconds == secondsNextSwarm) {
			int extraSlot = getRandomExtraSlotAvailable();
			if(extraSlot >= 0 && Game.randomBoolean(0.5)) {
				remTanksExtraSlot[extraSlot] += Math.min(currNumTanks++, MAX_TANKS_PER_SWARM[numPlayers]);
			} else {
				currTarget = (int)(Math.random()*linkedSlots.length);
				remTanks = Math.min(currNumTanks++, MAX_TANKS_PER_SWARM[numPlayers]);
			}
			secondsNextSwarm = Math.max(--secondsNextSwarm, DEFAULT_MIN_TIME[numPlayers]);
			seconds = 0;
		}
		
		if(extraSlots) {
			for(int i = 0; i < numExtraSlots; i++) {
				if(aiteam.extraSlotEnabled[i]) {
					secondsExtraSlot[i]++;
					if(secondsExtraSlot[i] == TIME_TRY_EXTRA_SLOT_SWARM[numPlayers]) {
						timerExtraSlot[i] = AITeamManager.TIME_TO_SPAN_NEW_UNIT;
						remTanksExtraSlot[i] += TANKS_PER_EXTRA_SLOT[numPlayers];
						secondsExtraSlot[i] = 0;
					}
				}
			}
		}
		
	}
	
	public void setExtraSlots(int n) {
		numExtraSlots = n;
		timerExtraSlot = new int[n];
		secondsExtraSlot = new int[n];
		remTanksExtraSlot = new int[n];
		extraSlots = true;
	}
	
	public int getRandomExtraSlotAvailable() {
		int extra = -1;
		ArrayList<Integer> extras = new ArrayList<Integer>();
		for(int i = 0; i < numExtraSlots; i++) {
			if(aiteam.extraSlotEnabled[i]) {
				extras.add(i);
			}
		}
		int n = 0;
		if((n = extras.size()) > 0) {
			extra = extras.get((int)(Math.random()*n));
		}
		return extra;
	}
}
