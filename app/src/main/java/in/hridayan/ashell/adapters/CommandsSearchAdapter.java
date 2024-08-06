package in.hridayan.ashell.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

import in.hridayan.ashell.R;
import in.hridayan.ashell.utils.CommandItems;
import in.hridayan.ashell.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 08, 2022
 */

public class CommandsSearchAdapter extends RecyclerView.Adapter<CommandsSearchAdapter.ViewHolder> {

  private final List<CommandItems> data;
  private final Context context;
  private final UseCommandInSearchListener useCommandListener;

  public CommandsSearchAdapter(@Nullable List<CommandItems> data, Context context, UseCommandInSearchListener listener) {
    this.data = data != null ? data : new ArrayList<>();
    this.context = context;
    this.useCommandListener = listener;
  }

  public interface UseCommandInSearchListener {
    void useCommandInSearch(String text);
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_examples, parent, false);
    return new ViewHolder(rowItem);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    CommandItems item = data.get(position);
    holder.mTitle.setText(item.getTitle());

    if (item.getSummary() != null) {
      holder.mSummary.setText(item.getSummary());
      holder.mSummary.setVisibility(View.VISIBLE);
    } else {
      holder.mSummary.setVisibility(View.GONE);
    }

    setMargins(holder.itemView, position);
  }

  @Override
  public int getItemCount() {
    return data.size();
  }

  private void setMargins(View view, int position) {
    int paddingInPixels = (int) Utils.convertDpToPixel(30, context);
    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

    if (position == data.size() - 1 && data.size() != 1) {
      layoutParams.bottomMargin = paddingInPixels;
    } else if (position == 0 || data.size() == 1) {
      layoutParams.topMargin = paddingInPixels;
    } else {
      layoutParams.bottomMargin = 30;
    }

    view.setLayoutParams(layoutParams);
  }

  public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final MaterialTextView mTitle;
    private final MaterialTextView mSummary;

    public ViewHolder(View view) {
      super(view);
      mTitle = view.findViewById(R.id.title);
      mSummary = view.findViewById(R.id.summary);
      view.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
      CommandItems item = data.get(getAdapterPosition());
      if (item.getExample() != null) {
        String sanitizedText = sanitizeText(item.getTitle());
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.example)
                .setMessage(item.getExample())
                .setPositiveButton(R.string.use, (dialogInterface, i) -> {
                  item.setUseCounter(item.getUseCounter() + 1);
                  useCommandListener.useCommandInSearch(sanitizedText);
                })
                .setNegativeButton(R.string.copy, (dialogInterface, i) -> Utils.copyToClipboard(sanitizedText, context))
                .show();
      }
    }

    private String sanitizeText(String text) {
      return text.replaceAll("<[^>]*>", "").trim();
    }
  }
}
