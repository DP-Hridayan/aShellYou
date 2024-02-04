package in.hridayan.ashell.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import in.hridayan.ashell.R;
import in.hridayan.ashell.utils.ChangelogItem;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_changelog, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ChangelogItem changelogItem = changelogList.get(position);

        holder.titleTextView.setText(changelogItem.getTitle());
        holder.descriptionTextView.setText(changelogItem.getDescription());
    }

    @Override
    public int getItemCount() {
        return changelogList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView;
        TextView descriptionTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.changelog_title);
            descriptionTextView = itemView.findViewById(R.id.changelog_description);
        }
    }
}