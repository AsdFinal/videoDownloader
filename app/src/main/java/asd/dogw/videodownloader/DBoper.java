package asd.dogw.videodownloader;


import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import java.util.*;

//DBOptions for login
public class DBoper
{
	private static final String DB_NAME = "rec.db";
	private static final String DB_CREATE="create table records(id int,videoname text)";
	public class DBHelper extends SQLiteOpenHelper
	{

		public DBHelper(Context context)
		{
			super(context, DB_NAME, null, 1);
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			// TODO Auto-generated method stub
			//建表
			db.execSQL(DB_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			// TODO Auto-generated method stub
			//db.execSQL("drop table if exists logininf");
			//onCreate(db);
		}

	}
	private Context context;
	private SQLiteDatabase db;
	private DBHelper dbHelper;
	public  DBoper(Context context)
	{
		this.context = context;
		dbHelper = new DBHelper(context);
		db = dbHelper.getReadableDatabase();

	}
	//自己写的方法，对数据库进行操作
	public String getName()
	{

		Cursor cursor = db.rawQuery("select name from logininf", null);
		cursor.moveToFirst();
		return cursor.getString(0);     
	}
	public int changePWD(String oldP, String pwd)
	{
		ContentValues values = new ContentValues();
		values.put("pwd", pwd);
		return db.update("logininf", values, "pwd=" + oldP, null);
	}
	public int insertRec(long id, String videoname)
	{
		ContentValues cv=new ContentValues();
		cv.put("id", id);
		cv.put("videoname", videoname);
		db.insert("records", null, cv);
		return 0;
	}
	public int deleteRecById(long id){
		db.delete("records","id=",new String[]{id+""});
		return 0;
	}
	public List getCurrentJobsId()
	{
		List<Long> res=new ArrayList()<Long>;
		long id=0;
		Cursor cr;
		cr = db.rawQuery("select * from records where 1", null);
		if (cr.moveToFirst())
		{
			for (int i = 0; i < cr.getCount(); i++)
			{
				id = cr.getLong((cr.getColumnIndex("id")));
				res.add(id);
				cr.moveToNext();
			}
		}
		return res;
	}
}
