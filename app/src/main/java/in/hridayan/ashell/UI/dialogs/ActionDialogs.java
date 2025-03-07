package in.hridayan.ashell.UI.dialogs;

import android.content.Context;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import in.hridayan.ashell.R;
import in.hridayan.ashell.config.Preferences;
import in.hridayan.ashell.utils.Utils;
import java.util.List;

/**
 * Handles dialogs for bookmark-related actions, such as viewing, sorting, and deleting bookmarks.
 */
public class ActionDialogs {

    /**
     * Displays a dialog listing all bookmarked items.
     */
    public static void bookmarksDialog(Context context, TextInputEditText mCommand, TextInputLayout mCommandInput) {
        List<String> bookmarks = Utils.getBookmarks(context);
        int totalBookmarks = bookmarks.size();

        String title = context.getString(R.string.bookmarks) + " (" + totalBookmarks + ")";
        CharSequence[] bookmarkItems = bookmarks.toArray(new CharSequence[0]);

        new MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setItems(bookmarkItems, (dialog, which) -> {
                mCommand.setText(bookmarks.get(which));
                mCommand.setSelection(mCommand.getText().length());
            })
            .setPositiveButton(context.getString(R.string.cancel), null)
            .setNegativeButton(context.getString(R.string.sort), (dialog, i) -> sortingDialog(context, mCommand, mCommandInput))
            .setNeutralButton(context.getString(R.string.delete_all), (dialog, i) -> deleteDialog(context, mCommand, mCommandInput))
            .show();
    }

    /**
     * Displays a confirmation dialog before deleting all bookmarks.
     */
    public static void deleteDialog(Context context, TextInputEditText mCommand, TextInputLayout mCommandInput) {
        new MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.confirm_delete))
            .setMessage(context.getString(R.string.confirm_delete_message))
            .setPositiveButton(context.getString(R.string.ok), (dialog, i) -> deleteAllBookmarks(context, mCommand, mCommandInput))
            .setNegativeButton(context.getString(R.string.cancel), (dialog, i) -> bookmarksDialog(context, mCommand, mCommandInput))
            .setOnCancelListener(dialog -> restoreBookmarksDialogIfNotEmpty(context, mCommand, mCommandInput))
            .show();
    }

    /**
     * Displays a sorting options dialog for bookmarks.
     */
    public static void sortingDialog(Context context, TextInputEditText mCommand, TextInputLayout mCommandInput) {
        CharSequence[] sortingOptions = {
            context.getString(R.string.sort_A_Z),
            context.getString(R.string.sort_Z_A),
            context.getString(R.string.sort_newest),
            context.getString(R.string.sort_oldest)
        };

        int currentSortingOption = Preferences.getSortingOption();
        final int[] selectedOption = {currentSortingOption};

        new MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.sort))
            .setSingleChoiceItems(sortingOptions, currentSortingOption, (dialog, which) -> selectedOption[0] = which)
            .setPositiveButton(context.getString(R.string.ok), (dialog, which) -> {
                Preferences.setSortingOption(selectedOption[0]);
                bookmarksDialog(context, mCommand, mCommandInput);
            })
            .setNegativeButton(context.getString(R.string.cancel), (dialog, i) -> bookmarksDialog(context, mCommand, mCommandInput))
            .setOnCancelListener(dialog -> bookmarksDialog(context, mCommand, mCommandInput))
            .show();
    }

    /**
     * Deletes all bookmarks and updates the UI accordingly.
     */
    private static void deleteAllBookmarks(Context context, TextInputEditText mCommand, TextInputLayout mCommandInput) {
        List<String> bookmarks = Utils.getBookmarks(context);
        for (String item : bookmarks) {
            Utils.deleteFromBookmark(item, context);
        }

        if (mCommand.getText().length() != 0) {
            mCommandInput.setEndIconDrawable(R.drawable.ic_add_bookmark);
        } else {
            mCommandInput.setEndIconVisible(false);
        }
    }

    /**
     * Restores the bookmarks dialog if there are still bookmarks after cancellation.
     */
    private static void restoreBookmarksDialogIfNotEmpty(Context context, TextInputEditText mCommand, TextInputLayout mCommandInput) {
        if (!Utils.getBookmarks(context).isEmpty()) {
            bookmarksDialog(context, mCommand, mCommandInput);
        }
    }
}