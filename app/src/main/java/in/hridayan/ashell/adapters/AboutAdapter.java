package in.hridayan.ashell.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.Category;
import in.hridayan.ashell.fragments.ChangelogFragment;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.utils.Utils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AboutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  private static final int CATEGORY = 0;
  private static final int CATEGORY_LEAD_DEV_ITEM = 1;
  private static final int CATEGORY_CONTRIBUTORS_ITEM = 2;
  private static final int CATEGORY_APP_ITEM = 3;
  private AdapterListener mListener;
  private final List<Object> items;
  private final Context context;
  private Activity activity;

  public AboutAdapter(List<Object> items, Context context, Activity activity) {
    this.items = items;
    this.context = context;
    this.activity = activity;
  }

  public interface AdapterListener {
    void onCheckUpdate();
  }

  public void setAdapterListener(AdapterListener listener) {
    mListener = listener;
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
            Map<String, String> idUrlMap = getContributorsIdUrlMap();

            String id = ContributorsItem.getId();
            String url = idUrlMap.get(id);
            if (url != null) {
              Utils.openUrl(context, url);
            }
          };
      viewHolder.buttonView.setOnClickListener(clickListener);
      viewHolder.categoryContributorsLayout.setOnClickListener(
          v -> {
            HapticUtils.weakVibrate(v, context);
            toggleExpandableLayout(viewHolder);
          });

      viewHolder.categoryContributorsLayout.setStrokeWidth(
          Utils.androidVersion() >= Build.VERSION_CODES.S ? 0 : 3);

    } else if (holder instanceof AppItemViewHolder) {
      Category.AppItem categoryCItem = (Category.AppItem) item;
      AppItemViewHolder viewHolder = (AppItemViewHolder) holder;
      viewHolder.imageView.setImageResource(categoryCItem.getImageResource());
      viewHolder.titleTextView.setText(categoryCItem.getTitle());
      viewHolder.descriptionTextView.setText(categoryCItem.getDescription());

      View.OnClickListener clickListener =
          v -> {
            // for haptic feedback
            HapticUtils.weakVibrate(v, context);

            Map<String, String> idUrlMap = getLeadDevIdUrlMap();
            String id = categoryCItem.getId();
            String url = idUrlMap.get(id);
            if (url != null) {
              Utils.openUrl(context, url);
            }

            if (id.equals("id_changelogs")) {
              ((FragmentActivity) activity)
                  .getSupportFragmentManager()
                  .beginTransaction()
                  .setCustomAnimations(
                      R.anim.fragment_enter,
                      R.anim.fragment_exit,
                      R.anim.fragment_pop_enter,
                      R.anim.fragment_pop_exit)
                  .replace(R.id.fragment_container, new ChangelogFragment())
                  .addToBackStack(null)
                  .commit();
            } else {
              return;
            }
          };

      if (categoryCItem.getId().equals("id_version")) {
        viewHolder.button.setVisibility(View.VISIBLE);
        viewHolder.button.setOnClickListener(
            v -> {
              // for haptic feedback
              HapticUtils.weakVibrate(v, context);

              if (mListener != null) {
                mListener.onCheckUpdate();
              }
            });
      } else {
        viewHolder.button.setVisibility(View.GONE);
      }

      viewHolder.categoryAppLayout.setOnClickListener(clickListener);

      int paddingInPixels = (int) (Utils.convertDpToPixel(30, context));
      ViewGroup.MarginLayoutParams layoutParams =
          (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
      layoutParams.bottomMargin = position == items.size() - 1 ? paddingInPixels : 0;
      viewHolder.itemView.setLayoutParams(layoutParams);
    }
  }

  // Function to expand or collapse the cardviews showing contributors
  private void toggleExpandableLayout(contributorsItemViewHolder viewHolder) {
    final LinearLayout expandableLayout = viewHolder.expandableLayout;
    final ImageView expandButton = viewHolder.expandButton;
    final int ANIMATION_DURATION = 250;

    if (expandableLayout.getVisibility() == View.GONE) {
      // Expand
      expandableLayout.setVisibility(View.VISIBLE);
      expandableLayout.measure(
          View.MeasureSpec.makeMeasureSpec(
              viewHolder.categoryContributorsLayout.getWidth(), View.MeasureSpec.EXACTLY),
          View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
      final int targetHeight = expandableLayout.getMeasuredHeight();

      expandableLayout.getLayoutParams().height = 0;
      ValueAnimator animator = ValueAnimator.ofInt(0, targetHeight);
      animator.addUpdateListener(
          animation -> {
            expandableLayout.getLayoutParams().height = (int) animation.getAnimatedValue();
            expandableLayout.requestLayout();
          });
      animator.addListener(
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
              expandableLayout.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
          });
      animator.setDuration(ANIMATION_DURATION);
      animator.start();

      // Rotate expand button
      expandButton.animate().rotation(180).setDuration(ANIMATION_DURATION).start();
    } else {
      // Collapse
      final int initialHeight = expandableLayout.getHeight();
      ValueAnimator animator = ValueAnimator.ofInt(initialHeight, 0);
      animator.addUpdateListener(
          animation -> {
            expandableLayout.getLayoutParams().height = (int) animation.getAnimatedValue();
            expandableLayout.requestLayout();
          });
      animator.addListener(
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
              expandableLayout.setVisibility(View.GONE);
            }
          });
      animator.setDuration(ANIMATION_DURATION);
      animator.start();

      // Rotate expand button back
      expandButton.animate().rotation(0).setDuration(ANIMATION_DURATION).start();
    }
  }

  private static @NonNull Map<String, String> getContributorsIdUrlMap() {
    Map<String, String> idUrlMap = new HashMap<>();

    idUrlMap.put("id_rikka", "https://github.com/RikkaApps/Shizuku");
    idUrlMap.put("id_sunilpaulmathew", "https://gitlab.com/sunilpaulmathew/ashell");
    idUrlMap.put("id_khun_htetz", "https://github.com/KhunHtetzNaing/ADB-OTG");
    idUrlMap.put("id_krishna", "https://github.com/KrishnaSSH");
    idUrlMap.put("id_shivam", "https://github.com/starry-shivam");
    idUrlMap.put("id_drDisagree", "https://github.com/Mahmud0808");
    idUrlMap.put("id_marciozomb13", "https://github.com/marciozomb13");
    idUrlMap.put("id_weiguangtwk", "https://github.com/WeiguangTWK");

    idUrlMap.put("id_winzort", "https://github.com/mikropsoft");
    return idUrlMap;
  }

  private static @NonNull Map<String, String> getLeadDevIdUrlMap() {
    Map<String, String> idUrlMap = new HashMap<>();

    idUrlMap.put("id_report", "mailto:hridayanofficial@gmail.com?subject=Bug%20Report");
    idUrlMap.put("id_feature", "mailto:hridayanofficial@gmail.com?subject=Feature%20Suggestion");
    idUrlMap.put("id_github", "https:github.com/DP-Hridayan/aShellYou");
    idUrlMap.put("id_telegram", "https://t.me/aShellYou");
    idUrlMap.put("id_discord", "https://discord.gg/cq5R2fF8sZ");
    idUrlMap.put("id_license", "https://github.com/DP-Hridayan/aShellYou/blob/master/LICENSE.md");
    return idUrlMap;
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
    LinearLayout expandableLayout;
    ImageView expandButton;
    MaterialCardView categoryContributorsLayout;

    public contributorsItemViewHolder(@NonNull View itemView) {
      super(itemView);
      imageView = itemView.findViewById(R.id.image_view);
      titleTextView = itemView.findViewById(R.id.title_text_view);
      descriptionTextView = itemView.findViewById(R.id.description_text_view);
      buttonView = itemView.findViewById(R.id.github_handle);
      expandButton = itemView.findViewById(R.id.expand_button);
      expandableLayout = itemView.findViewById(R.id.contrib_expanded_layout);
      categoryContributorsLayout = itemView.findViewById(R.id.category_contributors_layout);
    }
  }

  private static class AppItemViewHolder extends RecyclerView.ViewHolder {
    ImageView imageView;
    TextView titleTextView, descriptionTextView;
    LinearLayout categoryAppLayout;
    Button button;

    public AppItemViewHolder(@NonNull View itemView) {
      super(itemView);
      imageView = itemView.findViewById(R.id.image_view);
      titleTextView = itemView.findViewById(R.id.title_text_view);
      descriptionTextView = itemView.findViewById(R.id.description_text_view);
      button = itemView.findViewById(R.id.button);
      categoryAppLayout = itemView.findViewById(R.id.category_app_layout);
    }
  }
}
