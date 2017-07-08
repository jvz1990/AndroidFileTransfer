package com.johanvz.Utils;

import java.io.File;

/**
 * Created by j on 29/06/2017.
 */
public final class CreateDirectory {

    private CreateDirectory() {
    }

    public static boolean CreatedDirectory(String path) {
        boolean success = true;

        File file = new File(path);
        if(!file.exists()) {
            success = file.mkdir();
        }

        return success;
    }
}
