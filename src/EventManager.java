import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


public class EventManager {
	
	static final int NUM_TIMERS = 32;
	
	EventDriven owner;
	ArrayList<Integer> activeTimedEvents;
	Queue<Integer> readyEvents;
	Queue<Integer> newEvents;
	Queue<Integer> newTimerEvents;
	int[] remainingTimeForEvent;
	
	public EventManager(EventDriven owner) {
		this.owner = owner;
		activeTimedEvents = new ArrayList<Integer>();
		readyEvents = new LinkedList<Integer>();
		newEvents = new LinkedList<Integer>();
		newTimerEvents = new LinkedList<Integer>();
		remainingTimeForEvent = new int[NUM_TIMERS];
	}
	
	public void scheduleEvent(int eventID) {
		newEvents.add(eventID);
	}
	
	public void scheduleTimedEvent(int eventID, int time) {
		remainingTimeForEvent[eventID] = time;
		newTimerEvents.add(eventID);
	}
	
	public void processEvents() {
		ArrayList<Integer> aliveTimers = new ArrayList<Integer>();	// Copy all timed events that will not finalize in this call
		
		//while(!readyEvents.isEmpty()) owner.eventIn(readyEvents.poll());	// Fire events
		
		for(int timerId : activeTimedEvents) {
			remainingTimeForEvent[timerId]--;	// Advance timer
			if(remainingTimeForEvent[timerId] < 1) owner.eventOut(timerId);	// Timer out, launch a trigger on this EventManager owner
			else aliveTimers.add(timerId);	// Timer still alive, keep it
		}
		activeTimedEvents = aliveTimers;	// Replace old timer list
		
		while(!newTimerEvents.isEmpty()) {	// Add new timed events
			int newTimer = newTimerEvents.poll();
			owner.eventIn(newTimer);
			if(!activeTimedEvents.contains(newTimer)) activeTimedEvents.add(newTimer);
		}
	}
	
	public void endTimer(int timerID) {
		remainingTimeForEvent[timerID] = 0;
	}
	
	public void removeTimer(int timerID) {
		activeTimedEvents.remove((Integer)timerID);
	}
}
