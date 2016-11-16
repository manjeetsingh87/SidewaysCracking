package multicolumn;

/**
 * Stores a position 'p' referring to the cracker column.
 * all values before position p are smaller than v and all values after p are greater.
 */

public class Piece<H extends Comparable<H>> {
    public H value;                             // value in head
    public int position;                        // position of value in head
    // public boolean leftInc, rightInc;           // <= left-inclusive? or >= right-inclusive

    //public Piece(H value, int position, boolean leftInc, boolean rightInc) {
    public Piece(H value, int position) {
        this.value = value;
        this.position = position;
//        this.leftInc = leftInc;
//        this.rightInc = rightInc;
    }

    public H getValue() {
        return value;
    }

    public int getPosition() {
        return position;
    }
}