
public class Position {

	public float x;
	public float y;
	
	public Position() {
		this.x = 0;
		this.y = 0;
	}
	
	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Position(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public static float distance(Position p1, Position p2) {
		float xDiff = p1.x - p2.x;
		float yDiff = p1.y - p2.y;
		float distance = (float) Math.sqrt(Math.pow(xDiff, 2) + Math.pow(yDiff, 2));
		return distance;
	}
	
	public static float orientation(Position p1, Position p2) {
		float xDiff = p1.x - p2.x;
		float yDiff = p1.y - p2.y;

		return (float) (Math.atan(xDiff / -yDiff) / Math.PI * 180);
		
	}

}
