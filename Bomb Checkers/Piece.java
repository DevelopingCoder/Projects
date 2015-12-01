public class Piece {
	private boolean hasCaptured = false;
	private boolean isFire;
	private int x,y;
	private String type;
	private Board b;
	private boolean isKing = false;

	public Piece(boolean isFire1, Board b1, int x1, int y1, String type1){
		isFire = isFire1;
		b = b1;
		x = x1;
		y = y1;
		type = type1;

	}
	public boolean isFire(){
		return isFire;
	}
	public int side(){
		if (isFire){
			return 0;
		}
		return 1;
	}
	public boolean isKing(){
		return isKing;
	}
	public boolean isBomb(){
		if (type == "bomb"){
			return true;
		}
		return false;
	}
	public boolean isShield(){
		if (type == "shield"){
			return true;
		}
		return false;
	}
	public void move(int x, int y){ //remove the captured piece
		if ((isFire() && y==7) || (isFire()==false && y==0)) {
        	isKing=true;
        }
		b.place(this,x,y);
		if (x==this.x+2 || x==this.x-2) {
			hasCaptured = true;
		}
		if (hasCaptured()){
			b.remove((this.x+x)/2, (this.y +y)/2);
			if (isBomb()) {
				for(int row = -1; row<=1; row++){
					for(int col = -1; col <= 1; col++){
						Piece exploded = b.pieceAt(x + col, y + row);
						if (exploded!=null && exploded.isShield()==false && (col+x<8 && col+x>=0 && row+y>=0 && row+y<8)) {
							b.remove(x+col, y+row);
						}
					}
				}
			}
		}
		this.x = x;
		this.y = y;
	}
	public boolean hasCaptured(){
		if (hasCaptured == true){
			return true;
		}
		return false;
	}
	public void doneCapturing(){
		hasCaptured=false;
	}
}