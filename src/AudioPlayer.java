import java.io.IOException;
import java.util.HashMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;


public class AudioPlayer implements LineListener {
	
	static final int PLAYER_FIRE = 0;
	static final int PLAYER_MOVE = 1;
	static final int PLAYER_DIE = 2;
	static final int ENEMY_MOVE = 3;
	static final int ENEMY_DIE = 4;
	static final int POW_SPAN = 5;
	static final int POW_PICKUP = 6;
	static final int STOCK = 7;
	static final int TANK_HIT = 8;
	static final int METAL_HIT = 9;
	static final int TICK = 10;
	static final int GAME_START = 11;
	static final int LITTLE_HIT = 12;
	static final int BALL_FIRE = 13;
	static final int MG_FIRE = 14;
	static final int BONUS = 15;
	static final int PAUSE = 20;
	
	static final int MAX_SOUND_INDEX = PAUSE;
	static final int MAX_SUSPENDABLE_SOUND = MG_FIRE;
	static final int ARRAY_SIZE = MAX_SOUND_INDEX + 1;
	
	AudioFormat format;
	Clip[] clips;
	HashMap<Clip, Integer> numClip;
	boolean[] isPlaying;
	boolean[] suspended;
	
	String[] audioFiles = {
			"/snd/player_fire.wav",
			"/snd/player_move.wav",
			"/snd/player_die.wav",
			"/snd/enemy_move.wav",
			"/snd/enemy_die.wav",
			"/snd/pow_span.wav",
			"/snd/pow_pick.wav",
			"/snd/stock.wav",
			"/snd/hit.wav",
			"/snd/metal.wav",
			"/snd/tick.wav",
			"/snd/game_start.wav",
			"/snd/little_hit.wav",
			"/snd/ball_fire.wav",
			"/snd/mg_fire.wav",
			"/snd/bonus.wav",
			null,
			null,
			null,
			null,
			"/snd/pause.wav"
	};
	
	Game game;
	
	public AudioPlayer(Game game) {
		this.game = game;
		clips = new Clip[ARRAY_SIZE];
		numClip = new HashMap<Clip, Integer>();
		isPlaying = new boolean[ARRAY_SIZE];
		suspended = new boolean[ARRAY_SIZE];
		format = new AudioFormat(44100, 16, 2, true, false);
		
		DataLine.Info info = new DataLine.Info(Clip.class, format);
		try {
			for(int i = 0; i <= MAX_SOUND_INDEX; i++) {
				if(audioFiles[i] != null) {
					clips[i] = (Clip) AudioSystem.getLine(info);
					clips[i].open(AudioSystem.getAudioInputStream(AudioPlayer.class.getResource(audioFiles[i])));
					clips[i].addLineListener(this);
					numClip.put(clips[i], i);
				}
			}
			
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
	}
	
	public void playSound(int index) {
		if(index != PLAYER_DIE && isPlaying[GAME_START]) return;
		if(isPlaying[index]) {
			clips[index].stop();
			clips[index].flush();
		}
		clips[index].setFramePosition(0);
		clips[index].start();
	}
	
	public void loopSound(int index) {
		if(index != PLAYER_DIE && isPlaying[GAME_START]) return;
		if(!isPlaying[index]) {
			if(index == PLAYER_MOVE && isPlaying[ENEMY_MOVE]) {
				stopSound(ENEMY_MOVE);
			}
			if(!(index == ENEMY_MOVE && isPlaying[PLAYER_MOVE])) {
				clips[index].setLoopPoints(0, -1);
				clips[index].setFramePosition(0);
				clips[index].start();
			}
		}
	}
	
	public void stopSound(int index) {
		if(isPlaying[index]) {
			clips[index].stop();
			clips[index].flush();
		}
	}
	
	public void suspendAll() {
		for(int i = 0; i <= MAX_SUSPENDABLE_SOUND; i++) {
			if(clips[i] != null && isPlaying[i]) {
				clips[i].stop();
				suspended[i] = true;
			}
		}
	}
	
	public void resumeAll() {
		for(int i = 0; i <= MAX_SUSPENDABLE_SOUND; i++) {
			if(clips[i] != null && suspended[i]) {
				clips[i].start();
				suspended[i] = false;
			}
		}
	}

	@Override
	public void update(LineEvent event) {
		if(event.getType() == LineEvent.Type.STOP) {
			isPlaying[numClip.get((Clip)event.getLine())] = false;
		} else if(event.getType() == LineEvent.Type.START) {
			isPlaying[numClip.get((Clip)event.getLine())] = true;
		}
	}
}
