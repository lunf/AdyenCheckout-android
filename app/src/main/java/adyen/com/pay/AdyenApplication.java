package adyen.com.pay;

import android.app.Application;

/**
 * For some application which needs to extend Android Application
 */

public class AdyenApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initLogging();
    }

    private void initLogging(){
        // Whatever logging system that need to be called
    }
}
