package in.sunilpaulmathew.ashell.adapters;

import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import in.sunilpaulmathew.ashell.R;
import in.sunilpaulmathew.ashell.utils.CommandItems;
import in.sunilpaulmathew.ashell.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 05, 2022
 */
public class CommandsAdapter extends RecyclerView.Adapter<CommandsAdapter.ViewHolder> {

    private final List<CommandItems> data;

    private static ClickListener mClickListener;

    public CommandsAdapter(List<CommandItems> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public CommandsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_commands, parent, false);
        return new CommandsAdapter.ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull CommandsAdapter.ViewHolder holder, int position) {
        holder.mTitle.setText(this.data.get(position).getTitle());
        if (this.data.get(position).getSummary() != null) {
            holder.mSummary.setText(this.data.get(position).getSummary());
        } else {
            holder.mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            holder.mTitle.setTypeface(null, Typeface.BOLD_ITALIC);
            holder.mSummary.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final MaterialTextView mTitle, mSummary;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mTitle = view.findViewById(R.id.title);
            this.mSummary = view.findViewById(R.id.summary);
        }

        @Override
        public void onClick(View view) {
            mClickListener.onItemClick(this.mTitle.getText().toString(), view);
        }
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        CommandsAdapter.mClickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(String command, View v);
    }

}