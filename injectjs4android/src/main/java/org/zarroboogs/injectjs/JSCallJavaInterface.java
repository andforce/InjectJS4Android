package org.zarroboogs.injectjs;

import android.webkit.JavascriptInterface;

public abstract class JSCallJavaInterface extends Object{

	public JSCallJavaInterface() {
		super();
		// TODO Auto-generated constructor stub
	}

	public abstract void onJSCallJava(String... result);
	
	@JavascriptInterface
	public void jsCallJava(String result){
		String[] siplt = result.split("#&=&#");
		onJSCallJava(siplt);
	}
}
