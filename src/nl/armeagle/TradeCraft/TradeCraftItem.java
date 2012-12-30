package nl.armeagle.TradeCraft;

/**
 * 
 * @author ArmEagle
 * Store item type(ID) and optionally the data bit
 */
public class TradeCraftItem implements Comparable<TradeCraftItem> {
    public int id;
    public short data;
    
    TradeCraftItem(int id) {
        this(id, (short)0);
    }
    TradeCraftItem(int id, int data) {
        this(id, new Integer(data).shortValue());
    }
    TradeCraftItem(int id, short data) {
        this.id = id;
        this.data = data;
    }
    
    /**
     * @param compare
     * @throws NullPointerException if compare is null
     * @return default < 0 > compare values
     */
    @Override public int compareTo(TradeCraftItem compare) {
        if ( this == compare ) {
            return 0;
        }
        if ( this.id < compare.id ) {
            return -1;
        } else if ( this.id > compare.id ) {
            return 1;
        } else {
            if ( this.data < compare.data ) {
                return -1;
            } else if ( this.data > compare.data ) {
                return 1;
            } else {
                return 0;
            }
        }
    }
    @Override public boolean equals(Object compare) {
        return (compare == null ? false : (compare instanceof TradeCraftItem? this.compareTo((TradeCraftItem)compare) == 0 : false));
    }
    @Override public int hashCode() {
        return this.id * 32768 + this.data;
    }

    @Override public String toString() {
        return "TradeCraftItem("+ this.id +";"+ this.data +")";
    }
    public String toShortString() {
        if ( this.data == 0 ) {
            return String.valueOf(this.id);
        } else {
            return this.id +";"+ this.data;
        }
    }
}
