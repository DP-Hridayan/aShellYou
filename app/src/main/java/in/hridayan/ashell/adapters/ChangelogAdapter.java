package in.hridayan.ashell.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.content.ContextCompat;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.divider.MaterialDivider;
import in.hridayan.ashell.R;
import in.hridayan.ashell.utils.ChangelogItem;
import java.util.List;

public class ChangelogAdapter extends RecyclerView.Adapter<ChangelogAdapter.ViewHolder> {

  private List<ChangelogItem> changelogList;
  private Context context;

  public ChangelogAdapter(List<ChangelogItem> changelogList, Context context) {
    this.changelogList = changelogList;
    this.context = context;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_changelog, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    ChangelogItem changelogItem = changelogList.get(position);

    holder.titleTextView.setText(changelogItem.getTitle());
    holder.descriptionTextView.setText(changelogItem.getDescription());

    if (position == changelogList.size() - 1) {
      holder.divider.setVisibility(View.GONE);
    } else {
      holder.divider.setVisibility(View.VISIBLE);
    }

    if (position == 0) {
      holder.titleTextView.setTextAppearance(R.style.LatestVersionTitle);
    } else {
      holder.titleTextView.setTextAppearance(R.style.OtherVersionTitle);
    }
  }

  @Override
  public int getItemCount() {
    return changelogList.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {

    TextView titleTextView;
    TextView descriptionTextView;
    MaterialDivider divider;

    public ViewHolder(View itemView) {
      super(itemView);
      titleTextView = itemView.findViewById(R.id.changelog_title);
      descriptionTextView = itemView.findViewById(R.id.changelog_description);
      divider = itemView.findViewById(R.id.divider);
    }
  }
}
