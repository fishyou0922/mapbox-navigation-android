package com.mapbox.services.android.navigation.ui.v5.feedback;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mapbox.services.android.navigation.ui.v5.R;
import com.mapbox.services.android.navigation.ui.v5.ThemeSwitcher;

public class FeedbackBottomSheet extends BottomSheetDialogFragment implements FeedbackClickListener.ClickCallback {

  public static final String TAG = FeedbackBottomSheet.class.getSimpleName();

  private FeedbackBottomSheetListener feedbackBottomSheetListener;
  private FeedbackAdapter feedbackAdapter;
  private RecyclerView feedbackItems;
  private ProgressBar feedbackProgressBar;

  public static FeedbackBottomSheet newInstance(FeedbackBottomSheetListener feedbackBottomSheetListener) {
    FeedbackBottomSheet feedbackBottomSheet = new FeedbackBottomSheet();
    feedbackBottomSheet.setFeedbackBottomSheetListener(feedbackBottomSheetListener);
    return feedbackBottomSheet;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(BottomSheetDialogFragment.STYLE_NO_FRAME, R.style.Theme_Design_BottomSheetDialog);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.feedback_bottom_sheet_layout, container, false);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    bind(view);
    initFeedbackRecyclerView();
    initCountDownAnimation();
    initBackground(view);
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
      @Override
      public void onShow(DialogInterface dialog) {
        BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;
        FrameLayout bottomSheet = bottomSheetDialog.findViewById(android.support.design.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
          BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
          behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
          behavior.setSkipCollapsed(true);
        }
      }
    });
    return dialog;
  }

  @Override
  public void onFeedbackItemClick(int feedbackPosition) {
    FeedbackItem feedbackItem = feedbackAdapter.getFeedbackItem(feedbackPosition);
    Toast.makeText(getContext(), "Feedback Submitted", Toast.LENGTH_SHORT).show();
    feedbackBottomSheetListener.onFeedbackSelected(feedbackItem);
    dismiss();
  }

  @Override
  public void onDismiss(DialogInterface dialog) {
    super.onDismiss(dialog);
    feedbackBottomSheetListener.onFeedbackDismissed();
  }

  public void setFeedbackBottomSheetListener(FeedbackBottomSheetListener feedbackBottomSheetListener) {
    this.feedbackBottomSheetListener = feedbackBottomSheetListener;
  }

  private void bind(View bottomSheetView) {
    feedbackItems = bottomSheetView.findViewById(R.id.feedbackItems);
    feedbackProgressBar = bottomSheetView.findViewById(R.id.feedbackProgress);
  }

  private void initFeedbackRecyclerView() {
    feedbackAdapter = new FeedbackAdapter();
    feedbackItems.setAdapter(feedbackAdapter);
    feedbackItems.setOverScrollMode(RecyclerView.OVER_SCROLL_IF_CONTENT_SCROLLS);
    feedbackItems.addOnItemTouchListener(new FeedbackClickListener(getContext(), this));
    if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
      feedbackItems.setLayoutManager(new LinearLayoutManager(getContext(),
        LinearLayoutManager.HORIZONTAL, false));
    } else {
      feedbackItems.setLayoutManager(new GridLayoutManager(getContext(), 3));
    }
  }

  private void initCountDownAnimation() {
    ObjectAnimator countdownAnimation = ObjectAnimator.ofInt(feedbackProgressBar,
      "progress", 0);
    countdownAnimation.setInterpolator(new LinearInterpolator());
    countdownAnimation.setDuration(5000);
    countdownAnimation.addListener(new Animator.AnimatorListener() {
      @Override
      public void onAnimationStart(Animator animation) {

      }

      @Override
      public void onAnimationEnd(Animator animation) {
        FeedbackBottomSheet.this.dismiss();
      }

      @Override
      public void onAnimationCancel(Animator animation) {

      }

      @Override
      public void onAnimationRepeat(Animator animation) {

      }
    });
    countdownAnimation.start();
  }

  private void initBackground(View view) {
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
      int navigationViewPrimaryColor = ThemeSwitcher.retrieveNavigationViewPrimaryColor(getContext());
      int navigationViewSecondaryColor = ThemeSwitcher.retrieveNavigationViewSecondaryColor(getContext());
      // BottomSheet background
      Drawable bottomSheetBackground = DrawableCompat.wrap(view.getBackground()).mutate();
      DrawableCompat.setTint(bottomSheetBackground, navigationViewPrimaryColor);
      // ProgressBar progress color
      LayerDrawable progressBarBackground = (LayerDrawable) feedbackProgressBar.getProgressDrawable();
      Drawable progressDrawable = progressBarBackground.getDrawable(1);
      progressDrawable.setColorFilter(navigationViewSecondaryColor, PorterDuff.Mode.SRC_IN);
    }
  }
}
