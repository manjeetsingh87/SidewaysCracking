package multicolumn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Registry {
    Map<String, Map<String, CrackerMap>> mapSets = new HashMap<>();
    Map<String, Map<String, CrackerTape>> mapTape = new HashMap<>();


    // sideways.select(A, v1, v2, B)
    public Iterator scan(String colA, long low, long high, String colB) {
        CrackerMap crackerMap = mapSets.get(colA).get(colB);
        return null;
    }

    // multi-projection queries, using multiple CrackerMaps
    // Select B, C from R where A < k, needs M-AB and M-AC
    public Iterator scan(String colA, long low, long high, String... colB) {
        return null;
    }

    // For multi-select queries, using multiple CrackerMaps and BitVectors.
    // Ex: SELECT D from R where 3<A<10 AND 4<B<9 AND 1<C<8
    public Iterator scan(String col, String colA, long lowA, long highA, String colB, long lowB, long highB) {
        return null;
    }

    public CrackerTape tapeFor() {
        return null;
    }
}
