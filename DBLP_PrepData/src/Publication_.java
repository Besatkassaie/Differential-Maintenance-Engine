import java.util.HashMap;
import java.util.Map;

/*
 * Created on 20.11.2008
 */

public class Publication_ {
    private static Map<String, Publication_> publicationMap = new HashMap<String, Publication_>();

    private String key;

    private Publication_(String key) {
        this.key = key;
        publicationMap.put(key, this);
    }


    static public Publication_ create(String key) {
        Publication_ p;
        p = searchPublication(key);
        if (p == null)
            p = new Publication_(key);
        return p;
    }

    static public Publication_ searchPublication(String key) {
        return publicationMap.get(key);
    }
}