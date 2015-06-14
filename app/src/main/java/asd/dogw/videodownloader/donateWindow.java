package asd.dogw.videodownloader;

import android.app.*;
import android.content.*;
import android.view.*;

public class donateWindow extends AlertDialog
	{ 
		public donateWindow(Context context, String msg)
			{ 
				super(context); 
				final View view = getLayoutInflater().inflate(R.layout.donate, null);

				setButton(context.getText(R.string.close), (OnClickListener) null); 
				//setIcon(R.drawable.icon_about); 
				//setTitle("视频下载助手"); 
				setView(view); 
				if (msg != null)
					{
						setTitle(msg);
					}
				else
					{
						setTitle("视频伴侣-捐助我！");
					}
			} 
	}
