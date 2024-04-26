package in.hridayan.ashell.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.Category;
import in.hridayan.ashell.activities.ChangelogActivity;
import in.hridayan.ashell.utils.Utils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AboutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  private static final int CATEGORY = 0;
  private static final int CATEGORY_LEAD_DEV_ITEM = 1;
  private static final int CATEGORY_CONTRIBUTORS_ITEM = 2;
  private static final int CATEGORY_APP_ITEM = 3;

  private List<Object> items;
  private Context context;

  public AboutAdapter(List<Object> items, Context context) {
    this.items = items;
    this.context = context;
  }

  @Override
  public int getItemViewType(int position) {
    Object item = items.get(position);
    if (item instanceof Category) {
      return CATEGORY;
    } else if (item instanceof Category.LeadDeveloperItem) {
      return CATEGORY_LEAD_DEV_ITEM;
    } else if (item instanceof Category.ContributorsItem) {
      return CATEGORY_CONTRIBUTORS_ITEM;
    } else if (item instanceof Category.AppItem) {
      return CATEGORY_APP_ITEM;
    }
    return -1;
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    switch (viewType) {
            
      case CATEGORY:
        View categoryView = inflater.inflate(R.layout.category_about, parent, false);
        return new CategoryViewHolder(categoryView);
            
            
      case CATEGORY_LEAD_DEV_ITEM:
        View leadDevItemView = inflater.inflate(R.layout.category_lead_dev, parent, false);
        return new LeadDeveloperItemViewHolder(leadDevItemView);
            
            
      case CATEGORY_CONTRIBUTORS_ITEM:
        View contributorsItemView = inflater.inflate(R.layout.category_contributors, parent, false);
        return new contributorsItemViewHolder(contributorsItemView);
            
            
      case CATEGORY_APP_ITEM:
        View appItemView = inflater.inflate(R.layout.category_app, parent, false);
        return new AppItemViewHolder(appItemView);
            
            
      default:
        throw new IllegalArgumentException("Invalid view type");
            
    }
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    Object item = items.get(position);
    if (holder instanceof CategoryViewHolder) {
      Category category = (Category) item;
      CategoryViewHolder categoryViewHolder = (CategoryViewHolder) holder;
      categoryViewHolder.categoryTextView.setText(category.getName());
    } else if (holder instanceof LeadDeveloperItemViewHolder) {
      Category.LeadDeveloperItem categoryAItem = (Category.LeadDeveloperItem) item;
      LeadDeveloperItemViewHolder viewHolder = (LeadDeveloperItemViewHolder) holder;
      viewHolder.imageView.setImageResource(categoryAItem.getImageResource());
      viewHolder.titleTextView.setText(categoryAItem.getTitle());
      viewHolder.descriptionTextView.setText(categoryAItem.getDescription());

      Map<View, String> buttonUrlMap = new HashMap<>();

      buttonUrlMap.put(viewHolder.mXButton, "https://x.com/Spirriy1?t=VCLYRLEN-Pgq_RS2gQU-bg&s=09");
      buttonUrlMap.put(viewHolder.mGithubButton, "https://github.com/DP-Hridayan");
      buttonUrlMap.put(viewHolder.mMailButton, "mailto:hridayanofficial@gmail.com");
      buttonUrlMap.put(viewHolder.mSupportButton, "https://www.buymeacoffee.com/hridayan");

      for (Map.Entry<View, String> entry : buttonUrlMap.entrySet()) {
        entry.getKey().setOnClickListener(v -> Utils.openUrl(context, entry.getValue()));
      }
    } else if (holder instanceof contributorsItemViewHolder) {
      Category.ContributorsItem ContributorsItem = (Category.ContributorsItem) item;
      contributorsItemViewHolder viewHolder = (contributorsItemViewHolder) holder;
      viewHolder.imageView.setImageResource(ContributorsItem.getImageResource());
      viewHolder.titleTextView.setText(ContributorsItem.getTitle());
      viewHolder.descriptionTextView.setText(ContributorsItem.getDescription());
      View.OnClickListener clickListener =
          v -> {
            Map<String, String> idUrlMap = new HashMap<>();

            idUrlMap.put("id_rikka", "https://github.com/RikkaApps/Shizuku");
            idUrlMap.put("id_sunilpaulmathew", "https://gitlab.com/sunilpaulmathew/ashell");
            idUrlMap.put("id_khun_htetz", "https://github.com/KhunHtetzNaing/ADB-OTG");
            idUrlMap.put("id_krishna", "https://github.com/Krishna-G-OP");
            idUrlMap.put("id_drDisagree", "https://github.com/Mahmud0808");
            idUrlMap.put("id_marciozomb13", "https://github.com/marciozomb13");
            idUrlMap.put("id_weiguangtwk", "https://github.com/WeiguangTWK");

            String id = ContributorsItem.getId();
            String url = idUrlMap.get(id);
            if (url != null) {
              Utils.openUrl(context, url);
            }
          };
      viewHolder.buttonView.setOnClickListener(clickListener);
      viewHolder.categoryContributorsLayout.setOnClickListener(v -> {});

    } else if (holder instanceof AppItemViewHolder) {
      Category.AppItem categoryCItem = (Category.AppItem) item;
      AppItemViewHolder viewHolder = (AppItemViewHolder) holder;
      viewHolder.imageView.setImageResource(categoryCItem.getImageResource());
      viewHolder.titleTextView.setText(categoryCItem.getTitle());
      viewHolder.descriptionTextView.setText(categoryCItem.getDescription());

      View.OnClickListener clickListener =
          v -> {
            Map<String, String> idUrlMap = new HashMap<>();

            idUrlMap.put("id_report", "mailto:hridayanofficial@gmail.com?subject=Bug%20Report");
            idUrlMap.put(
                "id_feature", "mailto:hridayanofficial@gmail.com?subject=Feature%20Suggestion");
            idUrlMap.put("id_github", "https:github.com/DP-Hridayan/aShellYou");
            idUrlMap.put("id_telegram", "https://t.me/aShellYou");
            idUrlMap.put("id_discord", "https://discord.gg/cq5R2fF8sZ");
            String id = categoryCItem.getId();
            String url = idUrlMap.get(id);
            if (url != null) {
              Utils.openUrl(context, url);
            }

            Intent intent;
            switch (id) {
              case "id_changelogs":
                intent = new Intent(context, ChangelogActivity.class);
                break;

              default:
                return;
            }
            context.startActivity(intent);
          };
      viewHolder.categoryAppLayout.setOnClickListener(clickListener);

      int paddingInDp = 30;
      float scale = context.getResources().getDisplayMetrics().density;
      int paddingInPixels = (int) (paddingInDp * scale + 0.5f);

      ViewGroup.MarginLayoutParams layoutParams =
          (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
      layoutParams.bottomMargin = position == items.size() - 1 ? paddingInPixels : 0;
      viewHolder.itemView.setLayoutParams(layoutParams);
    }
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  private static class CategoryViewHolder extends RecyclerView.ViewHolder {
    TextView categoryTextView;

    public CategoryViewHolder(@NonNull View itemView) {
      super(itemView);
      categoryTextView = itemView.findViewById(R.id.category_text_view);
    }
  }

  private static class LeadDeveloperItemViewHolder extends RecyclerView.ViewHolder {
    ImageView imageView;
    TextView titleTextView, descriptionTextView;
    Button mMailButton, mXButton, mGithubButton, mSupportButton;

    public LeadDeveloperItemViewHolder(@NonNull View itemView) {
      super(itemView);
      imageView = itemView.findViewById(R.id.image_view);
      titleTextView = itemView.findViewById(R.id.title_text_view);
      descriptionTextView = itemView.findViewById(R.id.description_text_view);
      mMailButton = itemView.findViewById(R.id.mail);
      mGithubButton = itemView.findViewById(R.id.github);
      mXButton = itemView.findViewById(R.id.x);
      mSupportButton = itemView.findViewById(R.id.support);
    }
  }

  private static class contributorsItemViewHolder extends RecyclerView.ViewHolder {
    ImageView imageView;
    TextView titleTextView, descriptionTextView;
    Button buttonView;
    MaterialCardView categoryContributorsLayout;

    public contributorsItemViewHolder(@NonNull View itemView) {
      super(itemView);
      imageView = itemView.findViewById(R.id.image_view);
      titleTextView = itemView.findViewById(R.id.title_text_view);
      descriptionTextView = itemView.findViewById(R.id.description_text_view);
      buttonView = itemView.findViewById(R.id.github_handle);
      categoryContributorsLayout = itemView.findViewById(R.id.category_contributors_layout);
    }
  }

  private static class AppItemViewHolder extends RecyclerView.ViewHolder {
    ImageView imageView;
    TextView titleTextView, descriptionTextView;
    LinearLayout categoryAppLayout;

    public AppItemViewHolder(@NonNull View itemView) {
      super(itemView);
      imageView = itemView.findViewById(R.id.image_view);
      titleTextView = itemView.findViewById(R.id.title_text_view);
      descriptionTextView = itemView.findViewById(R.id.description_text_view);
      categoryAppLayout = itemView.findViewById(R.id.category_app_layout);
    }
  }
}
