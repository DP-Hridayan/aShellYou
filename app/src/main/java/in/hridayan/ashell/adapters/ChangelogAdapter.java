package in.hridayan.ashell.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.divider.MaterialDivider;
import in.hridayan.ashell.R;
import in.hridayan.ashell.utils.ChangelogItem;
import java.util.List;
import android.widget.TextView;

public class ChangelogAdapter extends RecyclerView.Adapter<ChangelogAdapter.ViewHolder> {

  private final List<ChangelogItem> changelogList;
  private final Context context;

  public ChangelogAdapter(List<ChangelogItem> changelogList, Context context) {
    this.changelogList = changelogList;
    this.context = context;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_changelog, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    ChangelogItem changelogItem = changelogList.get(position);

    holder.titleTextView.setText(changelogItem.getTitle());
    holder.descriptionTextView.setText(changelogItem.getDescription());
    holder.divider.setVisibility(position == changelogList.size() - 1 ? View.GONE : View.VISIBLE);

    holder.titleTextView.setTextAppearance(context, position == 0 ? R.style.LatestVersionTitle : R.style.OtherVersionTitle);
  }

  @Override
  public int getItemCount() {
    return changelogList.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {

    final TextView titleTextView;
    final TextView descriptionTextView;
    final MaterialDivider divider;

    public ViewHolder(View itemView) {
      super(itemView);
      titleTextView = itemView.findViewById(R.id.changelog_title);
      descriptionTextView = itemView.findViewById(R.id.changelog_description);
      divider = itemView.findViewById(R.id.divider);
    }
  }
}
