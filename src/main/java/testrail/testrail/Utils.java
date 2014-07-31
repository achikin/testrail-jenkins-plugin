package testrail.testrail;

import java.util.Arrays;

/**
 * Created by achikin on 7/31/14.
 */
public class Utils {
    public static void log(Object... objects) {
        System.console().printf(Arrays.toString(objects));
        System.console().printf("\n");
    }
}
