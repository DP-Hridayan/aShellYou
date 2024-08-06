package in.hridayan.ashell.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.airbnb.lottie.LottieAnimationView;
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

  public AboutAdapter(List<Object> items) {
    this.items = items;
  }

  public interface AdapterListener {
    void onCheckUpdate(Button button, LottieAnimationView loadingDots);
  }

  public void setAdapterListener(AdapterListener listener) {
    mListener = listener;
  }

  @Override
  public int getItemViewType(int position) {
    Object item = items.get(position);
    if (item instanceof Category) return CATEGORY;
    else if (item instanceof Category.LeadDeveloperItem) return CATEGORY_LEAD_DEV_ITEM;
    else if (item instanceof Category.ContributorsItem) return CATEGORY_CONTRIBUTORS_ITEM;
    else if (item instanceof Category.AppItem) return CATEGORY_APP_ITEM;
    return -1;
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    switch (viewType) {
      case CATEGORY:
        return new CategoryViewHolder(inflater.inflate(R.layout.category_about, parent, false));
      case CATEGORY_LEAD_DEV_ITEM:
        return new LeadDeveloperItemViewHolder(
            inflater.inflate(R.layout.category_lead_dev, parent, false));
      case CATEGORY_CONTRIBUTORS_ITEM:
        return new ContributorsItemViewHolder(
            inflater.inflate(R.layout.category_contributors, parent, false));
      case CATEGORY_APP_ITEM:
        return new AppItemViewHolder(
            inflater.inflate(R.layout.category_app, parent, false), mListener);
      default:
        throw new IllegalArgumentException("Invalid view type");
    }
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    Object item = items.get(position);
    if (holder instanceof CategoryViewHolder) {
      ((CategoryViewHolder) holder).bind((Category) item);
    } else if (holder instanceof LeadDeveloperItemViewHolder) {
      ((LeadDeveloperItemViewHolder) holder).bind((Category.LeadDeveloperItem) item);
    } else if (holder instanceof ContributorsItemViewHolder) {
      ((ContributorsItemViewHolder) holder).bind((Category.ContributorsItem) item);
    } else if (holder instanceof AppItemViewHolder) {
      ((AppItemViewHolder) holder).bind((Category.AppItem) item, position == items.size() - 1);
    }
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  private static class CategoryViewHolder extends RecyclerView.ViewHolder {
    private final TextView categoryTextView;

    public CategoryViewHolder(@NonNull View itemView) {
      super(itemView);
      categoryTextView = itemView.findViewById(R.id.category_text_view);
    }

    public void bind(Category category) {
      categoryTextView.setText(category.getName());
    }
  }

  private static class LeadDeveloperItemViewHolder extends RecyclerView.ViewHolder {
    private final ImageView imageView;
    private final TextView titleTextView, descriptionTextView;
    private final Button mMailButton, mXButton, mGithubButton, mSupportButton;

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

    public void bind(Category.LeadDeveloperItem item) {
      imageView.setImageResource(item.getImageResource());
      titleTextView.setText(item.getTitle());
      descriptionTextView.setText(item.getDescription());

      Map<Button, String> buttonUrlMap = new HashMap<>();
      buttonUrlMap.put(mXButton, "https://x.com/Spirriy1?t=VCLYRLEN-Pgq_RS2gQU-bg&s=09");
      buttonUrlMap.put(mGithubButton, "https://github.com/DP-Hridayan");
      buttonUrlMap.put(mMailButton, "mailto:hridayanofficial@gmail.com");
      buttonUrlMap.put(mSupportButton, "https://www.buymeacoffee.com/hridayan");

      for (Map.Entry<Button, String> entry : buttonUrlMap.entrySet()) {
        entry
            .getKey()
            .setOnClickListener(v -> Utils.openUrl(itemView.getContext(), entry.getValue()));
      }
    }
  }

  private static class ContributorsItemViewHolder extends RecyclerView.ViewHolder {
    private final ImageView imageView, expandButton;
    private final TextView titleTextView, descriptionTextView;
    private final Button buttonView;
    private final LinearLayout expandableLayout;
    private final MaterialCardView categoryContributorsLayout;

    public ContributorsItemViewHolder(@NonNull View itemView) {
      super(itemView);
      imageView = itemView.findViewById(R.id.image_view);
      titleTextView = itemView.findViewById(R.id.title_text_view);
      descriptionTextView = itemView.findViewById(R.id.description_text_view);
      buttonView = itemView.findViewById(R.id.github_handle);
      expandButton = itemView.findViewById(R.id.expand_button);
      expandableLayout = itemView.findViewById(R.id.contrib_expanded_layout);
      categoryContributorsLayout = itemView.findViewById(R.id.category_contributors_layout);
    }

    public void bind(Category.ContributorsItem item) {
      imageView.setImageResource(item.getImageResource());
      titleTextView.setText(item.getTitle());
      descriptionTextView.setText(item.getDescription());

      buttonView.setOnClickListener(
          v -> Utils.openUrl(itemView.getContext(), getContributorsIdUrlMap().get(item.getId())));
      categoryContributorsLayout.setOnClickListener(
          v -> {
            HapticUtils.weakVibrate(v, itemView.getContext());
            toggleExpandableLayout();
          });

      categoryContributorsLayout.setStrokeWidth(
          Utils.androidVersion() >= Build.VERSION_CODES.S ? 0 : 3);
    }

    private void toggleExpandableLayout() {
      final int ANIMATION_DURATION = 250;
      if (expandableLayout.getVisibility() == View.GONE) {
        expandableLayout.setVisibility(View.VISIBLE);
        expandableLayout.measure(
            View.MeasureSpec.makeMeasureSpec(
                categoryContributorsLayout.getWidth(), View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        final int targetHeight = expandableLayout.getMeasuredHeight();
        animateLayoutHeight(expandableLayout, 0, targetHeight, ANIMATION_DURATION);
        expandButton.animate().rotation(180).setDuration(ANIMATION_DURATION).start();
      } else {
        final int initialHeight = expandableLayout.getHeight();
        animateLayoutHeight(expandableLayout, initialHeight, 0, ANIMATION_DURATION);
        expandButton.animate().rotation(0).setDuration(ANIMATION_DURATION).start();
      }
    }

    private void animateLayoutHeight(View view, int startHeight, int endHeight, int duration) {
      ValueAnimator animator = ValueAnimator.ofInt(startHeight, endHeight);
      animator.addUpdateListener(
          animation -> {
            view.getLayoutParams().height = (int) animation.getAnimatedValue();
            view.requestLayout();
          });
      animator.addListener(
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
              if (endHeight == 0) 
                view.setVisibility(View.GONE);
              else 
                view.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
              
            }
          });
      animator.setDuration(duration);
      animator.start();
    }
  }

  private static class AppItemViewHolder extends RecyclerView.ViewHolder {
    private final ImageView imageView;
    private final TextView titleTextView, descriptionTextView;
    private final LinearLayout categoryAppLayout;
    private final Button button;
    private final AdapterListener mListener;
    private final LottieAnimationView loadingDots;

    public AppItemViewHolder(@NonNull View itemView, AdapterListener listener) {
      super(itemView);
      imageView = itemView.findViewById(R.id.image_view);
      titleTextView = itemView.findViewById(R.id.title_text_view);
      descriptionTextView = itemView.findViewById(R.id.description_text_view);
      button = itemView.findViewById(R.id.check_update_button);
      categoryAppLayout = itemView.findViewById(R.id.category_app_layout);
      loadingDots = itemView.findViewById(R.id.loading_animation);
      mListener = listener;
    }

    public void bind(Category.AppItem item, boolean isLastItem) {
      imageView.setImageResource(item.getImageResource());
      titleTextView.setText(item.getTitle());
      descriptionTextView.setText(item.getDescription());

      View.OnClickListener clickListener =
          v -> {
            HapticUtils.weakVibrate(v, itemView.getContext());

            String url = getAppIdUrlMap().get(item.getId());
            if (url != null) Utils.openUrl(itemView.getContext(), url);

            if ("id_changelogs".equals(item.getId())) {
              ((FragmentActivity) itemView.getContext())
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
            }
          };

      if ("id_version".equals(item.getId())) {
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(
            v -> {
              HapticUtils.weakVibrate(v, itemView.getContext());
              if (mListener != null) mListener.onCheckUpdate(button, loadingDots);
            });
      } else {
        button.setVisibility(View.GONE);
        loadingDots.setVisibility(View.GONE);
      }

      categoryAppLayout.setOnClickListener(clickListener);

      int paddingInPixels = (int) (Utils.convertDpToPixel(30, itemView.getContext()));
      ViewGroup.MarginLayoutParams layoutParams =
          (ViewGroup.MarginLayoutParams) itemView.getLayoutParams();
      layoutParams.bottomMargin = isLastItem ? paddingInPixels : 0;
      itemView.setLayoutParams(layoutParams);
    }
  }

  private static Map<String, String> getContributorsIdUrlMap() {
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

  private static Map<String, String> getAppIdUrlMap() {
    Map<String, String> idUrlMap = new HashMap<>();
    idUrlMap.put("id_report", "mailto:hridayanofficial@gmail.com?subject=Bug%20Report");
    idUrlMap.put("id_feature", "mailto:hridayanofficial@gmail.com?subject=Feature%20Suggestion");
    idUrlMap.put("id_github", "https:github.com/DP-Hridayan/aShellYou");
    idUrlMap.put("id_telegram", "https://t.me/aShellYou");
    idUrlMap.put("id_discord", "https://discord.gg/cq5R2fF8sZ");
    idUrlMap.put("id_license", "https://github.com/DP-Hridayan/aShellYou/blob/master/LICENSE.md");
    return idUrlMap;
  }
}
