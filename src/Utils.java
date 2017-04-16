/**
 * Created by nadir on 13.04.2017.
 */
public class Utils {
    static boolean isEmpty(String string) {
        if (string.isEmpty())
            return true;
        else {
            for (int i = 0; i < string.length(); i++) {
                if(string.charAt(i) != ' ')
                    return false;
            }
            return true;
        }
    }

    static boolean contains(String container, String[] chars){
        for(String c : chars){
            if(container.contains(c))
                return true;
        }
        return false;
    }

    static boolean containsOnly(String[] container, String only) {
        if(container.length > 1)
            return false;
        else if(container[0].equals(only))
            return true;
        return false;
    }
}
