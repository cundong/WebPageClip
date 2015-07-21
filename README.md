# WebPageClip
Clip the whole webpage

1.
WebView
onMeasure() 大小都设置为MeasureSpec.UNSPECIFIED

2.
调用WebView的layout()方法，重新布局。

3.
重新布局之后，获取当前webView的：
 int measuredWidth = mWebview.getMeasuredWidth();
 int measuredHeight = mWebview.getMeasuredHeight();

4.
根据获取的长宽，创建一个空的Bitmap，通过使用Canvas，把webView的内容draw到这个Bitmap上

感谢  @dodola 解决了我一直都想做的这个功能。
