package de.trundicho.timeclockstamper.ui.main;

import android.content.res.Configuration;
import android.view.View;

class NightModeChecker {

    public static boolean isDarkMode(View view) {
        boolean isDarkMode = false;
        int currentNightMode = view.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                isDarkMode = true;
                break;
        }
        return isDarkMode;
    }

}
