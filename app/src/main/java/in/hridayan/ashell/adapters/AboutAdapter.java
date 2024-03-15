package in.hridayan.ashell.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.Category;
import in.hridayan.ashell.utils.Utils;
import java.util.List;

public class AboutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  private static final int VIEW_TYPE_CATEGORY = 0;
  private static final int VIEW_TYPE_CATEGORY_A_ITEM = 1;
  private static final int VIEW_TYPE_CATEGORY_B_ITEM = 2;
  private static final int VIEW_TYPE_CATEGORY_C_ITEM = 3;

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
      return VIEW_TYPE_CATEGORY;
    } else if (item instanceof Category.CategoryAItem) {
      return VIEW_TYPE_CATEGORY_A_ITEM;
    } else if (item instanceof Category.CategoryBItem) {
      return VIEW_TYPE_CATEGORY_B_ITEM;
    } else if (item instanceof Category.CategoryCItem) {
      return VIEW_TYPE_CATEGORY_C_ITEM;
    }
    return -1;
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    switch (viewType) {
      case VIEW_TYPE_CATEGORY:
        View categoryView = inflater.inflate(R.layout.category_item_layout, parent, false);
        return new CategoryViewHolder(categoryView);
      case VIEW_TYPE_CATEGORY_A_ITEM:
        View categoryAItemView = inflater.inflate(R.layout.category_a_item_layout, parent, false);
        return new CategoryAItemViewHolder(categoryAItemView);
      case VIEW_TYPE_CATEGORY_B_ITEM:
        View categoryBItemView = inflater.inflate(R.layout.category_b_item_layout, parent, false);
        return new CategoryBItemViewHolder(categoryBItemView);
      case VIEW_TYPE_CATEGORY_C_ITEM:
        View categoryCItemView = inflater.inflate(R.layout.category_c_item_layout, parent, false);
        return new CategoryCItemViewHolder(categoryCItemView);
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
    } else if (holder instanceof CategoryAItemViewHolder) {
      Category.CategoryAItem categoryAItem = (Category.CategoryAItem) item;
      CategoryAItemViewHolder viewHolder = (CategoryAItemViewHolder) holder;
      viewHolder.imageView.setImageResource(categoryAItem.getImageResource());
      viewHolder.titleTextView.setText(categoryAItem.getTitle());
      viewHolder.descriptionTextView.setText(categoryAItem.getDescription());
      viewHolder.mXButton.setOnClickListener(
          v -> {
            Utils.openUrl(context, "https://x.com/Spirriy1?t=VCLYRLEN-Pgq_RS2gQU-bg&s=09");
          });

      viewHolder.mGithubButton.setOnClickListener(
          v -> {
            Utils.openUrl(context, "https://github.com/DP-Hridayan/aShellYou");
          });

      viewHolder.mMailButton.setOnClickListener(
          v -> {
            Utils.openUrl(context, "mailto:hridayanofficial@gmail.com");
          });

    } else if (holder instanceof CategoryBItemViewHolder) {
      Category.CategoryBItem categoryBItem = (Category.CategoryBItem) item;
      CategoryBItemViewHolder viewHolder = (CategoryBItemViewHolder) holder;
      viewHolder.imageView.setImageResource(categoryBItem.getImageResource());
      viewHolder.titleTextView.setText(categoryBItem.getTitle());
      viewHolder.descriptionTextView.setText(categoryBItem.getDescription());

      viewHolder.setButtonIdentifier(position);

      viewHolder.buttonView.setOnClickListener(
          new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              int position = viewHolder.buttonIdentifier - 2;

              switch (position) {
                case 1:
                  Utils.openUrl(context, "https://github.com/RikkaApps/Shizuku");
                  break;

                case 2:
                  Utils.openUrl(context, "https://gitlab.com/sunilpaulmathew/ashell");
                  break;

                case 3:
                  Utils.openUrl(context, "https://github.com/KhunHtetzNaing/ADB-OTG");
                  break;

                case 4:
                  Utils.openUrl(context, "https://github.com/Krishna-G-OP");
                  break;
                default:
                  break;
              }
            }
          });
    } else if (holder instanceof CategoryCItemViewHolder) {
      Category.CategoryCItem categoryCItem = (Category.CategoryCItem) item;
      CategoryCItemViewHolder viewHolder = (CategoryCItemViewHolder) holder;
      viewHolder.imageView.setImageResource(categoryCItem.getImageResource());
      viewHolder.titleTextView.setText(categoryCItem.getTitle());
      viewHolder.descriptionTextView.setText(categoryCItem.getDescription());

      View.OnClickListener clickListener =
          v -> {
            String title = categoryCItem.getTitle();
            Intent intent;

            switch (title) {
              case "Report an issue":
              case "Feature request":
                Utils.openUrl(context, "mailto:hridayanofficial@gmail.com");
                break;

              case "Github":
                Utils.openUrl(context, "https:github.com/DP-Hridayan/aShellYou");
                break;
              case "Telegram channel":
                Utils.openUrl(context, "https://t.me/aShellYou");
                break;

              default:
                return;
            }
          };
      viewHolder.titleTextView.setOnClickListener(clickListener);
      viewHolder.descriptionTextView.setOnClickListener(clickListener);
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

  private static class CategoryAItemViewHolder extends RecyclerView.ViewHolder {
    ImageView imageView;
    TextView titleTextView;
    TextView descriptionTextView;
    Button mMailButton, mXButton, mGithubButton;

    public CategoryAItemViewHolder(@NonNull View itemView) {
      super(itemView);
      imageView = itemView.findViewById(R.id.image_view);
      titleTextView = itemView.findViewById(R.id.title_text_view);
      descriptionTextView = itemView.findViewById(R.id.description_text_view);
      mMailButton = itemView.findViewById(R.id.mail);
      mGithubButton = itemView.findViewById(R.id.github);
      mXButton = itemView.findViewById(R.id.x);
    }
  }

  private static class CategoryBItemViewHolder extends RecyclerView.ViewHolder {
    ImageView imageView;
    TextView titleTextView;
    TextView descriptionTextView;
    Button buttonView;
    int buttonIdentifier;

    public CategoryBItemViewHolder(@NonNull View itemView) {
      super(itemView);
      imageView = itemView.findViewById(R.id.image_view);
      titleTextView = itemView.findViewById(R.id.title_text_view);
      descriptionTextView = itemView.findViewById(R.id.description_text_view);
      buttonView = itemView.findViewById(R.id.github_handle);
    }

    public void setButtonIdentifier(int identifier) {
      buttonIdentifier = identifier;
    }
  }

  private static class CategoryCItemViewHolder extends RecyclerView.ViewHolder {
    ImageView imageView;
    TextView titleTextView;
    TextView descriptionTextView;

    public CategoryCItemViewHolder(@NonNull View itemView) {
      super(itemView);
      imageView = itemView.findViewById(R.id.image_view);
      titleTextView = itemView.findViewById(R.id.title_text_view);
      descriptionTextView = itemView.findViewById(R.id.description_text_view);
    }
  }
}
