import android.app.Application;

import net.gotev.uploadservice.BuildConfig;
import net.gotev.uploadservice.UploadService;

/**
 * Created by Paul on 07/02/2018.
 */

public class Initializer extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //setup the broadcast actionnamespace string which will
        // be used to notiby upload status
        // Gradle automatically generates proper variable as below
        //UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
        //Or, you can define it manually
        UploadService.NAMESPACE = "ro.duoline.cateringsettings";
    }
}
