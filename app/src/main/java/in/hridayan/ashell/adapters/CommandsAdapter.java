package in.hridayan.ashell.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import in.hridayan.ashell.R;
import in.hridayan.ashell.items.CommandItems;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 05, 2022
 */
public class CommandsAdapter extends RecyclerView.Adapter<CommandsAdapter.ViewHolder> {

    private final List<CommandItems> data;
    private ClickListener mClickListener;

    public CommandsAdapter(List<CommandItems> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_commands, parent, false);
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
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        this.mClickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(String command, View v);
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
            if (mClickListener != null) {
                mClickListener.onItemClick(mTitle.getText().toString(), view);
            }
        }
    }
}
