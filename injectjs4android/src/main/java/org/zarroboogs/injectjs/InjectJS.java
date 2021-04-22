package org.zarroboogs.injectjs;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("JavascriptInterface")
public class InjectJS {

	private WebView mWebView;
    private OkHttpClient mOkHttpClient = new OkHttpClient();
    private String mInjectedString;
    private OnLoadListener mListener;
    private String mJSCallJavaFunction = "";
    
    private List<KeyValue> mReplaceList = new ArrayList<>();
    
    public static interface OnLoadListener{
    	public void onLoad();
    }
    
    public static class KeyValue{
    	private String key;
    	private String value;
		public KeyValue(String key, String value) {
			super();
			// TODO Auto-generated constructor stub
			this.key = key;
			this.value = value;
		}
		public String getKey() {
			return key;
		}
		public void setKey(String key) {
			this.key = key;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
    }
    
	public InjectJS(WebView webView) {
		super();
		// TODO Auto-generated constructor stub
		this.mWebView = webView;
		init();
	}
	
	public void setOnLoadListener(OnLoadListener listener){
		this.mListener = listener;
	}
	
	private void init(){
		InjectWebChromeClient injectWebChromeClient = new InjectWebChromeClient();
        mWebView.setWebChromeClient(injectWebChromeClient);
	}
	
	/**
	 * window.JSINTERFACE.saveAccountInfo(loginName.value, loginPassword.value);
	 * @param obj
	 * @param callParams
	 */
	public void addJSCallJavaInterface(JSCallJavaInterface obj, String... callParams){
		mJSCallJavaFunction = buildJSCallJava(callParams);
		mWebView.addJavascriptInterface(obj, "JS_CALL_JAVA");
	}
	
	/**
	 * @param callParams	loginName.value
	 * @return
	 */
	private String buildJSCallJava(String... callParams){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < callParams.length; i++) {
			if (i == callParams.length - 1) {
				sb.append(callParams[i]);
			}else {
				sb.append(callParams[i] + "+\"#&=&#\"+");
			}
		}
		String function = "\n" +
			"    function jsCallJavaFunction() {									\n" + 
	        "		window.JS_CALL_JAVA.jsCallJava(" + sb.toString() +");			\n" + 
	    	"    }																	\n";
		return function;
	}
	
	public void removeDocument(String remove){
		mReplaceList.add(new KeyValue(remove, ""));
	}
	
	public void replaceDocument(String src, String replace){
		mReplaceList.add(new KeyValue(src, replace));
	}
	public void exeJsFunction(String functionName){
		mWebView.loadUrl("javascript:" + functionName + "()");
	}
	
	public void exeJsFunctionWithParam(String functionName, String... parmas){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < parmas.length; i++) {
			if (i == parmas.length - 1) {
				sb.append("\""+ parmas[i] + "\"" );
			}else {
				sb.append("\""+parmas[i] + "\"" + " , ");
			}
		}
		String url = "javascript:" + functionName + "(" + sb.toString() + ")";

		mWebView.loadUrl(url);
	}
	
	public void jsCallJava(){
		exeJsFunction("jsCallJavaFunction()");
	}
	
	private String jsStr = "";
	public void injectUrl(final String url, String js, final String encode){
		jsStr = js;
		jsStr = jsStr.replace("</script>", mJSCallJavaFunction + "</script>");

        Request request = new Request.Builder().url(url).build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                String responseStr = new String(response.body().bytes(), "GBK");

                Log.d("responseStr",responseStr);
                mInjectedString = responseStr.replace("</head>", jsStr + "\n</head>");
                for (KeyValue remove : mReplaceList) {
                    mInjectedString = mInjectedString.replace(remove.getKey(), remove.getValue());
                }

                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadDataWithBaseURL(url, mInjectedString, "text/html", "UTF-8", "");
                    }
                });
            }
        });

	}
	
	class InjectWebChromeClient extends WebChromeClient {
		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			// TODO Auto-generated method stub
			if (newProgress == 100) {
                if (!TextUtils.isEmpty(view.getUrl()) && view.getUrl().equalsIgnoreCase("about:blank")) {
                	if (mListener != null) {
						mListener.onLoad();
					}
                }

            }
			super.onProgressChanged(view, newProgress);
		}
	}
}
