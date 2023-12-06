package cordova_plugin_upshotplugin;

import android.app.Activity;
import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

public class UpshotReviewManager {

  private ReviewManager reviewManager;

  UpshotReviewManager(Context context) {
    reviewManager = ReviewManagerFactory.create(context);
  }

  public void showRateApp(Activity activity) {

    Task<ReviewInfo> request = reviewManager.requestReviewFlow();
    request.addOnCompleteListener(task -> {

      if (task.isSuccessful()) {
        ReviewInfo reviewInfo = task.getResult();
        Task<Void> flow = reviewManager.launchReviewFlow(activity, reviewInfo);
        flow.addOnCompleteListener(task1 -> {

        });
      }
    });

  }
}
