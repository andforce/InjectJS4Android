package org.zarroboogs.injectjs;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

@SuppressLint("JavascriptInterface")
public class InjectJS {

	private WebView mWebView;
    private AsyncHttpClient mAsyncHttpClient = new AsyncHttpClient();
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
	 * @param js
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
		mAsyncHttpClient.get(url, new AsyncHttpResponseHandler() {
			
			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				// TODO Auto-generated method stub
				try {
					String response = new String(arg2, encode);
					mInjectedString = response.replace("</head>", jsStr + "\n</head>");
					for (KeyValue remove : mReplaceList) {
						mInjectedString = mInjectedString.replace(remove.getKey(), remove.getValue());
					}

					mWebView.loadDataWithBaseURL(url, mInjectedString, "text/html", "UTF-8", "");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	class InjectWebChromeClient extends WebChromeClient{
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
