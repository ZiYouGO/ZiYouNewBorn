package com.mingle.ZiYou.content;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.mingle.ZiYou.adapter.CommentAdapter;
import com.mingle.ZiYou.adapter.MyAdapter;
import com.mingle.ZiYou.bean.Comment;
import com.mingle.myapplication.R;
import com.qcloud.Module.Wenzhi;
import com.qcloud.QcloudApiModuleCenter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

public class CommentActivity extends AppCompatActivity {
    ListView listView;
    Button btn;
    EditText edit;
    int pid;

    private JSONObject json_sensity__result;
    private JSONObject json_emotion_result;
    double negative;
    double sensity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        listView=(ListView)findViewById(R.id.comment_list);
        btn = (Button)findViewById(R.id.comment_btn);
        btn.setOnClickListener(new SendCommentBtnListener());
        edit = (EditText)findViewById(R.id.comment_edt);
        pid = getIntent().getIntExtra("pid",0);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath()
                .build());
        //显示所有评论
        getCommentsByPointId(getIntent().getIntExtra("pid",0),this);
        //评论显示在界面上


    }

    //获取用户输入评论数据
    public String getInputComment(EditText edit){
        return edit.getText().toString();
    }
    //数据库加入数据
    public void addData(int pid,String commentStr) throws Exception {
        Log.i("info",commentStr);
        Log.i("pid",pid+" ");
        TreeMap<String, Object> config = new TreeMap<String, Object>();
        config.put("SecretId", "AKIDS36z6xfsuUvguPtorH1D4Gs93m3zoX3n");
        config.put("SecretKey", "nT1LTCtroi8bV1pJCkIxXaZvRgUQWJU6");
       /* 请求方法类型 POST、GET */
        config.put("RequestMethod", "GET");
	    /* 区域参数，可选: gz:广州; sh:上海; hk:香港; ca:北美;等。 */
        config.put("DefaultRegion", "sh");
        QcloudApiModuleCenter classification = new QcloudApiModuleCenter(new Wenzhi(), config);
        QcloudApiModuleCenter module = new QcloudApiModuleCenter(new Wenzhi(), config);
        TreeMap<String, Object> params = new TreeMap<String, Object>();
        TreeMap<String, Object> params1 = new TreeMap<String, Object>();
	    /* 将需要输入的参数都放入 params 里面，必选参数是必填的。 */
	    /* DescribeInstances 接口的部分可选参数如下 */
        params.put("offset", 0);
        params.put("limit", 3);
        params.put("content", commentStr);
        params.put("type", 2);
        params.put("code", 0x00200000);

        params1.put("offset", 0);
        params1.put("limit", 3);
        params1.put("content", commentStr);
        params1.put("type",2);
        params1.put("code", 0x00200000);
        /* generateUrl 方法生成请求串，但不发送请求。在正式请求中，可以删除下面这行代码。 */
        // System.out.println(module.generateUrl("DescribeInstances", params));
        String result = null;
        String result1=null;
	        /* call 方法正式向指定的接口名发送请求，并把请求参数params传入，返回即是接口的请求结果。 */
        result = module.call("TextSensitivity", params);
        result1=module.call("TextSentiment",params1);
        json_emotion_result=new JSONObject(result1);
        json_sensity__result = new JSONObject(result);

        handler.post(run);
        handler2.post(run2);


        Log.i("json", negative + "  " + sensity);

        double grade=(negative+sensity)/2;
        if(grade<=0.5)
            addComment(pid,commentStr,10 - Integer.parseInt(new java.text.DecimalFormat("0").format(grade*10)));

    }
    class SendCommentBtnListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            try {
                addData(pid,getInputComment(edit));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            Bundle b = msg.getData();
            sensity=b.getDouble("sensitive");
        }
    };

    Runnable run = new Runnable(){
        @Override
        public void run(){
            Bundle bundle = new Bundle();
            Message m = new Message();
            try {
                bundle.putDouble("sensitive", json_sensity__result.getDouble("sensitive"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            m.setData(bundle);
            handler.sendMessage(m);
            handler.removeCallbacks(run);
        }
    };

    Handler handler2 = new Handler(){
        @Override
        public void handleMessage(Message msg){
            //String s = String.valueOf(msg.what);
            Bundle b = msg.getData();
            negative=b.getDouble("negative");
        }
    };

    Runnable run2 = new Runnable(){
        @Override
        public void run(){
            Bundle bundle = new Bundle();
            Message m = new Message();
            //bundle.putDouble("sensitive", json_sensity__result.getDouble("sensitive"));
            try {
                bundle.putDouble("negative", json_emotion_result.getDouble("negative"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            m.setData(bundle);
            handler2.sendMessage(m);
            //handler.postDelayed(run, 5000);
            handler2.removeCallbacks(run);
        }
    };
    public void getCommentsByPointId(int pid, final Context context){
        //我们按游戏名统计所有玩家的总得分，并只返回总得分大于100的记录，并按时间降序

        BmobQuery<Comment> query = new BmobQuery<Comment>();
        query.addWhereGreaterThanOrEqualTo("cgrade",5);
        query.addWhereEqualTo("pid", pid);
        query.order("-cgrade,createdAt");
        query.findObjects(context, new FindListener<Comment>() {
            @Override
            public void onSuccess(List<Comment> object) {
                // TODO Auto-generated method stub
                List<Comment> comments=new ArrayList<Comment>();
                Toast.makeText(context,"查询成功：共"+object.size()+"条数据。",
                        Toast.LENGTH_SHORT).show();
                for (Comment comment : object) {
                    comments.add(comment);
                }
                //评论显示在界面上
                List<Map<String, Object>> list=new ArrayList<Map<String,Object>>();
                for (int i = 0; i < comments.size(); i++) {
                    Map<String, Object> map=new HashMap<String, Object>();
                    map.put("user", "用户"+comments.get(i).getObjectId());
                    map.put("comment", comments.get(i).getCtext());
                    list.add(map);
                }
                listView.setAdapter(new CommentAdapter(context,list));

            }
            @Override
            public void onError(int code, String msg) {
                // TODO Auto-generated method stub
                Toast.makeText(context,"查询失败："+msg,
                        Toast.LENGTH_SHORT).show();
            }
        });

    }
    public void addComment(int pid,String comment,int cgrade){
        Comment new_comment = new Comment();
        new_comment.setCgrade(cgrade);
        new_comment.setCtext(comment);
        new_comment.setPid(pid);
        final boolean flag=false;
        new_comment.save(this.getApplication(), new SaveListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(),"添加成功",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int code, String arg0) {
                // 添加失败
                Toast.makeText(getApplicationContext(),"添加失败",Toast.LENGTH_SHORT).show();
            }
        });
    }

}
