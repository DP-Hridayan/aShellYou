package in.hridayan.ashell.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import in.hridayan.ashell.AshellYou;
import in.hridayan.ashell.R;
import in.hridayan.ashell.ui.CategoryAbout;
import in.hridayan.ashell.activities.MainActivity;
import in.hridayan.ashell.config.Const;
import in.hridayan.ashell.fragments.ChangelogFragment;
import in.hridayan.ashell.ui.dialogs.ActionDialogs;
import in.hridayan.ashell.utils.DeviceUtils;
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
  private final Activity activity;

  public AboutAdapter(List<Object> items, Activity activity) {
    this.items = items;
    this.activity = activity;
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
    if (item instanceof CategoryAbout) return CATEGORY;
    else if (item instanceof CategoryAbout.LeadDeveloperItem) return CATEGORY_LEAD_DEV_ITEM;
    else if (item instanceof CategoryAbout.ContributorsItem) return CATEGORY_CONTRIBUTORS_ITEM;
    else if (item instanceof CategoryAbout.AppItem) return CATEGORY_APP_ITEM;
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
      ((CategoryViewHolder) holder).bind((CategoryAbout) item);
    } else if (holder instanceof LeadDeveloperItemViewHolder) {
      ((LeadDeveloperItemViewHolder) holder).bind((CategoryAbout.LeadDeveloperItem) item);
    } else if (holder instanceof ContributorsItemViewHolder) {
      ((ContributorsItemViewHolder) holder).bind((CategoryAbout.ContributorsItem) item);
    } else if (holder instanceof AppItemViewHolder) {
      ((AppItemViewHolder) holder)
          .bind((CategoryAbout.AppItem) item, position == items.size() - 1, activity);
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

    public void bind(CategoryAbout category) {
      categoryTextView.setText(category.getName());
    }
  }

  private static class LeadDeveloperItemViewHolder extends RecyclerView.ViewHolder {
    private final ImageView imageView;
    private final TextView titleTextView, descriptionTextView;
    private final MaterialButton mMailButton, mTelegram, mGithubButton;
    private final LinearLayout support;

    public LeadDeveloperItemViewHolder(@NonNull View itemView) {
      super(itemView);
      imageView = itemView.findViewById(R.id.image_view);
      titleTextView = itemView.findViewById(R.id.title_text_view);
      descriptionTextView = itemView.findViewById(R.id.description_text_view);
      mMailButton = itemView.findViewById(R.id.mail);
      mGithubButton = itemView.findViewById(R.id.github);
      mTelegram = itemView.findViewById(R.id.telegram);
      support = itemView.findViewById(R.id.supportLayout);
    }

    public void bind(CategoryAbout.LeadDeveloperItem item) {
      imageView.setImageResource(item.getImageResource());
      titleTextView.setText(item.getTitle());
      descriptionTextView.setText(item.getDescription());

      Map<View, String> viewUrlMap = new HashMap<>();
      viewUrlMap.put(mTelegram, "https://t.me/hridayan");
      viewUrlMap.put(mGithubButton, Const.URL_DEV_GITHUB);
      viewUrlMap.put(mMailButton, "mailto:" + Const.DEV_EMAIL);
      viewUrlMap.put(support, Const.URL_DEV_BM_COFFEE);

      for (Map.Entry<View, String> entry : viewUrlMap.entrySet()) {
        entry
            .getKey()
            .setOnClickListener(
                v -> {
                  HapticUtils.weakVibrate(v);
                  Utils.openUrl(itemView.getContext(), entry.getValue());
                });
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

    public void bind(CategoryAbout.ContributorsItem item) {
      imageView.setImageResource(item.getImageResource());
      titleTextView.setText(item.getTitle());
      descriptionTextView.setText(item.getDescription());

      // onclick listener for github button which redirects to the github profiles of individual
      // contributors
      buttonView.setOnClickListener(
          v -> {
            HapticUtils.weakVibrate(v);
            Utils.openUrl(itemView.getContext(), item.getId().getGithub());
          });
      categoryContributorsLayout.setOnClickListener(
          v -> {
            HapticUtils.weakVibrate(v);
            toggleExpandableLayout();
          });

      categoryContributorsLayout.setStrokeWidth(
          DeviceUtils.androidVersion() >= Build.VERSION_CODES.S ? 0 : 3);
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
              if (endHeight == 0) view.setVisibility(View.GONE);
              else view.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
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

    public void bind(CategoryAbout.AppItem item, boolean isLastItem, Activity activity) {
      imageView.setImageResource(item.getImageResource());
      titleTextView.setText(item.getTitle());
      descriptionTextView.setText(item.getDescription());

      if (item.getId().equals(Const.ID_VERSION)) {
        button.setVisibility(View.VISIBLE);
      } else {
        button.setVisibility(View.GONE);
        loadingDots.setVisibility(View.GONE);
      }

      View.OnClickListener clickListener =
          v -> {
            HapticUtils.weakVibrate(v);

            String url = getAppIdUrlMap().get(item.getId());
            if (url != null) Utils.openUrl(itemView.getContext(), url);

            if (item.getId().equals(Const.ID_VERSION)) {
              button.setOnClickListener(
                  view -> {
                    HapticUtils.weakVibrate(view);
                    if (mListener != null) mListener.onCheckUpdate(button, loadingDots);
                  });
            }

            if (item.getId().equals(Const.ID_CHANGELOGS)) {
              ChangelogFragment fragment = new ChangelogFragment();
              ((MainActivity) activity)
                  .getSupportFragmentManager()
                  .beginTransaction()
                  .setCustomAnimations(
                      R.anim.fragment_enter,
                      R.anim.fragment_exit,
                      R.anim.fragment_pop_enter,
                      R.anim.fragment_pop_exit)
                  .replace(R.id.fragment_container, fragment, fragment.getClass().getSimpleName())
                  .addToBackStack(fragment.getClass().getSimpleName())
                  .commit();
            }
          };

      if (item.getId().equals(Const.ID_REPORT)) {
        categoryAppLayout.setOnClickListener(
            v -> {
              HapticUtils.weakVibrate(v);
              ActionDialogs.chooseFeedbackModeDialog(activity, Const.FEEDBACK_MODE_BUG);
            });
      } else if (item.getId().equals(Const.ID_FEATURE)) {
        categoryAppLayout.setOnClickListener(
            v -> {
              HapticUtils.weakVibrate(v);
              ActionDialogs.chooseFeedbackModeDialog(activity, Const.FEEDBACK_MODE_FEATURE);
            });
      } else {
        categoryAppLayout.setOnClickListener(clickListener);
      }

      int paddingInPixels = (int) (Utils.convertDpToPixel(30, itemView.getContext()));
      ViewGroup.MarginLayoutParams layoutParams =
          (ViewGroup.MarginLayoutParams) itemView.getLayoutParams();
      layoutParams.bottomMargin = isLastItem ? paddingInPixels : 0;
      itemView.setLayoutParams(layoutParams);
    }
  }

  private static Map<String, String> getAppIdUrlMap() {
    Map<String, String> idUrlMap = new HashMap<>();
    idUrlMap.put(Const.ID_GITHUB, Const.URL_GITHUB_REPOSITORY);
    idUrlMap.put(Const.ID_TELEGRAM, Const.URL_TELEGRAM);
    idUrlMap.put(Const.ID_DISCORD, "https://discord.gg/cq5R2fF8sZ");
    idUrlMap.put(Const.ID_LICENSE, Const.URL_APP_LICENSE);
    return idUrlMap;
  }
}
