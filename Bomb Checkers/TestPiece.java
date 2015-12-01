import static org.junit.Assert.*;

import org.junit.Test;

public class TestPiece{
	public static void main(String[] args) {
		jh61b.junit.textui.runClasses(TestPiece.class); 
	}
	@Test
	public void testPieces(){
		Board b = new Board(true);
		Piece fire = new Piece(true, b, 0, 0, "pawn");
		Piece water = new Piece(false, b, 1, 1, "shield");
		assertEquals(true, fire.isFire());
		assertEquals(false, water.isFire());

		assertEquals(false, fire.isShield());
		assertEquals(true, water.isShield());
		assertEquals(false, fire.isBomb());
		assertEquals(false, water.isBomb());

		assertEquals(0, fire.side());
		assertEquals(1, water.side());

		Piece fire2 = new Piece(true, b, 6, 6, "bomb"); //move makes it a king, not place
		b.place(fire2, 6,6);
		fire2.move(7, 7);
		assertEquals(true, fire2.isKing());
		assertEquals(false, water.isKing());

		
	}
	@Test 
	public void testPlaceAndRemove(){
		Board b = new Board(true);
		Piece fire = new Piece(true, b, 0, 0, "pawn");
		b.place(fire, 0, 0);
		assertEquals(fire, b.pieceAt(0,0));
		assertEquals(null, b.pieceAt(0,3));
		b.remove(0,0);
		assertEquals(null, b.pieceAt(0,0));

		b.place(fire, 5, 5);
		assertEquals(fire, b.pieceAt(5, 5));
		assertEquals(null, b.pieceAt(0,0)); 
		assertEquals(null, b.pieceAt(10,20)); //tests out of bounds

		assertEquals(fire, b.remove(5, 5));
		assertEquals(null, b.pieceAt(5,5));
		assertEquals(null, b.remove(5,5));
		assertEquals(null, b.pieceAt(5,5));
	}

	@Test
	public void testCanSelectAndSelect(){
		Board b = new Board(true);
		Piece fire = new Piece(true, b, 2, 2, "pawn");
		Piece fire2 = new Piece(true, b, 6, 6, "pawn");
		Piece fire3 = new Piece(true, b, 5, 5, "pawn");
		Piece water = new Piece(false, b, 0, 0, "shield");

		b.place(fire,2,2);
		b.place(water,0,0);
		b.place(fire2,6,6);
		b.place(fire3,5,5);

		assertEquals(false, b.canSelect(4,4)); //we must first select a piece
		assertEquals(true, b.canSelect(2,2));
		assertEquals(false, b.canSelect(0,0)); //cannot select the opposite piece

		b.select(2,2);
		assertEquals(true, b.canSelect(2,2)); //no penalty for reselecting the same piece
		assertEquals(true, b.canSelect(3,3)); //we can move to an empty spot
		assertEquals(false, b.canSelect(2,3)); 
		assertEquals(false, b.canSelect(4,4)); //we cannot move 2x unless we capture
		assertEquals(true, b.canSelect(6,6)); //we should be able to change select methods
		assertEquals(true, b.canSelect(1,3)); 

		b.select(3,3);  //should move the piece
		assertEquals(fire, b.pieceAt(3,3)); 
		assertEquals(false, b.canSelect(4,4)); //cannot select again


		b.endTurn(); //switches to water piece
		assertEquals(true, b.canSelect(0,0));
		assertEquals(false, b.canSelect(3,3)); //cannot select fire piece

		b.select(0,0);
		assertEquals(false, b.canSelect(1,1)); // water can only move down		
	}


	@Test 
	public void testMoveandCapture(){
		Board b = new Board(true);
		Piece fire = new Piece(true, b, 0, 0, "pawn");
		Piece water = new Piece(false, b, 1, 1, "pawn");
		Piece water2 = new Piece(false, b, 3, 3, "pawn");

		b.place(fire, 0, 0);
		b.place(water, 1, 1);
		b.place(water2, 3, 3);

		fire.move(2,2);
		assertEquals(true, fire.hasCaptured());
		assertEquals(fire, b.pieceAt(2,2));
		assertEquals(null, b.pieceAt(1,1));

		fire.move(4,4);
		assertEquals(true, fire.hasCaptured());
		assertEquals(fire, b.pieceAt(4,4));
		assertEquals(null, b.pieceAt(3,3));
	}

	@Test
	public void testComplexSelection(){
		Board b = new Board(true);
		Piece water = new Piece(false, b, 4, 4, "pawn");
		Piece fire = new Piece(true, b, 5, 5, "pawn");
		Piece fire2 = new Piece(true, b, 6, 2, "bomb");
		b.place(water, 4, 4);
		b.place(fire, 3, 3);
		b.place(fire2, 6, 2);

		b.endTurn();

		b.select(4,4);
		assertEquals(false, b.canSelect(3,3)); //fire in the way
		assertEquals(false, b.canSelect(3,5)); //water can only move down
		assertEquals(true, b.canSelect(5,3));
		assertEquals(true, b.canSelect(2,2)); //capturing

		b.select(5,3);
		assertEquals(false, b.canSelect(4,5)); //cannot select again
		assertEquals(false, b.canSelect(7,1)); //cannot capture after not capturing
		
		assertEquals(fire2, b.pieceAt(6,2)); 
		b.select(7,1);
		assertEquals(water, b.pieceAt(7,1));
		assertEquals(null, b.pieceAt(6,2));
	}

	@Test
	public void testBombandShield(){
		Board b = new Board(true);
		Piece fire = new Piece(true, b, 5, 5, "bomb");
		Piece water = new Piece(false, b, 4, 6, "shield");
		Piece water2 = new Piece(false, b, 2, 6, "pawn");
		b.place(fire, 5, 5);
		b.place(water, 4, 6);
		b.place(water2, 2, 6);

		fire.move(3,7);
		assertEquals(null, b.pieceAt(4,7));
		assertEquals(null, b.pieceAt(3,6));
		assertEquals(null, b.pieceAt(5,6));

		fire = new Piece(true, b, 4, 4, "bomb");
		water = new Piece(false, b, 3, 5, "shield");
		water2 = new Piece(false, b, 3, 7, "pawn");
		Piece water3 = new Piece(false, b, 1, 7, "pawn");
		Piece water4 = new Piece(false, b, 1, 5, "bomb");

		b.place(fire, 4, 4);
		b.place(water, 3, 5);
		b.place(water2, 3, 7);
		b.place(water3, 1, 7);
		b.place(water4, 1, 5);
		fire.move(2,6);

		assertEquals(null, b.pieceAt(3,5));
		assertEquals(null, b.pieceAt(3,7));
		assertEquals(null, b.pieceAt(1,7));
		assertEquals(null, b.pieceAt(1,5));
	}

	@Test
	public void testKing(){
		Board b = new Board(true);
		Piece water = new Piece(false, b, 2, 2, "bomb");
		Piece fire = new Piece(true, b, 1, 1, "pawn");
		Piece fire2 = new Piece(true, b, 2, 6, "shield");
		b.place(water, 2, 2);
		b.place(fire, 1, 1);
		b.place(fire2, 2, 6);

		assertEquals(false, water.isKing());
		assertEquals(false, fire2.isKing());
		assertEquals(fire, b.pieceAt(1,1));

		fire2.move(3,7);
		assertEquals(true, fire2.isKing());
		assertEquals(true, fire2.isFire());
		assertEquals(fire2, b.pieceAt(3,7));

		water.move(0,0);
		assertEquals(true, water.isKing());
		assertEquals(true, fire2.isKing());
		assertEquals(null, b.pieceAt(1,1));
		assertEquals(null, b.pieceAt(0,0));

		b.select(3,7);
		assertEquals(true, b.canSelect(4,6)); //fire can now move down
		assertEquals(false, b.canSelect(4,8)); //out of bounds

	}

	@Test
	public void testEndingTurn(){
		Board b = new Board(true);
		Piece fire = new Piece(true, b, 0, 0, "pawn");
		Piece water = new Piece(false, b, 1, 1, "shield");
		b.place(fire, 0, 0);
		b.place(water, 1, 1);

		assertEquals(true, b.canSelect(0,0));
		assertEquals(false, b.canSelect(1,1));
		assertEquals(false, b.canEndTurn());

		b.endTurn();
		b.endTurn();
		assertEquals(false, b.canEndTurn());
		assertEquals(true, b.canSelect(0,0));
		assertEquals(false, b.canSelect(1,1));

		b.endTurn();
		assertEquals(false, b.canSelect(0,0));
		assertEquals(true, b.canSelect(1,1));
		
	}

	@Test
	public void testDefaultBoard(){
		Board b = new Board(false);
			
		for (int i = 0; i<8; i+=2) {
			assertEquals(true, b.pieceAt(i,0).isFire());
			assertEquals(true, b.pieceAt(i,2).isBomb());
			assertEquals(true, b.pieceAt(i,6).isShield());
		}
		for (int i = 1; i<8 ; i+=2) {
			assertEquals(true, b.pieceAt(i,1).isShield());
			assertEquals(true, b.pieceAt(i,5).isBomb());
			assertEquals(false, b.pieceAt(i,7).isFire());
		}
	}
	@Test
	public void testWinner(){
		Board b = new Board(true);
		Piece water = new Piece(false, b, 2, 2, "bomb");
		Piece fire = new Piece(true, b, 1, 1, "pawn");
		Piece water2 = new Piece(false, b, 2, 2, "shield");

		b.place(water, 2, 2);
		b.place(fire, 1, 1);
		assertEquals(null, b.winner());

		water.move(0,0);
		assertEquals("No one", b.winner());

		b.place(fire, 1, 1);
		b.place(water2, 2, 2);
		assertEquals(null, b.winner());
		fire.move(3,3);
		assertEquals("fire", b.winner());

		Piece water3 = new Piece(false, b, 4, 4, "pawn");
		b.place(water3, 4, 4);
		assertEquals(null, b.winner());
		water3.move(2,2);
		assertEquals("water", b.winner());
	}
	// @Test
	// public void testValidMove(){
	// 	Board b = new Board(true);
	// 	Piece fire = new Piece(true, b, 0, 0, "pawn");
	// 	Piece water = new Piece(false, b, 1, 1, "pawn");
	// 	b.place(fire, 0, 0);
	// 	b.place(water, 1, 1);
	// 	assertEquals(true, b.validMove(0, 0, 1, 1));
	// 	assertEquals(false, b.validMove(0,0, 1, 2));
	// 	assertEquals(true, b.validMove(0, 0, 2, 2));
	// 	assertEquals(false, b.validMove(0, 0, 3, 3));

	// }
}