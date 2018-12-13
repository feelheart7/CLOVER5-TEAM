package com.example.lenovo.myapplication;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.Random;
import java.util.Timer;

//新添加
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.MailTo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.lenovo.myapplication.Main2Activity;
import com.example.lenovo.myapplication.Upload;
import com.example.lenovo.myapplication.UserDBhelper;
//以上包如有没用的删除即可


public class MainActivity extends Activity {

    public static final String CREATE_ACCOUNT_URL = "http://122.152.193.34:8080/information/servlet/NewAccount";
    public static final int MSG_CREATE_RESULT = 1;


    TextView display,phoneNumber;
    EditText userID,temperature,weight,heartbeat,systolicPressure,diastolicPressure,bloodFat;
    Button insert,insert2,findAll,findLast,show,start_show,pause_show,contact;
    User user=null;
    static  Context mContext;
    Upload upload;
    private SQLiteDatabase db;
    private int record_Heartbeat,record_systolicPressure,record_diastolicPressure;
    private int count_hignHeartbeat=0,count_lowHeartbeat=0,count_highPressure=0,count_lowPressure=0;
    private UserDBhelper myDBHelper;
    private Timer timer =null;
    private TimerTask timerTask =null;

	/*
	使用多线程上传数据。
	*/

    ProgressDialog progress;//提示上传数据对话框


    private Handler m2Handler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_CREATE_RESULT:
                    //progress.dismiss();
                    JSONObject json = (JSONObject) msg.obj;
                    hanleCreateAccountResult(json);
                    progress.dismiss();
                    break;
            }
        }
    };

    private void hanleCreateAccountResult(JSONObject json) {
        /*
         *   result_code:
         * 0  注册成功
         * 1  用户名已存在
         * 2 数据库操作异常
         * */
        int result;
        try {
            result = json.getInt("result_code");
        } catch (JSONException e) {
            Toast.makeText(this, "没有获取到网络的响应！", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
        }

        if(result == 1) {
            Toast.makeText(this, "！", Toast.LENGTH_LONG).show();
            return;
        }

        if(result == 2) {
            Toast.makeText(this, "服务端出现异常！", Toast.LENGTH_LONG).show();
            return;
        }

        if(result == 0) {
            Toast.makeText(this, "上传成功！", Toast.LENGTH_LONG).show();
            return;
        }

    };

    private void createAccount(final User user) {
        progress = new ProgressDialog(this);
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);
        progress=progress.show(this, null, "上传中...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("clover5", "Start Network!");
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(CREATE_ACCOUNT_URL);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
//                params.add(new BasicNameValuePair("username", user.getUserID()));
                params.add(new BasicNameValuePair("username", "weijiajin1"));
                params.add(new BasicNameValuePair("temperature",  user.getTemperature()));
                params.add(new BasicNameValuePair("weight",  user.getWeight()));
                params.add(new BasicNameValuePair("heartbeat",  user.getHeartbeat()));
                params.add(new BasicNameValuePair("systolicPressure",  user.getSystolicPressure()));
                params.add(new BasicNameValuePair("diastolicPressure",  user.getDiastolicPressure()));
                params.add(new BasicNameValuePair("bloodFat",  user.getBloodFat()));
                System.out.print(user.getTemperature() + user.getWeight());
                try {
                    httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                    HttpResponse httpResponse = httpClient.execute(httpPost);
                    if(httpResponse.getStatusLine().getStatusCode() == 200) {
                        Log.d("clover5", "Network OK!");
                        HttpEntity entity = httpResponse.getEntity();
                        String entityStr = EntityUtils.toString(entity);
                        String jsonStr = entityStr.substring(entityStr.indexOf("{"));
                        JSONObject json = new JSONObject(jsonStr);
                        sendMessage(MSG_CREATE_RESULT, json);
                    }
                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void sendMessage(int what, Object obj) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        m2Handler.sendMessage(msg);
    }
    //////////////


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userID = (EditText) findViewById(R.id.userID);
        temperature = (EditText) findViewById(R.id.temperature);
        weight = (EditText) findViewById(R.id.weight);
        heartbeat = (EditText) findViewById(R.id.heartbeat);
        systolicPressure=(EditText) findViewById(R.id.systolicPressure);
        diastolicPressure=(EditText) findViewById(R.id.diastolicPressure);

        bloodFat = (EditText) findViewById(R.id.bloodFat);
        insert= (Button) findViewById(R.id.insert);
        insert2= (Button) findViewById(R.id.insert2);
        show= (Button) findViewById(R.id.show);
        findAll= (Button) findViewById(R.id.findAll);
        findLast= (Button) findViewById(R.id.findLast);
        display =(TextView) findViewById(R.id.display) ;
        phoneNumber =(TextView) findViewById(R.id.phoneNumber);
        contact= (Button) findViewById(R.id.contact);
        start_show =(Button) findViewById(R.id.start_show);
        pause_show =(Button) findViewById(R.id.pause_show);
        mContext=MainActivity.this;
        myDBHelper = new UserDBhelper(mContext, "Clover", null, 2);

        insert.setOnClickListener(new insertOnClick());
        insert2.setOnClickListener(new insert2OnClick());
        findAll.setOnClickListener(new findAllOnClick());
        findLast.setOnClickListener(new findLastOnClick());
        contact.setOnClickListener(new contactOnClick());
//        start_show.setOnClickListener(new startOnClick());
//        pause_show.setOnClickListener(new pauseOnClick());

        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                startActivity(intent);
            }
        });
        upload = new Upload(mContext,user);

        display =(TextView) findViewById(R.id.display) ;
        phoneNumber =(TextView) findViewById(R.id.phoneNumber);
        contact= (Button) findViewById(R.id.contact);
        start_show =(Button) findViewById(R.id.start_show);
        pause_show =(Button) findViewById(R.id.pause_show);
    }

    class insertOnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            user=new User(userID.getText().toString(),temperature.getText().toString(),weight.getText().toString(),heartbeat.getText().toString(),systolicPressure.getText().toString(),diastolicPressure.getText().toString(),bloodFat.getText().toString());
            createAccount(user);
            upload.insert(user);
        }
    }

    class insert2OnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            user=new User(nub(1,100),nub(36.3,45.0),nub(40.0,300.0),nub(50,100),nub(80,145),nub(55,95),nub(2.50,5.50));
            createAccount(user);
            upload.insert(user);
        }
    }

    public String nub(double min,double max){//输出1位小数点随机数
        BigDecimal db = new BigDecimal(Math.random() * (max - min) + min);
        return db.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }
    public String nub(int min,int max){//输出整数随机数
        BigDecimal db = new BigDecimal(Math.random() * (max - min) + min);
        return db.setScale(0, BigDecimal.ROUND_HALF_UP).toString();
    }
    public String nub(float min,float max){//输出2位小数点随机数
        BigDecimal db = new BigDecimal(Math.random() * (max - min) + min);
        return db.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }
    private class findAllOnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            StringBuilder sb= new StringBuilder();
            db=myDBHelper.getReadableDatabase();
            //指定查询结果的排序方式
            Cursor cursor = db.query("user", null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    String userID = cursor.getString(cursor.getColumnIndex("userID"));
                    String temperature = cursor.getString(cursor.getColumnIndex("temperature"));
                    String weight = cursor.getString(cursor.getColumnIndex("weight"));
                    String heartbeat = cursor.getString(cursor.getColumnIndex("heartbeat"));
                    String systolicPressure = cursor.getString(cursor.getColumnIndex("systolicPressure"));
                    String diastolicPressure = cursor.getString(cursor.getColumnIndex("diastolicPressure"));
                    String bloodFat = cursor.getString(cursor.getColumnIndex("bloodFat"));
                    String dateTime = cursor.getString(cursor.getColumnIndex("dateTime"));
                    sb.append("用户编号：" + userID +
                            " 体温：" + temperature +
                            " 体重：" + weight +
                            " 心跳：" + heartbeat +
                            " 收缩压（高压）：" + systolicPressure +
                            " 舒张压（低压）：" + diastolicPressure +
                            " 血脂：" + bloodFat +
                            " 记录时间：" + dateTime +
                            "\n\n");
                } while (cursor.moveToNext());
            }
            cursor.close();
            Toast.makeText(mContext, sb.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private class findLastOnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String dateTime = null;
            user=new User();
            db=myDBHelper.getReadableDatabase();
            Cursor cursor = db.query("user", null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {

                    user.setTemperature(cursor.getString(cursor.getColumnIndex("temperature")));
                    user.setWeight(cursor.getString(cursor.getColumnIndex("weight")));
                    user.setHeartbeat(cursor.getString(cursor.getColumnIndex("heartbeat")));
                    user.setSystolicPressure(cursor.getString(cursor.getColumnIndex("systolicPressure")));
                    user.setDiastolicPressure(cursor.getString(cursor.getColumnIndex("diastolicPressure")));
                    user.setBloodFat(cursor.getString(cursor.getColumnIndex("bloodFat")));
                    dateTime = cursor.getString(cursor.getColumnIndex("dateTime"));
                } while (cursor.moveToNext());
            }
            cursor.close();
            Toast.makeText(mContext,"用户编号：" +
                    " 体温：" + user.getTemperature() +
                    " 体重：" + user.getWeight() +
                    " 心跳：" + user.getHeartbeat() +
                    " 收缩压（高压）：" + user.getSystolicPressure() +
                    " 舒张压（低压）：" + user.getDiastolicPressure() +
                    " 血脂：" + user.getBloodFat() +
                    " 记录时间：" + dateTime, Toast.LENGTH_SHORT).show();
        }
    }


    private class contactOnClick implements  View.OnClickListener{
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    ContactsContract.Contacts.CONTENT_URI);
            MainActivity.this.startActivityForResult(intent, 1);

        }
    }

    private Handler mHandler =new Handler() {
        public void handleMessage(Message msg) {
            int n=0;
            record_Heartbeat = Integer.parseInt(user.getHeartbeat());
            record_systolicPressure = Integer.parseInt(user.getSystolicPressure());
            record_diastolicPressure = Integer.parseInt(user.getDiastolicPressure());
            display.setText("心跳:" + record_Heartbeat + "收缩压:" + record_systolicPressure + "舒张压:" + record_diastolicPressure);
            if (record_Heartbeat<60) {
                count_lowHeartbeat++;
                n=1;
            }
            if (record_Heartbeat > 100) {
                count_hignHeartbeat++;
                n=1;
            }
            if (record_systolicPressure<90 || record_diastolicPressure<60) {
                count_lowPressure++;
                n=1;
            }

            if (record_systolicPressure >140 || record_diastolicPressure> 90) {
                count_highPressure++;
                n=1;
            }
            if(n==1) {
                Toast.makeText(mContext, "当前出现\n" + count_lowHeartbeat + "次心率过低的情况,\n" + count_hignHeartbeat + "次心率过高的情况,\n" + count_lowPressure + "次血压过低的情况\n" + count_highPressure + "次血压过高的情况,\n请注意行车安全", Toast.LENGTH_SHORT).show();
            }
            if (count_hignHeartbeat > 5 || count_lowHeartbeat > 5 || count_highPressure > 5 || count_lowPressure > 5) {
                count_hignHeartbeat=0;
                count_lowHeartbeat=0;
                count_highPressure=0;
                count_lowPressure=0;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:"+phoneNumber.getText().toString()));
                        startActivity(intent);
                        timer.cancel();
                    }
                }).start();

            }
            random_number();
        }
    };
    public void random_number() {
        timer = new Timer();
        timerTask = new TimerTask() {

            @Override
            public void run() {
                user=new User(nub(1,100),nub(36.3,45.0),nub(40.0,300.0),nub(50,110),nub(80,145),nub(55,95),nub(2.50,5.50));
                Message message= mHandler.obtainMessage();
                message. obj =user;
                mHandler.sendMessage(message);
            }
        };

        timer .schedule(timerTask, 2000);
    }
    public void stopTime(){
        Toast. makeText(mContext, "停止实时监测" ,Toast.LENGTH_SHORT).show();
        timer.cancel();
    }

    protected void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);
        switch (requestCode)
        {
            case 1:
                if(resultCode==RESULT_OK)
                {
                    Uri contactData = data.getData();
                    Cursor cursor =managedQuery(contactData,null,null,null,null); //
                    cursor.moveToFirst();
                    String num =this.getContactPhone(cursor);
                    phoneNumber.setText(num);
                }
        }
    }
    private String getContactPhone (Cursor cursor)
    {
        int phoneColumn = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
        int phoneNum = cursor.getInt(phoneColumn);
        String result ="";
        if (phoneNum>0)
        {
            int idColumn = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            String contactId =cursor.getString(idColumn);
            Cursor phone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.
                    CONTENT_URI,null, ContactsContract.CommonDataKinds.Phone.
                    CONTACT_ID+"="+contactId,null,null);
            if(phone.moveToFirst())
            {
                for(;!phone.isAfterLast();phone.moveToNext())
                {
                    int index =phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    int typeindex =phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
                    int phone_type = phone.getInt(typeindex);
                    String phoneNumber = phone.getString(index);
                    result = phoneNumber;
                }
                if (!phone.isClosed()) {
                    phone.close();
                }

            }
        }
        return result;
    }

    class startOnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            if(Utils.isFastClick())
            {
                random_number();
            }
            else
            {
                Toast. makeText(mContext, "请不要重复点击" ,Toast.LENGTH_SHORT).show();
            }

        }
    }
    class pauseOnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            stopTime();
        }
    }
    public static class Utils {
        // 两次点击按钮之间的点击间隔不能少于1000毫秒
        private static final int MIN_CLICK_DELAY_TIME = 1000;
        private static long lastClickTime;

        public static boolean isFastClick() {
            boolean flag = false;
            long curClickTime = System.currentTimeMillis();
            if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
                flag = true;
            }
            lastClickTime = curClickTime;
            return flag;
        }
    }

}

