package biz.swisstech.testapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkView;

import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

public class MainActivity extends AppCompatActivity {

    final static String HOST = "http://rghost.ru/";
    XWalkView webView;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        webView = (XWalkView) findViewById(R.id.web_view);
        webView.setResourceClient(new VtXWalkResource(webView));
        webView.load(HOST, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        webView.onActivityResult(requestCode, resultCode, data);
    }

    private class VtXWalkResource extends XWalkResourceClient {

        public VtXWalkResource(XWalkView view) {
            super(view);
        }

        @Override
        public void onLoadStarted(XWalkView view, String url) {
            super.onLoadStarted(view, url);
        }

        @Override
        public void onLoadFinished(XWalkView view, final String url) {
            super.onLoadFinished(view, url);
            progressBar.setVisibility(View.VISIBLE);
            Task.callInBackground(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    String linkResult = null;
                    Document doc = Jsoup.connect(url).get();
                    Element link = doc.select("#content a").first();
                    linkResult = link.getElementsByAttribute("href").attr("href");
                    return linkResult;
                }
            }).continueWith(new Continuation<String, Object>() {
                @Override
                public Object then(Task<String> task) throws Exception {
                    progressBar.setVisibility(View.GONE);
                    if (task.isFaulted()) {
                        // Toast.makeText(MainActivity.this, "link parser error: " + task.getError(), Toast.LENGTH_LONG).show();
                    } else {
                        String res = task.getResult();
                        Toast.makeText(MainActivity.this, res, Toast.LENGTH_LONG).show();
                    }
                    return null;
                }
            }, Task.UI_THREAD_EXECUTOR);
        }
    }
}
