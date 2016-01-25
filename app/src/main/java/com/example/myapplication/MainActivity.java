package com.example.myapplication;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    public static String URL_BASE = "http://biz.trace.ickd.cn/";

    public static final int MSG_SUCCUSS = 101;
    public static final int MSG_FAILED = 102;
    private ArrayAdapter<String> mAdapter;
    private String[] expressCNs;
    private String[] expressENs;

    private String expressNum;
    private int index;

    private String hintName,hintNumber;
    private SharedPreferences mSpref;

    private AutoCompleteTextView expressNameView;

    private EditText expressNumView;

    private TextView messageView = null;
    private ListView listView = null;
    private List<DataBean> mList = null;

    private TextView headView;

    private CommonAdapter<DataBean> mAdapter1;

    private ProgressDialog progressDialog;

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SUCCUSS:
                    InputStream is = (InputStream) msg.obj;

                    String expressMsg = inputStream2String(is);

                    Matcher matcher;

                    String message = "", tel = "", data = "";
                    Pattern mPattern = Pattern.compile("\"message\":\"(.*?)\"");
                    matcher = mPattern.matcher(expressMsg);

                    while (matcher.find()) {
                        message = matcher.group(1);
                        Log.i("zlt", "message = " + message);
                    }

                    mPattern = Pattern.compile("\"tel\":\"(.*?)\"");
                    matcher = mPattern.matcher(expressMsg);
                    while (matcher.find()) {
                        tel = matcher.group(1);
                        Log.i("zlt", "tel = " + tel);
                    }

                    mPattern = Pattern.compile("\"data\":(\\[.*?\\])");
                    matcher = mPattern.matcher(expressMsg);
                    while (matcher.find()) {
                        data = matcher.group(1);
                        Log.i("zlt", "data = " + data);
                    }

                    showMessageViews(message, tel, data);

                    Log.i("zlt", "text = " + expressMsg);
                    break;
                case MSG_FAILED:
                    break;
            }

            progressDialog.dismiss();
        }
    };

    public void showMessageViews(String message, String tel, String data) {
        if (message.length() > 0) {
            messageView.setText(message);
            messageView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            return;
        }

        if (data.length() >  0) {
            messageView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            mList.clear();
            mList.addAll(putStringToBean(data));
            mAdapter1.notifyDataSetChanged();
        }

    }

    private List<DataBean> putStringToBean(String data) {
        List<DataBean> list = new ArrayList<DataBean>();

        try {
            JSONArray jArray = new JSONArray(data);
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jObject = jArray.getJSONObject(i);
                DataBean bean = new DataBean();
                bean.setTime(jObject.getString("time"));
                bean.setContext(jObject.getString("context"));
                list.add(bean);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                getInputStream();
            }
        });

        mSpref = PreferenceManager.getDefaultSharedPreferences(this);
        hintName = mSpref.getString("hintName","中通");
        hintNumber = mSpref.getString("hintNunber","");

        initViews();
    }

    private void initViews() {
        expressCNs = getResources().getStringArray(R.array.express_name_cn);
        expressENs = getResources().getStringArray(R.array.express_name_en);

        mAdapter = new ArrayAdapter<String>(this, R.layout.custom_spinner_layout, expressCNs);

        expressNameView = (AutoCompleteTextView) findViewById(R.id.express_name);
        expressNameView.setText(hintName);
        expressNameView.setAdapter(mAdapter);
        expressNameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expressNameView.showDropDown();
            }
        });


        expressNumView = (EditText) findViewById(R.id.express_number);
        expressNumView.setText(hintNumber);

        messageView = (TextView) findViewById(R.id.message_view);

        mList = new ArrayList<>();
        listView = (ListView) findViewById(R.id.data_list);
        listView.setAdapter(mAdapter1 = new CommonAdapter<DataBean>(this, mList, R.layout.express_item) {
            @Override
            public void convert(ViewHolder viewHolder, DataBean item) {
                viewHolder.setText(R.id.id_time,item.getTime());
                viewHolder.setHtmlText(R.id.id_context,item.getContext().trim());
            }
        });

        headView = new TextView(this);
        headView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        headView.setMovementMethod(LinkMovementMethod.getInstance());
        listView.addHeaderView(headView);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在查询...");
        progressDialog.setCancelable(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getInputStream() {
        Log.i("zlt", "getInputStream");

        if (!checkIfEmpty()) {
            return;
        }


        index = checkExpressIndex();

        if (index == -1) {
            showToast("快递名不对！");
            return;
        }

        mSpref.edit().putString("hintName",expressNameView.getText().toString()).commit();
        mSpref.edit().putString("hintNunber",expressNumView.getText().toString()).commit();

        progressDialog.show();
        final String http_url = URL_BASE + expressENs[index] + "/" + expressNum + "?mailNo=" + expressNum;
        Log.i("zlt", "http_url = " + http_url);
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream is = null;
                try {
                    URL url = new URL(http_url);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setReadTimeout(5000);

                    Log.i("zlt","encodeing = " + connection.getContentEncoding());
                    is = connection.getInputStream();
                    Message msg = Message.obtain();
                    msg.what = MSG_SUCCUSS;
                    msg.obj = is;
                    mHandler.sendMessage(msg);

                } catch (IOException e) {
                    mHandler.sendEmptyMessage(MSG_FAILED);
                }
            }
        }).start();
    }

    private int checkExpressIndex() {
        String text = expressNameView.getText().toString().trim();

        for (int i = 0; i < expressCNs.length; i++) {
            if (text.equals(expressCNs[i])) {
                return i;
            }
        }
        return -1;
    }

    public String inputStream2String(InputStream in) {
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        try {
            for (int n; (n = in.read(b)) != -1; ) {
                out.append(new String(b, 0, n,
//                        "utf-8"
                        "gbk"
                ));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toString();
    }

    private boolean checkIfEmpty() {
        if (expressNameView.getText().toString().trim().length() <= 0) {
            showToast("快递名不能为空");
            return false;
        }
        expressNum = expressNumView.getText().toString();
        if (expressNum.length() <= 0 || expressNum == null) {
            showToast("快递单号不能未空");
            return false;
        }

        return true;
    }

    private Toast mToast;

    private void showToast(CharSequence s) {
        if (mToast == null) {
            mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        }
        mToast.setText(s);
        mToast.show();
    }
}
