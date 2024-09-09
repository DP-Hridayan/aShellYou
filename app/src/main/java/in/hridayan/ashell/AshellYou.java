package in.hridayan.ashell;
import android.app.Application;
import android.content.Context;
import java.lang.ref.WeakReference;


public class AshellYou extends Application{
    private static AshellYou instance;
    private static WeakReference<Context> contextReference;

   public void onCreate() {
        super.onCreate();
        instance = this;
        contextReference = new WeakReference<>(getApplicationContext());
    }

    public static Context getAppContext() {
        if (contextReference == null || contextReference.get() == null) {
            contextReference = new WeakReference<>(AshellYou.getInstance().getApplicationContext());
        }
        return contextReference.get();
    }

    private static AshellYou getInstance() {
        if (instance == null) {
            instance = new AshellYou();
        }
        return instance;
    }

}
