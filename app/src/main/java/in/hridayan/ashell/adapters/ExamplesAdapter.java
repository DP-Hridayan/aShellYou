package in.hridayan.ashell.adapters;

import static in.hridayan.ashell.utils.Preferences.SORT_A_TO_Z;
import static in.hridayan.ashell.utils.Preferences.SORT_LEAST_USED;
import static in.hridayan.ashell.utils.Preferences.SORT_MOST_USED;
import static in.hridayan.ashell.utils.Preferences.SORT_Z_TO_A;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
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
import in.hridayan.ashell.activities.MainActivity;
import in.hridayan.ashell.utils.CommandItems;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.utils.Preferences;
import in.hridayan.ashell.utils.Utils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 08, 2022
 */
public class ExamplesAdapter extends RecyclerView.Adapter<ExamplesAdapter.ViewHolder> {
  public final List<CommandItems> data;
  public List<CommandItems> selectedItems = new ArrayList<>();
  private final Context context;
  private OnItemClickListener listener;

  public ExamplesAdapter(List<CommandItems> data, Context context) {
    this.data = data;
    this.context = context;
  }

  public void setOnItemClickListener(OnItemClickListener listener) {
    this.listener = listener;
  }

  public interface OnItemClickListener {
    void onItemClick(int position);

    void onItemLongClick(int position);
  }

  @NonNull
  @Override
  public ExamplesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View rowItem =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_examples, parent, false);
    return new ExamplesAdapter.ViewHolder(rowItem);
  }

  @Override
  public void onBindViewHolder(@NonNull ExamplesAdapter.ViewHolder holder, int position) {
    CommandItems currentItem = this.data.get(position);
    holder.pin.setVisibility(currentItem.isPinned() ? View.VISIBLE : View.GONE);
    if (Utils.androidVersion() >= Build.VERSION_CODES.S) {
      holder.pin.setColorFilter(Utils.getColor(pinColor(), context));
    }
    holder.card.setStrokeWidth(currentItem.isPinned() ? 3 : 0);
    holder.card.setChecked(currentItem.isChecked());
    holder.itemView.startAnimation(
        AnimationUtils.loadAnimation(context, R.anim.on_scroll_animator));
    holder.mTitle.setText(currentItem.getTitle());
    if (currentItem.getSummary() != null) {
      holder.mSummary.setText(currentItem.getSummary());

      int paddingInPixels = (int) (Utils.convertDpToPixel(50, context));

      ViewGroup.MarginLayoutParams layoutParams =
          (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
      layoutParams.bottomMargin = position == data.size() - 1 ? paddingInPixels : 30;
      holder.itemView.setLayoutParams(layoutParams);
      translatePinIcon(holder.card.isChecked(), holder.pin);
    }
  }

  @Override
  public int getItemCount() {
    return this.data.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder
      implements View.OnClickListener, View.OnLongClickListener {
    private final MaterialTextView mTitle, mSummary;
    private final MaterialCardView card;
    private final AppCompatImageButton pin;

    public ViewHolder(View view) {
      super(view);
      view.setOnClickListener(this);
      view.setOnLongClickListener(this);
      this.card = view.findViewById(R.id.commands_card);
      this.mTitle = view.findViewById(R.id.title);
      this.mSummary = view.findViewById(R.id.summary);
      this.pin = view.findViewById(R.id.pin);
    }

    @Override
    public void onClick(View view) {
      if (data.get(getAdapterPosition()).getExample() != null && !isAtLeastOneItemChecked()) {
        HapticUtils.weakVibrate(view);
        String sanitizedText = sanitizeText(data.get(getAdapterPosition()).getTitle());
        Context context = view.getContext();
        new MaterialAlertDialogBuilder(context)
            .setTitle(R.string.example)
            .setMessage(data.get(getAdapterPosition()).getExample())
            .setPositiveButton(
                R.string.use,
                (dialogInterface, i) -> {
                  int counter = data.get(getAdapterPosition()).getUseCounter();
                  data.get(getAdapterPosition()).setUseCounter(counter + 1);
                  Intent intent = new Intent(context, MainActivity.class);
                  intent.putExtra("use_command", sanitizedText);
                  intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                  context.startActivity(intent);
                })
            .setNegativeButton(
                R.string.copy,
                (dialogInterface, i) -> Utils.copyToClipboard(sanitizedText, context))
            .show();

      } else {
        startItemSelecting();
      }
    }

    @Override
    public boolean onLongClick(View view) {
      startItemSelecting();
      return true;
    }

    public void startItemSelecting() {
      card.setChecked(!card.isChecked());
      data.get(getAdapterPosition()).setChecked(card.isChecked());
      updateSelectedItems(data.get(getAdapterPosition()), card.isChecked());
      listener.onItemLongClick(getAdapterPosition());
      translatePinIcon(card.isChecked(), pin);
    }

    public boolean isAtLeastOneItemChecked() {
      for (CommandItems item : data) {
        if (item.isChecked()) {
          return true;
        }
      }
      return false;
    }
  }

  public void updateSelectedItems(CommandItems item, boolean isChecked) {
    if (isChecked) {
      selectedItems.add(item);
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
      notifyDataSetChanged();
    }
  }

  public void deselectAll() {
    for (CommandItems item : data) {
      if (item.isChecked()) {
        item.setChecked(false);
        updateSelectedItems(item, false);
      }
      notifyDataSetChanged();
    }
  }

  public void addSelectedToBookmarks() {

    int totalItems = selectedItems.size();
    int numBatches = Math.min(totalItems, 5);

    // Determine the batch size
    int batchSize = totalItems / numBatches;
    AtomicInteger counter = new AtomicInteger(0);

    IntStream.range(0, numBatches)
        .parallel()
        .forEach(
            i -> {
              int startIndex = i * batchSize;
              int endIndex = (i == numBatches - 1) ? totalItems : (startIndex + batchSize);
              List<CommandItems> batch = selectedItems.subList(startIndex, endIndex);

              Set<String> bookmarksSet = new HashSet<>(Utils.getBookmarks(context));
              batch.forEach(
                  item -> {
                    String command = sanitizeText(item.getTitle());
                    if (!bookmarksSet.contains(command)) {
                      Utils.addToBookmark(command, context);
                    } else {
                      counter.incrementAndGet();
                    }
                  });
            });
  }

  public void deleteSelectedFromBookmarks() {
    for (CommandItems item : selectedItems) {
      String command = sanitizeText(item.getTitle());
      Utils.deleteFromBookmark(command, context);
    }
  }

  public boolean isAllItemsBookmarked() {
    for (CommandItems item : selectedItems) {
      String command = sanitizeText(item.getTitle());
      if (!Utils.isBookmarked(command, context)) {
        return false;
      }
    }
    return true;
  }

  public boolean isAllItemsPinned() {
    for (CommandItems item : selectedItems) {
      if (!item.isPinned()) {
        return false;
      }
    }
    return true;
  }

  public String sanitizeText(String text) {
    String sanitizedText = text.replaceAll("<[^>]*>", "");
    return sanitizedText.trim();
  }

  public void pinUnpinSelectedItems(boolean isAllPinned) {

    if (!selectedItems.isEmpty()) {
      List<CommandItems> pinnedItems = new ArrayList<>(selectedItems);

      for (CommandItems selectedItem : pinnedItems) {
        if (!isAllPinned) {
          selectedItem.setPinned(true);
          data.remove(selectedItem);
          data.add(0, selectedItem);
        } else {
          selectedItem.setPinned(false);
        }
        sortData();
        selectedItem.setChecked(false);
        updateSelectedItems(selectedItem, false);
      }
      notifyDataSetChanged();
    }
  }

  public void sortData() {
    data.sort((item1, item2) -> {

        if (item1.isPinned() && !item2.isPinned()) {
            return -1;
        } else if (!item1.isPinned() && item2.isPinned()) {
            return 1;
        }

        int sortOption = Preferences.getSortingExamples(context);
        int counter1 = item1.getUseCounter();
        int counter2 = item2.getUseCounter();

        switch (sortOption) {
            case SORT_A_TO_Z:
                return item1.getTitle().compareToIgnoreCase(item2.getTitle());
            case SORT_Z_TO_A:
                return item2.getTitle().compareToIgnoreCase(item1.getTitle());
            case SORT_MOST_USED:
                if (counter1 != counter2) {
                    return counter2 - counter1;
                } else {
                    return item1.getTitle().compareToIgnoreCase(item2.getTitle());
                }
            case SORT_LEAST_USED:
                if (counter1 != counter2) {
                    return counter1 - counter2;
                } else {
                    return item1.getTitle().compareToIgnoreCase(item2.getTitle());
                }

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
      ObjectAnimator animator = ObjectAnimator.ofFloat(pin, "translationX", translationX);
      animator.setDuration(350);
      animator.start();
      pin.setTranslationX(translationX);
    }
  }
}
