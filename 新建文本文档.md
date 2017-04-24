###RippleBackground
水波纹效果，由点变成圈（RelativeLayout的子类）


1, xml中写法：
````xml
<com.iflytek.eduvoiceassistant.ui.widget.RippleBackground
		android:id="@+id/view_ripple"
		android:layout_width="match_parent"
		android:layout_height="230dp"
		android:layout_gravity="bottom"
		android:background="@color/transparent"
		app:rb_color="#fff"//圈颜色
		app:rb_duration="2000" // 动画时间
		app:rb_radius="80dp"// 半径
		app:rb_rippleAmount="2"// 波纹个数2
		app:rb_scale="4"
		app:rb_type="strokeRipple">
</com.iflytek.eduvoiceassistant.ui.widget.RippleBackground>
````

2.代码中：

````java
 mRipple.startRippleAnimation();//开始动画
 mRipple.stopRippleAnimation();//结束动画
````

###RollView
歌词滚动效果,自定义的view

1.xml中
````xml
<com.iflytek.eduvoiceassistant.ui.widget.RollView
		android:id="@+id/main_roll_view"
		android:layout_width="match_parent"
		android:layout_height="280dp"
		android:layout_marginBottom="5dp"
		android:layout_marginTop="50dp"
		app:currentTextColor="@android:color/white"	//中间行的颜色
		app:currenttextSize="24sp"	//中间行的字体大小
		app:dividerHeight="20dp"	//间距
		app:normalTextColor="#88ffffff"	//其他字颜色
		app:rows="5" //行数
		app:textSize="18sp"	//正常字体大小
></com.iflytek.eduvoiceassistant.ui.widget.RollView>
````
2.代码中
````java
		List<String> data = new ArrayList<>();
        data.add("您可以说，找一下昨天/上周三/4月8号高一八班的数学作业");
        data.add("比如您可以说，我要找/来一个/搜一下小孔成像的视频");
        mRollView.setData(data);//设置数据
        mRollView.moveNow();//进行动起来
````
