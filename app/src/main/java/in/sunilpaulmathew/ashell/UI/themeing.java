package in.sunilpaulmathew.ashell;

import android.app.Application;
import com.google.android.material.color.DynamicColors;

public class themeing extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}