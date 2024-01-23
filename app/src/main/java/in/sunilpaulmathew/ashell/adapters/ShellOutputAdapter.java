package in.sunilpaulmathew.ashell.adapters;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import in.sunilpaulmathew.ashell.R;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 09, 2022
 */
public class ShellOutputAdapter extends RecyclerView.Adapter<ShellOutputAdapter.ViewHolder> {

    private final List<String> data;

    public ShellOutputAdapter(List<String> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ShellOutputAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_shell_output, parent, false);
        return new ShellOutputAdapter.ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ShellOutputAdapter.ViewHolder holder, int position) {
        holder.mOutput.setText(Html.fromHtml(this.data.get(position), Html.FROM_HTML_MODE_LEGACY));
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView mOutput;

        public ViewHolder(View view) {
            super(view);
            this.mOutput = view.findViewById(R.id.shell_output);
        }
    }

}