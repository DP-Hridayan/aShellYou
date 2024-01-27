package in.hridayan.ashell.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import in.hridayan.ashell.R;
import in.hridayan.ashell.models.ChangelogItem;

public class ChangelogAdapter extends RecyclerView.Adapter<ChangelogAdapter.ChangelogViewHolder> {

    private List<ChangelogItem> changelogItems;

    public ChangelogAdapter(List<ChangelogItem> changelogItems) {
        this.changelogItems = changelogItems;
    }

    @NonNull
    @Override
    public ChangelogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_changelog, parent, false);
        return new ChangelogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChangelogViewHolder holder, int position) {
        ChangelogItem changelogItem = changelogItems.get(position);
        holder.descriptionTextView.setText(changelogItem.getDescription());
    }

    @Override
    public int getItemCount() {
        return changelogItems.size();
    }

    static class ChangelogViewHolder extends RecyclerView.ViewHolder {
        TextView descriptionTextView;

        public ChangelogViewHolder(@NonNull View itemView) {
            super(itemView);
            descriptionTextView = itemView.findViewById(R.id.textViewDescription);
        }
    }
}