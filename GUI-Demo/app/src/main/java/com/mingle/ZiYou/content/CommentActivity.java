package com.mingle.ZiYou.content;

import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.mingle.ZiYou.bean.Comment;
import com.mingle.myapplication.R;
import com.qcloud.Module.Wenzhi;
import com.qcloud.QcloudApiModuleCenter;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        listView=(ListView)findViewById(R.id.comment_list);
        btn = (Button)findViewById(R.id.comment_btn);
        btn.setOnClickListener(new SendCommentBtnListener());
        edit = (EditText)findViewById(R.id.comment_edt);
        pid = getIntent().getIntExtra("pid",0);
       /* List<Map<String, Object>> list=getData();
        listView.setAdapter(new MyAdapter(this, list));*/
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath()
                .build());

    }
    public List<Map<String, Object>> getData(){
        List<Map<String, Object>> list=new ArrayList<Map<String,Object>>();
        for (int i = 0; i < 10; i++) {
            Map<String, Object> map=new HashMap<String, Object>();
            map.put("info", getInputComment(edit));
            list.add(map);
        }
        return list;
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
	    /* 将需要输入的参数都放入 params 里面，必选参数是必填的。 */
	    /* DescribeInstances 接口的部分可选参数如下 */
        params.put("offset", 0);
        params.put("limit", 3);
        params.put("content", commentStr);
        params.put("type", 2);
        params.put("code", 0x00200000);
	    /* generateUrl 方法生成请求串，但不发送请求。在正式请求中，可以删除下面这行代码。 */
        // System.out.println(module.generateUrl("DescribeInstances", params));
        String result = null;
	        /* call 方法正式向指定的接口名发送请求，并把请求参数params传入，返回即是接口的请求结果。 */
        result = module.call("TextSensitivity", params);
        JSONObject json_result = new JSONObject(result);
      //  int sensitity = json_result.getInt("sensitive");
//      resultText.setText(json_result.toString());
        Log.i("json",json_result.toString());
        addComment(pid,commentStr,1);
                /*if(sensitity<=0.5)
                    addComment(1,"",1);*/

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
    public void getCommentsByPointId(int pid, final Context context){
        //我们按游戏名统计所有玩家的总得分，并只返回总得分大于100的记录，并按时间降序

        BmobQuery<Comment> query = new BmobQuery<Comment>();
        query.addWhereGreaterThanOrEqualTo("cgrade",5);
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
