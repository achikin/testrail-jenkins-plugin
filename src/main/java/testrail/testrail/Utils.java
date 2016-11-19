package testrail.testrail;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by achikin on 7/31/14.
 */
public class Utils {
    private static final Logger LOGGER = Logger.getLogger("TestRailNotifier");
    public static void log(Object... objects) {
        LOGGER.log(Level.WARNING, Arrays.toString(objects));
    }
}
