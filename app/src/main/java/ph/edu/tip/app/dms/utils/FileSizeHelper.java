package ph.edu.tip.app.dms.utils;

import java.text.DecimalFormat;

/**
 * Created by Mark Jansen Calderon on 11/16/2016.
 */

public class FileSizeHelper {

    public static String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));

        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
