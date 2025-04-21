package in.hridayan.ashell.fragments.home;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.transition.MaterialContainerTransform;

import in.hridayan.ashell.databinding.FragmentScriptExecutorBinding;

public class ScriptExecutorFragment extends Fragment {

  private Uri selectedScriptUri;
  private FragmentScriptExecutorBinding binding;
  private Context context;

  private final ActivityResultLauncher<Intent> pickScriptLauncher =
      registerForActivityResult(
          new ActivityResultContracts.StartActivityForResult(),
          result -> {
            if (result.getResultCode() == android.app.Activity.RESULT_OK
                && result.getData() != null) {
              Uri uri = result.getData().getData();
              if (uri != null && isShellScript(uri)) {
                selectedScriptUri = uri;
                Toast.makeText(requireContext(), "Shell script selected!", Toast.LENGTH_SHORT)
                    .show();
                // Ready to execute
              } else {
                Toast.makeText(
                        requireContext(), "Please select a valid .sh file", Toast.LENGTH_SHORT)
                    .show();
              }
            }
          });

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    binding = FragmentScriptExecutorBinding.inflate(inflater, container, false);

    setExitTransition(null);
    setSharedElementEnterTransition(new MaterialContainerTransform());

    context = requireContext();

    binding.pickButton.setOnClickListener(v -> openFilePicker());

    return binding.getRoot();
  }

  private void openFilePicker() {
    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    intent.setType("*/*");
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    pickScriptLauncher.launch(Intent.createChooser(intent, "Select a Shell Script"));
  }

  private boolean isShellScript(@NonNull Uri uri) {
    String fileName = getFileNameFromUri(uri);
    return fileName != null && fileName.toLowerCase().endsWith(".sh");
  }

  private @Nullable String getFileNameFromUri(@NonNull Uri uri) {
    String result = null;
    if (uri.getScheme() != null && uri.getScheme().equals("content")) {
      try (android.database.Cursor cursor =
          context.getContentResolver().query(uri, null, null, null, null)) {
        if (cursor != null && cursor.moveToFirst()) {
          int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
          if (nameIndex != -1) {
            result = cursor.getString(nameIndex);
          }
        }
      }
    }
    if (result == null) {
      result = uri.getLastPathSegment();
    }
    return result;
  }
}
