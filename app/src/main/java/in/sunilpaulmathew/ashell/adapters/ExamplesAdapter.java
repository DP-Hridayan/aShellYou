package in.sunilpaulmathew.ashell.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import in.sunilpaulmathew.ashell.R;
import in.sunilpaulmathew.ashell.utils.CommandItems;
import in.sunilpaulmathew.ashell.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 08, 2022
 */
public class ExamplesAdapter extends RecyclerView.Adapter<ExamplesAdapter.ViewHolder> {

    private final List<CommandItems> data;

    public ExamplesAdapter(List<CommandItems> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ExamplesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_examples, parent, false);
        return new ExamplesAdapter.ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamplesAdapter.ViewHolder holder, int position) {
        holder.mTitle.setText(this.data.get(position).getTitle());
        if (this.data.get(position).getSummary() != null) {
            holder.mSummary.setText(this.data.get(position).getSummary());
        }
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final MaterialTextView mTitle, mSummary;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mTitle = view.findViewById(R.id.title);
            this.mSummary = view.findViewById(R.id.summary);
        }

        @Override
        public void onClick(View view) {
            if (data.get(getAdapterPosition()).getExample() != null) {
                new MaterialAlertDialogBuilder(view.getContext())
                        .setMessage(data.get(getAdapterPosition()).getExample())
                        .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                        })
                        .setPositiveButton(R.string.copy, (dialogInterface, i) ->
                                Utils.copyToClipboard(data.get(getAdapterPosition()).getExample(), view.getContext())
                        ).show();
            }
        }
    }

}