package snakeindustries.abandonedtime.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.utils.Array;

public class GameScreen extends AbstractGameScreen {

	SpriteBatch batch;
	Texture player;
	Texture coin = new Texture("coin.png");
	Texture background;
	private BitmapFont font;
	
	float playerX = 16;
	float playerY = 64;
	boolean InputRight = false;
	boolean InputLeft = false;
	boolean InputUp = false;
	private float elapsed;
	//with updateWorld
	static float velocityX = 0;
	static float velocityY = 0;
	private static final int MaxSpeed = 100000;
	private static final float Gravity = 20F;
	private static final float Friction = 1F;
	private static final float viewHeight = 240;
	private static final float viewWidth = 320;
	private static final float backgroundViewHeight = 128;
	private static final float backgroundViewWidth = 256;
	
	static final int PLATFORM = 5;
	static final int DIRT = 6;
	static final int COIN = 7;
	static final int POTION = 8;
	private static final String FONT_FILE = "Basic Regular 400.ttf";
	
	float terminalVelocity;
	//MapLoader
	
	private OrthographicCamera cam;
	private OrthographicCamera hudCam;
	private OrthographicCamera backgroundCam;
	
	private float camY = 128;
	private float camX = 53;
	int tileSize;
	private boolean facingLeft = false;
	private boolean spiked = false;
	private int rotation = 0;
	private float lerpB = (viewWidth/5);
	private float lerpA = (viewWidth/5);
	private float lerpedTime;
	
	private Collisions collisions;
	private Array<Mover> movers;
	private int coinCount;
	private float abscamX;
	private float playerFreedom;
	
	static float lastMoverY;
	
	static float lastMoverX;
	
	public TiledMap tileMap;
	OrthogonalTiledMapRenderer tmr;
	private int rightKey[] = {Input.Keys.RIGHT,
	Input.Keys.D
	};
	private int leftKey[] = {Input.Keys.LEFT,
	Input.Keys.A
	};
	private int upKey[] = {Input.Keys.UP,
	Input.Keys.W
	};
	
	
	private void init() 
	{
		batch = new SpriteBatch();
		cam = new OrthographicCamera();
		cam.setToOrtho(false, viewWidth, viewHeight);
		hudCam = new OrthographicCamera();
		hudCam.setToOrtho(false, viewWidth, viewHeight);
		backgroundCam = new OrthographicCamera();
		backgroundCam.setToOrtho(false, backgroundViewWidth, backgroundViewHeight);
		hudCam.update();
		collisions = new Collisions();
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(FONT_FILE));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 16;
		font = generator.generateFont(parameter);
		font.setColor(Color.WHITE);
		
		tileMap = new TmxMapLoader().load("maps/untitled.tmx");
		tmr = new OrthogonalTiledMapRenderer(tileMap);
		
		coinCount = 0;
		movers = new Array<Mover>();
		for(MapObject object: tmr.getMap().getLayers().get("Moving Platforms").getObjects()) {
			if(object instanceof PolylineMapObject) {
				Polyline line = ((PolylineMapObject)object).getPolyline();
				movers.add(new Mover(line, 8));
			}
		}
		player = new Texture("Character.png");
				
		
		
	}
	
	public GameScreen (Game game) {
		super(game);
	}
	
	@Override
	public void show() {
		init();

	}

	
	public void render(float delta) {
		handleInput();
		terminalVelocity = -(6/delta);
		collisions.colliding(delta, playerY, playerX, facingLeft, tileMap, movers);
		tileSize = collisions.tileSize;
		
		collisionReact();
		ItemPickup();
		updateWorld(delta);
		for (Mover mover: movers) {
			mover.Update(delta);
		}
		setCamera(delta);
		
		Gdx.gl.glClearColor(119/255.0f, 247/255.0f, 247/255.0f, 0.5f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		//renderBackground();
		tmr.setView(cam);
		tmr.render();
		batch.setProjectionMatrix(cam.projection);
		batch.setTransformMatrix(cam.view);
		batch.begin();
		for (Mover mover: movers) {
			mover.Render(batch);
		}
		batch.draw(player, playerX, playerY, 0, 0, 16, 32, 1, 1, rotation, 0, 0, tileSize, tileSize*2, facingLeft, false);
		batch.end();
		
		batch.setProjectionMatrix(hudCam.projection);
		batch.setTransformMatrix(hudCam.view);
		batch.begin();
		batch.draw(coin, 2, viewHeight-23);
		font.draw(batch, String.format("%d", coinCount), 20, viewHeight-10);
		batch.end();

	}
	
	private void renderBackground() {
		batch.setProjectionMatrix(backgroundCam.projection);
		batch.setTransformMatrix(backgroundCam.view);
		batch.begin();
		int layers[] = { 5, 4, 3, 2, 1};
		for (int i : layers) {
			//This if is used to handle 1st, 2nd and 3rd syntax
			if(i == 1 || i ==2 ||i==3) {
				if(i==1) {
					background = new Texture("background/1st order.png");				
				}
				if(i==2) {
					background = new Texture("background/2nd order.png");
				}
				if(i==3) {
					background = new Texture("background/3rd order.png");
				}
			}else {
				background = new Texture("background/"+i+"th order.png");
			}
			//Draw whatever texture called earlier three times in a way to cover the whole screen...
			//Simplifies drawing three separate locations into one for loop
			
			//Uses multiplication and rounding to get to nearest multiple of 256
			//Three versions made as screen is bigger than background images, so you need 3 images in worst case to cover it.
			float Img[] = {
					(float) (backgroundViewWidth*Math.ceil((abscamX/i)/backgroundViewWidth)), 
					(float) (backgroundViewWidth*Math.floor((abscamX/i)/backgroundViewWidth)),
					(float) (backgroundViewWidth*Math.floor(((abscamX/i)/backgroundViewWidth)-1))
			};
			for (float f : Img) {
				batch.draw(background, f-(abscamX/i), 0);
			}
			
			
			
		}
		batch.end();
				
	}

	private void lerp(float delta) {
		if(lerpedTime <= 0.4F) {
			lerpedTime += delta;
			camX  = lerpA + (lerpB - lerpA)*(lerpedTime/0.4F);
		}
		
	}
	
	private void setCamera(float delta) {
		lerp(delta);
		camY = playerY;
		
		//limit camY from going down below 128
		if(camY <= 128) {
			camY = 128;
		}
		//if the absolute x camera pos is less than 160, set it to 160
		 abscamX = playerX + camX;
		if(abscamX <= 160) {
			abscamX = 160;
		}
		//set position of camera, runs each cycle
		cam.position.set(abscamX, camY, 0);
		
		cam.update();
	}

	private void collisionReact() {
		if((!collisions.isPlayerInBlock ) || velocityY > 0 )
			velocityY -= Gravity;
		if (velocityY < terminalVelocity) 
			velocityY = terminalVelocity;
		if(collisions.isPlayerOnMover) {
			
		}
		
		if ((collisions.isPlayerInBlock && velocityY <= 0) && !collisions.isPlayerOnMover)
		{
			if( playerY % tileSize >= (tileSize-6)) {
				playerY += (tileSize - playerY % tileSize);
				velocityY = 0;
			}
			
		}
		if(collisions.isPlayerOnMover && velocityY <= 0) {
			velocityY = 0;
			if(playerY <= collisions.usedMover.posY+15.999F) {
				playerY = collisions.usedMover.posY+15.999F;
			}
		}
		if (spiked) {
			
		}
		
	}

	
	
	private void ItemPickup() {
		//opening the right Layer
		TiledMapTileLayer layer = 
				(TiledMapTileLayer) tileMap.getLayers().get("Items");
		//Arrays to make for loop possible
		int YOffsets[] = { 0, 15, 31 };
		int XOffsets[] = { (int) ((playerX+3)/tileSize), (int) ((playerX+13)/tileSize) };
		// making an empty Tile to replace existing Items
		Cell emptyCell = new Cell();
		TiledMapTileSet tileSet = tileMap.getTileSets().getTileSet("version2");
		emptyCell.setTile(tileSet.getTile(0));
		
		for (int y : YOffsets) {
			for (int x : XOffsets) {
				Cell cell = layer.getCell(x, (int) ((playerY+y)/tileSize));
				if (cell != null && cell.getTile() != null) {
					if(cell.getTile().getId() == COIN ) {
						layer.setCell(x, (int) ((playerY+y)/tileSize), emptyCell);
						pickupCoin();
					}
					if(cell.getTile().getId() == POTION ) {
						layer.setCell(x, (int) ((playerY+y)/tileSize), emptyCell);
						pickupPotion();
					}
				}
			}
		}
		
	}

	private void pickupPotion() {
		// TODO Auto-generated method stub
		
	}

	private void pickupCoin() {
		coinCount ++;
		
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		dispose();
	}

	@Override
	public void dispose() {
		batch.dispose();
		player.dispose();
		movers = null;
		

	}
	
	private void updateWorld(float delta) 
	{
		
		
		if(collisions.isPlayerOnMover) 
		{
			playerX += collisions.usedMover.velocityX*delta;
			playerY += collisions.usedMover.velocityY*delta;
		}
		
		elapsed += delta;
		if(elapsed > 0.005F)
		{
			elapsed -= 0.005F;
			if(InputLeft == true) {
				if (velocityX > -MaxSpeed) 
				{
	        		if(velocityX >= 0) 
	        		{
	        			velocityX -= 10000F;
	        		}
	        		else
	        		{
	        		velocityX -= 7500F ;
	        		}
	        	}
				InputLeft = false;
			}
			if(InputRight == true) {
				if (velocityX < MaxSpeed) 
				{
	        		if(velocityX <= 0) 
	        		{
	        			velocityX += 10000F;
	        		}
	        		else
	        		{
	        		velocityX += 7500F ;
	        		}
	        	}
				InputRight = false;
			}
			if(InputUp && (collisions.isPlayerInBlock || collisions.isPlayerOnMover) && velocityY <= 0) 
			{
	    		velocityY = 500;
	    		collisions.isPlayerInBlock = false;
	    		collisions.isPlayerOnMover = false;
			}
			InputUp = false;
		}
		velocityX *= Friction*delta;
		playerY += velocityY*delta;
		if ((collisions.WallLeft && velocityX < 0) ||(collisions.WallRight && velocityX > 0)) {
			velocityX = 0;
		}
		else {
			playerX += velocityX*delta;
			}
		
		if(playerX < 0) {
			playerX = 0;
		}
		collisions.isPlayerOnMover = false;
	}
	
	private void handleInput() {
		for (int k: leftKey){
			if(Gdx.input.isKeyPressed(k)){
				if(facingLeft == false) {
					playerFreedom = playerX;
				}
				InputLeft = true;
				facingLeft = true;
				if(Math.abs(playerFreedom-playerX) > 20) {
					if(lerpedTime == 1F || lerpB == (viewWidth/5)) {
						lerpedTime = 0F;
						lerpA = camX;
						lerpB = -((viewWidth/5));
					}
				}
			}
		}
		for (int k: rightKey){
			if(Gdx.input.isKeyPressed(k)) {
				if(facingLeft == true) {
					playerFreedom = playerX;
				}
				InputRight = true;
				facingLeft = false;
				if(Math.abs(playerFreedom-playerX) > 20) {
					if(lerpedTime == 1F || lerpB == -((viewWidth/5))) {
						lerpedTime = 0F;
						lerpA = camX;
						lerpB = (viewWidth/5);
					}
				}
			}
		}
		for (int k: upKey){
			if(Gdx.input.isKeyPressed(k)) {
				InputUp = true;
			}
		}
	}

}
