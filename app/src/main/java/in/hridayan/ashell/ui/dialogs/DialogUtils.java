package in.hridayan.ashell.ui.dialogs;

import android.content.Context;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DialogUtils {
    public static View inflateDialogView(Context context, int layoutRes) {
        return LayoutInflater.from(context).inflate(layoutRes, null);
    }

    public static AlertDialog createDialog(Context context, View view) {
        return new MaterialAlertDialogBuilder(context).setView(view).show();
    }
}