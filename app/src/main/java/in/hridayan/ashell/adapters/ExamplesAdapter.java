package in.hridayan.ashell.adapters;

import static in.hridayan.ashell.utils.Preferences.*;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import in.hridayan.ashell.R;
import in.hridayan.ashell.utils.CommandItems;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.utils.Preferences;
import in.hridayan.ashell.utils.Utils;
import java.util.*;

public class ExamplesAdapter extends RecyclerView.Adapter<ExamplesAdapter.ViewHolder> {
  private final List<CommandItems> data;
  public final List<CommandItems> selectedItems = new ArrayList<>();
  private final Context context;
  private OnItemClickListener listener;
  private final UseCommandListener useCommandListener;

  public ExamplesAdapter(
      List<CommandItems> data, Context context, UseCommandListener useCommandListener) {
    this.data = new ArrayList<>(data);
    this.context = context;
    this.useCommandListener = useCommandListener;
  }

  public void setOnItemClickListener(OnItemClickListener listener) {
    this.listener = listener;
  }

  public interface UseCommandListener {
    void useCommand(String text);
  }

  public interface OnItemClickListener {
    void onItemClick(int position);

    void onItemLongClick(int position);
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View rowItem =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_examples, parent, false);
    return new ViewHolder(rowItem);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    CommandItems currentItem = this.data.get(position);
    holder.bind(currentItem, position);
  }

  @Override
  public int getItemCount() {
    return this.data.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    private final MaterialTextView mTitle, mSummary;
    private final MaterialCardView card;
    private final AppCompatImageButton pin;

    public ViewHolder(View view) {
      super(view);
      this.card = view.findViewById(R.id.commands_card);
      this.mTitle = view.findViewById(R.id.title);
      this.mSummary = view.findViewById(R.id.summary);
      this.pin = view.findViewById(R.id.pin);

      view.setOnClickListener(v -> handleClick());
      view.setOnLongClickListener(
          v -> {
            startItemSelecting();
            return true;
          });
    }

    public void bind(CommandItems item, int position) {
      pin.setVisibility(item.isPinned() ? View.VISIBLE : View.GONE);
      if (Utils.androidVersion() >= Build.VERSION_CODES.S) {
        pin.setColorFilter(Utils.getColor(pinColor(), context));
      }
      card.setStrokeWidth(item.isPinned() ? 3 : 0);
      card.setChecked(item.isChecked());
      itemView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.on_scroll_animator));
      mTitle.setText(item.getTitle());

      if (item.getSummary() != null) {
        mSummary.setText(item.getSummary());
        int paddingInPixels = (int) Utils.convertDpToPixel(50, context);
        ViewGroup.MarginLayoutParams layoutParams =
            (ViewGroup.MarginLayoutParams) itemView.getLayoutParams();
        layoutParams.bottomMargin = position == data.size() - 1 ? paddingInPixels : 30;
        itemView.setLayoutParams(layoutParams);
        translatePinIcon(card.isChecked(), pin);
      }
    }

    private void handleClick() {
      int position = getAdapterPosition();
      if (position != RecyclerView.NO_POSITION) {
        CommandItems item = data.get(position);
        if (item.getExample() != null && selectedItems.isEmpty()) showExampleDialog(item);
        else startItemSelecting();
      }
    }

    private void showExampleDialog(CommandItems item) {
      HapticUtils.weakVibrate(itemView, context);
      String sanitizedText = sanitizeText(item.getTitle());
      new MaterialAlertDialogBuilder(context)
          .setTitle(R.string.example)
          .setMessage(item.getExample())
          .setPositiveButton(
              R.string.use,
              (dialog, which) -> {
                item.setUseCounter(item.getUseCounter() + 1);
                useCommandListener.useCommand(sanitizedText);
              })
          .setNegativeButton(
              R.string.copy, (dialog, which) -> Utils.copyToClipboard(sanitizedText, context))
          .show();
    }

    private void startItemSelecting() {
      int position = getAdapterPosition();
      if (position != RecyclerView.NO_POSITION) {
        card.setChecked(!card.isChecked());
        CommandItems item = data.get(position);
        item.setChecked(card.isChecked());
        updateSelectedItems(item, card.isChecked());
        listener.onItemLongClick(position);
        translatePinIcon(card.isChecked(), pin);
      }
    }
  }

  private void updateSelectedItems(CommandItems item, boolean isChecked) {
    if (isChecked) {
      if (!selectedItems.contains(item)) selectedItems.add(item);
    } else {
      selectedItems.remove(item);
    }
  }

  public List<CommandItems> getSelectedItems() {
    return selectedItems;
  }

  public int getSelectedItemsSize() {
    return selectedItems.size();
  }

  public void selectAll() {
    for (CommandItems item : data) {
      if (!item.isChecked()) {
        item.setChecked(true);
        updateSelectedItems(item, true);
      }
    }
    notifyDataSetChanged();
  }

  public void deselectAll() {
    for (CommandItems item : data) {
      if (item.isChecked()) {
        item.setChecked(false);
        updateSelectedItems(item, false);
      }
    }
    notifyDataSetChanged();
  }

  public void addSelectedToBookmarks() {
    Set<String> bookmarksSet = new HashSet<>(Utils.getBookmarks(context));
    for (CommandItems item : selectedItems) {
      String command = sanitizeText(item.getTitle());
      if (!bookmarksSet.contains(command)) Utils.addToBookmark(command, context);
    }
  }

  public void deleteSelectedFromBookmarks() {
    for (CommandItems item : selectedItems) {
      Utils.deleteFromBookmark(sanitizeText(item.getTitle()), context);
    }
  }

  public boolean isAllItemsBookmarked() {
    for (CommandItems item : selectedItems) {
      if (!Utils.isBookmarked(sanitizeText(item.getTitle()), context)) return false;
    }
    return true;
  }

  public boolean isAllItemsPinned() {
    for (CommandItems item : selectedItems) {
      if (!item.isPinned()) return false;
    }
    return true;
  }

  public String sanitizeText(String text) {
    return text.replaceAll("<[^>]*>", "").trim();
  }

  public void pinUnpinSelectedItems(boolean isAllPinned) {

    if (!selectedItems.isEmpty()) {
      List<CommandItems> pinnedItems = new ArrayList<>(selectedItems);

      for (CommandItems item : pinnedItems) {
        if (!isAllPinned) {
          item.setPinned(true);
          data.remove(item);
          data.add(0, item);
        } else {
          item.setPinned(false);
        }
        sortData();
        item.setChecked(false);
        selectedItems.remove(item);
      }
      notifyDataSetChanged();
    }
  }

  public void sortData() {
    data.sort(
        (item1, item2) -> {
          if (item1.isPinned() != item2.isPinned()) {
            return Boolean.compare(item2.isPinned(), item1.isPinned());
          }
          int sortOption = Preferences.getSortingExamples(context);
          switch (sortOption) {
            case SORT_A_TO_Z:
              return item1.getTitle().compareToIgnoreCase(item2.getTitle());
            case SORT_Z_TO_A:
              return item2.getTitle().compareToIgnoreCase(item1.getTitle());
            case SORT_MOST_USED:
            case SORT_LEAST_USED:
              int counterCompare = Integer.compare(item2.getUseCounter(), item1.getUseCounter());
              return sortOption == SORT_MOST_USED ? counterCompare : -counterCompare;
            default:
              return 0;
          }
        });
    notifyDataSetChanged();
  }

  private int pinColor() {
    int currentMode =
        context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
    return currentMode == Configuration.UI_MODE_NIGHT_YES
        ? android.R.color.system_accent3_100
        : android.R.color.system_accent3_500;
  }

  private void translatePinIcon(boolean isChecked, AppCompatImageButton pin) {
    float translationX = isChecked ? -Utils.convertDpToPixel(30, context) : 0;
    if (pin.getTranslationX() != translationX) {
      ObjectAnimator.ofFloat(pin, "translationX", translationX).setDuration(350).start();
    }
  }
}
