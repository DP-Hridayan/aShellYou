package in.hridayan.ashell.UI.bottomsheets;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.ToastUtils;
import in.hridayan.ashell.fragments.home.WifiAdbFragment;
import in.hridayan.ashell.shell.WifiAdbShell;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.utils.Utils;

/** Handles the bottom sheet UI for pairing and connecting to ADB over WiFi. */
public class WifiAdbBottomSheet {

  /**
   * Displays the bottom sheet dialog for pairing and connecting ADB over WiFi.
   *
   * @param context The application context.
   * @param activity The parent activity to which the dialog is attached.
   */
  public static void showPairAndConnectBottomSheet(Context context, Activity activity) {
    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity);
    View bottomSheetView =
        LayoutInflater.from(activity).inflate(R.layout.bottom_sheet_pair_and_connect, null);
    bottomSheetDialog.setContentView(bottomSheetView);
    bottomSheetDialog.show();

    // Initialize UI elements and set up event listeners
    initializeViewsAndSetupListeners(context, activity, bottomSheetDialog, bottomSheetView);
  }

  /**
   * Initializes UI components and sets up event listeners for buttons and input fields.
   *
   * @param context Application context.
   * @param activity Parent activity.
   * @param bottomSheetDialog The bottom sheet dialog instance.
   * @param bottomSheetView The inflated bottom sheet layout.
   */
  private static void initializeViewsAndSetupListeners(
      Context context,
      Activity activity,
      BottomSheetDialog bottomSheetDialog,
      View bottomSheetView) {

    // Retrieve UI elements from the layout
    MaterialButton pairButton = bottomSheetView.findViewById(R.id.pair_button);
    MaterialButton connectButton = bottomSheetView.findViewById(R.id.connect_button);
    TextInputEditText pairingIPEditText = bottomSheetView.findViewById(R.id.ipAddressPairEditText);
    TextInputEditText pairingPortEditText = bottomSheetView.findViewById(R.id.pairingPortEditText);
    TextInputEditText pairingCodeEditText = bottomSheetView.findViewById(R.id.pairingCodeEditText);
    TextInputEditText connectingIPEditText =
        bottomSheetView.findViewById(R.id.ipAddressConnectEditText);
    TextInputEditText connectingPortEditText =
        bottomSheetView.findViewById(R.id.connectPortEditText);
    TextInputLayout pairingIPLayout = bottomSheetView.findViewById(R.id.ipAddressPairInputLayout);
    TextInputLayout pairingPortLayout = bottomSheetView.findViewById(R.id.pairingPortInputLayout);
    TextInputLayout pairingCodeLayout = bottomSheetView.findViewById(R.id.pairingCodeInputLayout);
    TextInputLayout connectingIPLayout =
        bottomSheetView.findViewById(R.id.ipAddressConnectInputLayout);
    TextInputLayout connectingPortLayout =
        bottomSheetView.findViewById(R.id.connectPortInputLayout);
    LinearLayout connectLayout = bottomSheetView.findViewById(R.id.connectLayout);
    LottieAnimationView pairLoadingAnim = bottomSheetView.findViewById(R.id.loading_anim_on_pair);
    LottieAnimationView connectLoadingAnim =
        bottomSheetView.findViewById(R.id.loading_anim_on_connect);

    // Setup real-time input validation
    setupInputValidation(pairingIPEditText, pairingIPLayout);
    setupInputValidation(pairingPortEditText, pairingPortLayout);
    setupInputValidation(pairingCodeEditText, pairingCodeLayout);

    // Set event listeners for Pair and Connect buttons
    pairButton.setOnClickListener(
        v ->
            handlePairing(
                context,
                v,
                pairButton,
                pairLoadingAnim,
                pairingIPEditText,
                pairingPortEditText,
                pairingCodeEditText,
                pairingIPLayout,
                pairingPortLayout,
                pairingCodeLayout,
                connectLayout,
                connectingIPEditText));

    connectButton.setOnClickListener(
        v ->
            handleConnection(
                context,
                v,
                activity,
                bottomSheetDialog,
                connectButton,
                connectLoadingAnim,
                connectingIPEditText,
                connectingPortEditText,
                connectingIPLayout,
                connectingPortLayout));
  }

  /** Adds real-time validation to input fields to remove errors when the user types. */
  private static void setupInputValidation(TextInputEditText editText, TextInputLayout layout) {
    editText.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}

          @Override
          public void afterTextChanged(Editable s) {
            layout.setError(null);
          }
        });
  }

  /** Handles the ADB pairing process. */
  private static void handlePairing(
      Context context,
      View view,
      MaterialButton pairButton,
      LottieAnimationView loadingAnim,
      TextInputEditText ipEditText,
      TextInputEditText portEditText,
      TextInputEditText codeEditText,
      TextInputLayout ipLayout,
      TextInputLayout portLayout,
      TextInputLayout codeLayout,
      LinearLayout connectLayout,
      TextInputEditText connectingIPEditText) {

    String ip = ipEditText.getText().toString().trim();
    String port = portEditText.getText().toString().trim();
    String code = codeEditText.getText().toString().trim();

    if (!validateInputs(context, ip, port, code, ipLayout, portLayout, codeLayout)) return;

    startLoadingAnimation(pairButton, loadingAnim);

    WifiAdbShell.pair(
        context,
        ip,
        port,
        code,
        new WifiAdbShell.PairingCallback() {
          @Override
          public void onSuccess() {
            HapticUtils.weakVibrate(view);
            stopLoadingAnimation(
                pairButton,
                loadingAnim,
                context.getString(R.string.paired),
                Utils.getDrawable(R.drawable.ic_connect, context));
            connectLayout.setVisibility(View.VISIBLE);
            connectingIPEditText.setText(ip);
          }

          @Override
          public void onFailure(String errorMessage) {
            stopLoadingAnimation(
                pairButton,
                loadingAnim,
                context.getString(R.string.pair),
                Utils.getDrawable(R.drawable.ic_connect, context));
            ToastUtils.showToast(
                context, context.getString(R.string.pairing_failed), ToastUtils.LENGTH_SHORT);
          }
        });
  }

  /** Handles the ADB connection process. */
  private static void handleConnection(
      Context context,
      View view,
      Activity activity,
      BottomSheetDialog bottomSheetDialog,
      MaterialButton connectButton,
      LottieAnimationView loadingAnim,
      TextInputEditText ipEditText,
      TextInputEditText portEditText,
      TextInputLayout ipLayout,
      TextInputLayout portLayout) {

    String ip = ipEditText.getText().toString().trim();
    String port = portEditText.getText().toString().trim();

    if (!validateInputs(context, ip, port, null, ipLayout, portLayout, null)) return;

    startLoadingAnimation(connectButton, loadingAnim);

    WifiAdbShell.connect(
        context,
        ip,
        port,
        new WifiAdbShell.ConnectingCallback() {
          @Override
          public void onSuccess() {
            HapticUtils.weakVibrate(view);
            stopLoadingAnimation(
                connectButton,
                loadingAnim,
                context.getString(R.string.connect),
                Utils.getDrawable(R.drawable.ic_connect, context));
            bottomSheetDialog.dismiss();
            ToastUtils.showToast(
                context, context.getString(R.string.connected), ToastUtils.LENGTH_SHORT);
          }

          @Override
          public void onFailure(String errorMessage) {
            stopLoadingAnimation(
                connectButton,
                loadingAnim,
                context.getString(R.string.connect),
                Utils.getDrawable(R.drawable.ic_wireless, context));
            ToastUtils.showToast(
                context, context.getString(R.string.connection_failed), ToastUtils.LENGTH_SHORT);
          }
        });
  }

  private static boolean validateInputs(
      Context context,
      String ip,
      String port,
      String code,
      TextInputLayout ipLayout,
      TextInputLayout portLayout,
      TextInputLayout codeLayout) {
    boolean isValid = true;
    if (ip.isEmpty()) ipLayout.setError(context.getString(R.string.must_fill));
    if (port.isEmpty()) portLayout.setError(context.getString(R.string.must_fill));
    if (codeLayout != null && code.isEmpty())
      codeLayout.setError(context.getString(R.string.must_fill));
    return isValid;
  }

  private static void startLoadingAnimation(
      MaterialButton button, LottieAnimationView animationView) {
    animationView.setVisibility(View.VISIBLE);
    button.setText(null);
    button.setIcon(null);
  }

  private static void stopLoadingAnimation(
      MaterialButton button,
      LottieAnimationView animationView,
      String buttonText,
      Drawable buttonIcon) {
    animationView.setVisibility(View.GONE);
    button.setText(buttonText);
    button.setIcon(buttonIcon);
  }
}
