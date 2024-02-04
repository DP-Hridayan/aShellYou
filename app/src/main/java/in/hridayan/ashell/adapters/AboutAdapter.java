package in.hridayan.ashell.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.materialswitch.MaterialSwitch;
import in.hridayan.ashell.R;
import in.hridayan.ashell.activities.AboutActivity;
import in.hridayan.ashell.activities.ChangelogActivity;
import in.hridayan.ashell.activities.ExamplesActivity;
import in.hridayan.ashell.activities.StartActivity;
import in.hridayan.ashell.utils.AboutItem;
import in.hridayan.ashell.utils.Utils;
import java.util.List;

public class AboutAdapter extends RecyclerView.Adapter<AboutAdapter.ViewHolder> {

  private List<AboutItem> aboutList;
  private Context context;

  public AboutAdapter(List<AboutItem> aboutList, Context context) {
    this.aboutList = aboutList;
    this.context = context;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_about, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    AboutItem aboutItem = aboutList.get(position);
    Drawable symbolDrawable = aboutItem.getSymbol(context);
    holder.symbolImageView.setImageDrawable(symbolDrawable);
    holder.titleTextView.setText(aboutItem.getTitle());
    holder.descriptionTextView.setText(aboutItem.getDescription());

    View.OnClickListener clickListener =
        v -> {
          String title = aboutItem.getTitle();
          Intent intent;

          switch (title) {
            case "Report an issue":
            case "Feature request":
              intent = new Intent(Intent.ACTION_SENDTO);
              intent.setData(Uri.parse("mailto:hridayanofficial@gmail.com"));

              break;

            case "Examples":
              intent = new Intent(context, ExamplesActivity.class);
              break;

            case "About":
              intent = new Intent(context, AboutActivity.class);
              break;
            case "Github":
              intent = new Intent(Intent.ACTION_VIEW);
              intent.setData(Uri.parse("https://github.com/DP-Hridayan/ashell"));
              break;
            default:
              return;
          }

          context.startActivity(intent);
        };
    holder.titleTextView.setOnClickListener(clickListener);
    holder.descriptionTextView.setOnClickListener(clickListener);
  }

  @Override
  public int getItemCount() {
    return aboutList.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    ImageView symbolImageView;
    TextView titleTextView;
    TextView descriptionTextView;

    public ViewHolder(View itemView) {
      super(itemView);
      symbolImageView = itemView.findViewById(R.id.symbol_image_view);
      titleTextView = itemView.findViewById(R.id.about_title);
      descriptionTextView = itemView.findViewById(R.id.about_description);
    }
  }
}
