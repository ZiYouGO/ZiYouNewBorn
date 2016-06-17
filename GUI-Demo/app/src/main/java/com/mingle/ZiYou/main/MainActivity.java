package com.mingle.ZiYou.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.mingle.ZiYou.adapter.MyAdapter;
import com.mingle.ZiYou.bean.Scene;
import com.mingle.ZiYou.content.MapActivity;
import com.mingle.ZiYou.service.SceneInfor;
import com.mingle.myapplication.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

public class MainActivity extends AppCompatActivity {
private List<Scene> scenes=new ArrayList<Scene>();
    private  ListView listView;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bmob.initialize(this, "c1c024e7612cd05fcfecbc3d9909b3ee");
        setContentView(R.layout.activity_main);

        listView=(ListView)findViewById(R.id.list);

        getAllScenes();
        Log.i("size:1",scenes.size()+"");

    }


    public List<Map<String, Object>> getData(List<Scene> scenes){
        List<Map<String, Object>> list=new ArrayList<Map<String,Object>>();
        SceneInfor sceneInfor = new SceneInfor();
//        List<Scene> scenes = sceneInfor.getSceneList(MainActivity.this);
        for (int i = 0; i < scenes.size(); i++) {
            Map<String, Object> map=new HashMap<String, Object>();
            map.put("image", R.drawable.bjtu);
            map.put("title", scenes.get(i).getSname());
            map.put("info", "information"+i);
            list.add(map);
        }
        return list;
    }

    /*
    获取数据
     */

    //获取景区列表
    public void getAllScenes(){
        BmobQuery<Scene> sceneBmobQuery=new BmobQuery<Scene>();
        sceneBmobQuery.findObjects(MainActivity.this, new FindListener<Scene>() {
            @Override
            public void onSuccess(List<Scene> object) {
                // TODO Auto-generated method stub
                List<Scene> sceneList = new ArrayList<Scene>();
                //Toast.makeText(MainActivity.this,"查询成功：共"+object.size()+"条数据。",
                        //Toast.LENGTH_SHORT).show();
                for (Scene scene : object) {
                    sceneList.add(scene);
                }
//                getData(sceneList);
                scenes=sceneList;
                Log.i("size:2",scenes.size()+"");
                List<Map<String, Object>> list=getData(scenes);
                listView.setAdapter(new MyAdapter(MainActivity.this, list));
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        //Intent intent = new Intent(MainActivity.this, MapActivity.class);
                        //测试用，启动TestMapActivity
                        Intent intent = new Intent(MainActivity.this, MapActivity.class);
                        //Intent intent = new Intent(MainActivity.this, CommentActivity.class);
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onError(int code, String msg) {
                // TODO Auto-generated method stub
                Toast.makeText(MainActivity.this,"查询失败："+msg,
                        Toast.LENGTH_SHORT).show();
            }
        });
//        return sceneList;
    }

}
