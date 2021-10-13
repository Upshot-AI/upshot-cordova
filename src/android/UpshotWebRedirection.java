package cordova_plugin_upshotplugin;

import android.app.Activity;
import android.app.Application;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class UpshotWebRedirection extends Activity {


  private String mUrl = "";
  private WebView mWebView = null;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // init default values
    init();

    Resources resources = getApplicationResources();
    String packageName = getApplicationPackageName();
    int layout = resources.getIdentifier("upshot_web_redirection", "layout", packageName);
    int close_buttonId = resources.getIdentifier("close_button", "id", packageName);
    // set content view
    setContentView(layout);

    Button button = (Button) findViewById(close_buttonId);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
      }
    });


    // inti web view settings
    initWebView();
  }

  /**
   * Initialize default values
   * by getting values from Bundle
   */
  private void init() {
    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      mUrl = extras.getString("url");
    }

  }

  /**
   * Initialize web view
   */
  private void initWebView() {

    Resources resources = getApplicationResources();
    String packageName = getApplicationPackageName();
    int webviewId = resources.getIdentifier("webview", "id", packageName);
    mWebView = (WebView) findViewById(webviewId);
    mWebView.setWebViewClient(new CustomWebViewClient());
    mWebView.getSettings().setJavaScriptEnabled(true);
    mWebView.loadUrl(mUrl);
  }


  @Override
  protected void onPause() {
    super.onPause();

    // stop loading web view in-order
    // to stop any videos that keep playing in the background even
    // after the web view has stopped.
    mWebView.stopLoading();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // reset web view
    mWebView.loadUrl("about:blank");
  }

  private Resources getApplicationResources() {
    Application app = getApplication();
    Resources resources = app.getResources();
    return resources;
  }

  private String getApplicationPackageName() {
    Application app = getApplication();
    String packageName = app.getPackageName();
    return packageName;
  }

  /**
   * Create ad redirection event
   */
  private void createEvent() {
    //TODO
  }

  private class CustomWebViewClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      // Web view URl redirects

      view.loadUrl(url);
      return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
      super.onPageStarted(view, url, favicon);

      // Web View is still loading
    }

    @Override
    public void onPageFinished(WebView view, String url) {
      super.onPageFinished(view, url);

    }
  }

}
