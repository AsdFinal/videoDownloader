package asd.dogw.videodownloader;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.view.animation.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import org.json.*;


public class MainActivity extends Activity
	{
		public static Handler handler;
		static Boolean isProcessing = false;
		static String serverUrl;
		static DBoper mydb;
		EditText EV1;
		ListView LV1;
		Button BTN1;
		LinearLayout proBar;
		Thread thread1;
		Animation probar_animate_show;
		Animation probar_animate_hide;

		/*public String getExceptionAllinformation(Exception ex)
		 {
		 String sOut = "";
		 StackTraceElement[] trace = ex.getStackTrace();

		 for (StackTraceElement s : trace)
		 {
		 sOut += ("\tat " + s + "\r\n");
		 }

		 SimpleDateFormat formatter = new SimpleDateFormat("HH_mm_ss");
		 Date curDate = new Date(System.currentTimeMillis()); //获取当前时间     
		 String dateStr = formatter.format(curDate);
		 echo2file(Environment.getExternalStorageDirectory() + "/asd.debug." +
		 dateStr + ".txt", sOut);

		 try
		 {
		 Thread.sleep(2000);
		 }
		 catch (Exception e)
		 {
		 }

		 return sOut;
		 }*/

		public void echo2file ( String path, String content )
			{
				try
					{
						File file = new File ( path );

						if ( !file.exists ( ) )
							{
								file.createNewFile ( );
							}

						FileWriter fw = new FileWriter ( file.getAbsoluteFile ( ) );
						BufferedWriter bw = new BufferedWriter ( fw );
						bw.write ( content );
						bw.close ( );
					}
				catch (IOException e)
					{
						Toast.makeText ( getApplicationContext ( ), path, 1 ).show ( );
					}
			}

		public Map obj2map ( JSONObject jsonObject )
			{
				Iterator keyIter = jsonObject.keys ( );
				String key;
				Object value;
				Map valueMap = new HashMap ( );

				while ( keyIter.hasNext ( ) )
					{
						key = (String) keyIter.next ( );

						try
							{
								value = jsonObject.get ( key );
								valueMap.put ( key, value );
							}
						catch (Exception e)
							{
								e.printStackTrace ( );
							}
					}

				return valueMap;
			}

		public String executeHttpGet ( String targetUrl )
			{
				String result = null;
				URL url = null;
				HttpURLConnection connection = null;
				InputStreamReader in = null;

				try
					{
						url = new URL ( targetUrl );
						connection = (HttpURLConnection) url.openConnection ( );
						in = new InputStreamReader ( connection.getInputStream ( ) );

						BufferedReader bufferedReader = new BufferedReader ( in );
						StringBuffer strBuffer = new StringBuffer ( );
						String line = null;

						while ( ( line = bufferedReader.readLine ( ) ) != null )
							{
								strBuffer.append ( line );
							}

						result = strBuffer.toString ( );
					}
				catch (Exception e)
					{
						Toast.makeText ( getApplicationContext ( ), "网络故障", 1 ).show ( );
						e.printStackTrace ( );
					}
				finally
					{
						if ( connection != null )
							{
								connection.disconnect ( );
							}

						if ( in != null )
							{
								try
									{
										in.close ( );
									}
								catch (IOException e)
									{
										e.printStackTrace ( );
									}
							}
					}

				return result;
			}

		@Override
		public boolean onOptionsItemSelected ( MenuItem item )
			{
				switch ( item.getItemId ( ) )
					{
						case R.id.item1:
							new aboutWindow ( this, null ).show ( );

							break;

						case R.id.item2:
							new donateWindow ( this, null ).show ( );

							break;
						case R.id.item4:
						    new changelogWindow ( this, null ).show ( );
							break;

						case R.id.item3:
							finish ( );

							break;
					}

				return true;
			}

		@Override
		public boolean onCreateOptionsMenu ( Menu menu )
			{
				MenuInflater inflater = getMenuInflater ( );
				inflater.inflate ( R.menu.menu, menu );

				return true;
			}

		@Override
		protected void onCreate ( Bundle savedInstanceState )
			{
				super.onCreate ( savedInstanceState );
				requestWindowFeature ( Window.FEATURE_NO_TITLE );
				setContentView ( R.layout.main );


				StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder ( ).permitAll ( ).build ( );
				StrictMode.setThreadPolicy ( policy );

				EV1 = (EditText) findViewById ( R.id.EditText1 );
				LV1 = (ListView) findViewById ( R.id.ListView1 );
				BTN1 = (Button) findViewById ( R.id.mainButton1 );
				proBar = (LinearLayout) findViewById ( R.id.processingBar );
				probar_animate_show = AnimationUtils.loadAnimation ( this,
																	R.anim.probar_animate_show );
				probar_animate_hide = AnimationUtils.loadAnimation ( this,
																	R.anim.probar_animate_hide );
				proBar.setVisibility ( View.GONE );
				mydb = new DBoper ( getApplicationContext ( ) );
				Intent intent = getIntent ( );
				String action = intent.getAction ( );
				String type = intent.getType ( );

				if ( Intent.ACTION_SEND.equals ( action ) && ( type != null ) )
					{
						//if ("text/plain".equals(type))
							{
								handleSendText ( intent );
							}
					}

				handler = new Handler ( ) {
						@Override
						public void handleMessage ( Message msg )
							{
								//Toast.makeText(getApplicationContext(), "recived", 1).show();
								proBar.startAnimation ( probar_animate_hide );
								proBar.setVisibility ( View.GONE );

								switch ( msg.what )
									{
										case 1:
											//Toast.makeText(getApplicationContext(), (String)msg.obj, 1).show();
											work ( (String) msg.obj );

											break;

										case 0:
											Toast.makeText ( getApplicationContext ( ),
															"发生错误，可能是网络故障", 1 ).show ( );

											break;

										case 3:

											TextView aboutText = (TextView) findViewById ( R.id.abouttext );
											aboutText.setText ( "g" );

											break;
									}
							}
					};
			}

		private void handleSendText ( Intent intent )
			{
				String sharedText = intent.getStringExtra ( Intent.EXTRA_TEXT );
				if ( sharedText != null )
					{
						EV1.setText ( sharedText );
						onWork ( null );
					}
			}

		private long download ( String url, String dir, String filename )
			{
				DownloadManager downloadManager = (DownloadManager) getSystemService ( DOWNLOAD_SERVICE );
				Uri uri = Uri.parse ( url );
				DownloadManager.Request request = new DownloadManager.Request ( uri );
				request.setAllowedNetworkTypes ( DownloadManager.Request.NETWORK_MOBILE |
												DownloadManager.Request.NETWORK_WIFI );
				request.setShowRunningNotification ( true );
				//不显示下载界面
				request.setVisibleInDownloadsUi ( true );
				request.allowScanningByMediaScanner ( );

				try
					{
						request.setDestinationInExternalFilesDir ( this, dir, filename );
					}
				catch (Exception e)
					{
						e.printStackTrace ( );
						Toast.makeText ( getApplicationContext ( ), "设置存储路径时发生异常,取消下载。", 1 )
							.show ( );

						return -1;
					}
				long id = downloadManager.enqueue ( request );

				/*DownloadManager.Query query=new DownloadManager.Query ( );
				 query.setFilterById ( id );
				 Cursor myDownload=downloadManager.query ( query );
				 myDownload.moveToFirst ( );
				 int mediaTypeIndex=myDownload.getColumnIndex ( downloadManager.COLUMN_MEDIA_TYPE );
				 String mediaType=myDownload.getString ( mediaTypeIndex );
				 myDownload.close ( );
				 echo2file ( "/sdcard/log.txt", mediaType );
				 //Toast.makeText(getApplicationContext(),mediaType,1);
				 mydb.insertRec ( id, dir );*/
				return id;
			}

		public void onWork ( View view )
			{
				if ( isProcessing )
					{
						Toast.makeText ( getApplicationContext ( ), "处理中，少安毋躁", 1 ).show ( );

						return;
					}
				ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>> ( );

				serverUrl = "http://asdserver.sinaapp.com/videoDownloaderQ?url=" +
					EV1.getText ( ).toString ( );
				String resp = executeHttpGet ( serverUrl );
				//Toast.makeText ( getApplicationContext ( ), resp, 1 ).show ( );
				try
					{

						JSONObject jsonObject = new JSONObject ( resp );
						Map infoOfVideoQuality = obj2map ( jsonObject );
						if ( (int) infoOfVideoQuality.get ( "default" ) == 1 )
							{
								HashMap<String, Object> map = new HashMap<String, Object> ( );
								map.put ( "videoName", "默认画质" );
								map.put ( "videoQuality", "available" );
								listItem.add ( map );

							}
						else
							{
								HashMap<String, Object> map = new HashMap<String, Object> ( );
								map.put ( "videoName", "默认画质" );
								map.put ( "videoQuality", "unavailable" );
								listItem.add ( map );
							}
						if ( (int) infoOfVideoQuality.get ( "high" ) == 1 )
							{
								HashMap<String, Object> map = new HashMap<String, Object> ( );
								map.put ( "videoName", "高清画质" );
								map.put ( "videoQuality", "available" );
								listItem.add ( map );

							}
						else
							{
								HashMap<String, Object> map = new HashMap<String, Object> ( );
								map.put ( "videoName", "高清画质" );
								map.put ( "videoQuality", "unavailable" );
								listItem.add ( map );

							}
						if ( (int)infoOfVideoQuality.get ( "super" ) == 1 )
							{
								HashMap<String, Object> map = new HashMap<String, Object> ( );
								map.put ( "videoName", "超清画质" );
								map.put ( "videoQuality", "available" );
								listItem.add ( map );

							}
						else
							{
								HashMap<String, Object> map = new HashMap<String, Object> ( );
								map.put ( "videoName", "超清画质" );
								map.put ( "videoQuality", "unavailable" );
								listItem.add ( map );
							}
						//Toast.makeText ( getApplicationContext ( ), "ff" + ( (int)infoOfVideoQuality.get ( "default" ) == 1 ), 1 ).show ( );

					}
				catch (Exception e)
					{
						Toast.makeText ( getApplicationContext ( ), "解析出错！！", 1 ).show ( );

					}
				//Toast.makeText(getApplicationContext(), "开始下载，在下载管理器可查看", 1)
				//.show();

				SimpleAdapter listItemAdapter = new SimpleAdapter ( this,
																   listItem, //数据源 
																   R.layout.list_items, //ListItem的XML实现 
																   new String[] { "videoName", "videoQuality" },

																   //ImageItem的XML文件里面的一个ImageView,两个TextView ID 
																   new int[] { R.id.videoName, R.id.videoQuality} );
				LV1.setAdapter ( listItemAdapter );
				LV1.setOnItemClickListener ( new AdapterView.OnItemClickListener ( ) {
							@Override
							public void onItemClick ( AdapterView<?> parent, View view,
													 int position, long id )
								{
									//Toast.makeText ( getApplicationContext ( ), "" + position, 1 ).show ( );
									isProcessing = true;
									proBar.setVisibility ( View.VISIBLE );
									proBar.startAnimation ( probar_animate_show );
									try
										{
											serverUrl = "http://asdserver.sinaapp.com/videoDownloader?url=" +
												EV1.getText ( ).toString ( ) + "&format=" + ( position + 1 );
											thread1 = new Thread ( new Runnable ( ) {
														@Override
														public void run ( )
															{
																Message msg = new Message ( );

																try
																	{
																		String resp = executeHttpGet ( serverUrl );
																		msg.obj = resp;
																		msg.what = 1;
																		handler.sendMessage ( msg );
																	}
																catch (Exception e)
																	{
																		msg.what = 0;
																		handler.sendMessage ( msg );
																	}
																finally
																	{
																		isProcessing = false;
																	}
															}
													} );
										}
									catch (Exception e)
										{
											//getExceptionAllinformation(e);
											proBar.startAnimation ( probar_animate_hide );
											proBar.setVisibility ( View.GONE );
										}

									thread1.start ( );
								}
						} );


			}

		private void work ( String json )
			{
				try
					{
						//String json = executeHttpGet(url);
						if ( json == "error" )
							{
								Toast.makeText ( getApplicationContext ( ), "输入有误或无法解析", 1 ).show ( );
							}
						else
							{
								try
									{
										JSONObject jsonObject = new JSONObject ( json );
										Map infoOfVideo = obj2map ( jsonObject );
										SimpleDateFormat formatter = new SimpleDateFormat (				
											"yyyy_MM_dd_HH_mm_ss" );
										Date curDate = new Date ( System.currentTimeMillis ( ) ); //获取当前时间     
										String dateStr = formatter.format ( curDate );
										String countOfVideo = "" + ( infoOfVideo.size ( ) - 1 );
										String tmpstr = (String) infoOfVideo.get ( "videoName" );
										String videoName = tmpstr.replaceAll ( " ", "" );
										Toast.makeText ( getApplicationContext ( ),
														videoName + " 解析成功" + "，共" + countOfVideo + "段视频", 1 )
											.show ( );



										for ( int i = 0; i < ( infoOfVideo.size ( ) - 1 ); i++ )
											{
												String key = "" + i;
												//Toast.makeText ( getApplicationContext ( ), "now " + i, 1 ).show ( );
												download ( (String) infoOfVideo.get ( key ),
														  videoName + dateStr,
														  "「" + key + "」" + videoName + ".flv" );
											}


									}
								catch (JSONException e)
									{
										e.printStackTrace ( );
										Toast.makeText ( getApplicationContext ( ), "未知错误,解析失败。请检查网址是否正确。", 1 )
											.show ( );
									}
							}
					}
				catch (Exception e)
					{
						//Toast.makeText(getApplicationContext(), "未知故障\n", 1).show();
						//new aboutWindow(this, getExceptionAllinformation(e)).show();
					}
			}
	}

