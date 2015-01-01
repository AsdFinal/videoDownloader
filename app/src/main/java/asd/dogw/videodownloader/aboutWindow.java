package asd.dogw.videodownloader;

import android.app.*;
import android.content.*;
import android.view.*;
import android.widget.*;
import android.os.*;
import asd.dogw.videodownloader.*;

public class aboutWindow extends AlertDialog
{ 
	public aboutWindow(Context context, String msg)
	{ 
		super(context); 
		final View view = getLayoutInflater().inflate(R.layout.about, null);

		setButton(context.getText(R.string.close), (OnClickListener) null); 
		//setIcon(R.drawable.icon_about); 
		setTitle("视频下载助手"); 
		setView(view); 
		if (msg != null)
		{
			//TextView ed=(TextView) findViewById(R.id.abouttext);
			//ed.setText(msg);
			//ed.setText("g");
			Message Tmsg=new Message();
			Tmsg.obj = msg;
			Tmsg.what = 3;
			MainActivity.handler.sendMessage(Tmsg);
		}
	} 
}
