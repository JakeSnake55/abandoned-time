package snakeindustries.abandonedtime.game;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.utils.Array;

public class Collisions {
	int tileSize;
	boolean isPlayerInBlock;
	boolean isPlayerOnMover;
	boolean spiked;
	boolean WallLeft;
	boolean WallRight;
	
	Mover usedMover;
	int rotation;
	
		// TODO Fix wallstand glitch
		public void colliding(float delta, float playerY, float playerX, boolean facingLeft, TiledMap tileMap, Array<Mover> movers) {
			
			
			TiledMapTileLayer layer = 
					(TiledMapTileLayer)   tileMap.getLayers().get("Platforms");
			tileSize = layer.getTileWidth();
			int playerCellFloorY = (int) ((playerY-1)/tileSize) ;
			int playerCellFloorX1 = (int) ((playerX+4)/tileSize) ;
			int playerCellFloorX2 = (int) ((playerX+12)/tileSize) ;
			
			int playerCellWallLeft = (int) ((playerX+3)/tileSize);
			int playerCellWallRight = (int) ((playerX+13)/tileSize);
			int YOffsets[] = {
					0, 15, 31
			};
			
			isPlayerInBlock = false;
			
			for (Mover mover : movers) {
				if (mover.Intersects((playerX+4), playerY)|| mover.Intersects((playerX+12), playerY)) {
					usedMover = mover;
					isPlayerOnMover = true;
					isPlayerInBlock = true;
				}
			}
			Cell cell = layer.getCell(playerCellFloorX1, playerCellFloorY);
			if(cell != null && cell.getTile() != null ) {
				isPlayerInBlock = true;
				if(cell.getTile().getId() == 3) {
					spiked  = true;
				}
			}
			cell = layer.getCell(playerCellFloorX2, playerCellFloorY);
			if(cell != null && cell.getTile() != null) {
				isPlayerInBlock = true;
				if(cell.getTile().getId() == 3) {
					spiked  = true;
					
				}
			}
			
			WallLeft = false;
			for (int y : YOffsets) {
				cell = layer.getCell(playerCellWallLeft, (int) ((playerY+y)/tileSize));
				if (cell != null && cell.getTile() != null) {
					if(cell.getTile().getId() == GameScreen.DIRT) {
						WallLeft = true;
					}
				}
			}
			
			WallRight = false;
			for (int y : YOffsets) {
				cell = layer.getCell(playerCellWallRight, (int) ((playerY+y)/tileSize));
				if (cell != null && cell.getTile() != null) {
					if(cell.getTile().getId() == GameScreen.DIRT) {
						WallRight = true;
					}
				}
			}
			
			if(spiked) {
				if(facingLeft) {
					rotation = 90;
				} 
				else {
					rotation = 270;
				}
			}
			
		}
}
