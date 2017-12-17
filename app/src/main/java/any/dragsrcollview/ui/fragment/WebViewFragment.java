package any.dragsrcollview.ui.fragment;

import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import any.dragsrcollview.R;

/**
 * Created by anyrsan on 2017/12/17.
 */

public class WebViewFragment extends BaseFragment {

    private WebView webView;

    // 实际上可以传参过来，通过setArguments(Bundle bundle)
    public static WebViewFragment getInstance() {
        WebViewFragment webViewFragment = new WebViewFragment();
        return webViewFragment;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_webview_layout;
    }

    @Override
    public void scroll2Top() {
        webView.scrollTo(0,0);
    }

    @Override
    public View getScrollableView() {
        return webView; //这里是根view,有时并不一定是
    }

    @Override
    public void initView() {
        webView = rootView.findViewById(R.id.fw_layout_webview);
        //注意这里最好是本地内容加载
        webView.loadUrl("https://m.autohome.com.cn/");
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
    }
}
