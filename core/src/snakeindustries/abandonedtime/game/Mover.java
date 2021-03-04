package snakeindustries.abandonedtime.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polyline;

public class Mover {
	
	Polyline path;
	Texture platform;
	float time = 0;
	float posX;
	float posY;
	float period;
	float velocityX;
	float velocityY;
	
	public Mover(Polyline route, float _period) {
		path = route;
		period = _period;
		time = 0;
		platform = new Texture("Platform block.jpg");
	}
	
	public boolean Intersects(float playerX, float playerY) {
		if(playerX >= posX && playerX <= posX+16) {
			if(playerY >= posY + 10 && playerY <= posY + 16 ) {
				return true;
			}
		}
		return false;
	}
	
	public void Update(float delta) {
		time += delta;
		if(time >= period)
			time -= period;
		if(time<= period/2) {
			float fraction = (2*time)/period;
			float[] vertices = path.getTransformedVertices();
			posX = vertices[0] + fraction*(vertices[2]-vertices[0]);
			posY = vertices[1] + fraction*(vertices[3]-vertices[1]);
			velocityX = (2*(vertices[2]-vertices[0]))/period;
			velocityY = (2*(vertices[3]-vertices[1]))/period;
		}
		else {
			float fraction = (2*time-period)/period;
			float[] vertices = path.getTransformedVertices();
			posX = vertices[2] + fraction*(vertices[0]-vertices[2]);
			posY = vertices[3] + fraction*(vertices[1]-vertices[3]);
			velocityX = (2*(vertices[0]-vertices[2]))/period;
			velocityY = (2*(vertices[1]-vertices[3]))/period;
		}
	}
	
	public void Render(SpriteBatch batch) {
		batch.draw(platform, posX, posY);
	}

}
