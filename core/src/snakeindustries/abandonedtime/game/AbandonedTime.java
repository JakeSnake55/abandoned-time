package snakeindustries.abandonedtime.game;

import com.badlogic.gdx.Game;

public class AbandonedTime extends Game {
	@Override
	public void create () 
	{
		setScreen ( new GameScreen(this) );
	}
}

