package com.mingle.ZiYou.content;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.mingle.ZiYou.bean.Point;
import com.mingle.ZiYou.clusterutil.clustering.ClusterItem;
import com.mingle.ZiYou.clusterutil.clustering.ClusterManager;
import com.mingle.ZiYou.main.MainActivity;
import com.mingle.entity.MenuEntity;
import com.mingle.myapplication.R;
import com.mingle.sweetpick.BlurEffect;
import com.mingle.sweetpick.RecyclerViewDelegate;
import com.mingle.sweetpick.SweetSheet;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

public class MapActivity extends AppCompatActivity {

    private SweetSheet mSweetSheet;
    private SweetSheet mSweetSheet2;
    private SweetSheet mSweetSheet3;
    private RelativeLayout rl;
    Button Sheet1, Sheet2, Sheet3, Back, btnGo;
    private MapView mapView = null;
    private BaiduMap baiduMap;
    private List<Point> points2Travel = new ArrayList<Point>();
    private RoutePlanSearch mSearch, mSearch2;
    private int  i = 0;

    //定位相关
    public LocationClient mLocationClient = null;
    private static final int UPDATE_TIME = 5000;//间隔时间之后重新获取定位
    public BDLocationListener myListener = new MyLocationListener();//监听器
    BitmapDescriptor mCurrentMarker;//定位小图标
    MyLocationConfiguration.LocationMode mCurrentMode;//定位模式
    boolean isFirstLoc = true;//是否是首次定位

    //标注相关
    MapStatus ms;
    private ClusterManager<MyItem> mClusterManager;
    private myMarkerListener markerListener = new myMarkerListener();
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //调用地图组建前使用的一句话
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_map);

        mapView = (MapView) findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        //mxy-地图中心点和缩放比例
        baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        baiduMap.setMyLocationEnabled(true);
        LatLng cenpt = new LatLng(39.95799,116.349642);
        MapStatus mMapStatus = new MapStatus.Builder()
                .target(cenpt)
                .zoom(17)
                .build();
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        baiduMap.setMapStatus(mMapStatusUpdate);
        //用户定位
        baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        baiduMap.setMyLocationEnabled(true);
        mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode,true,mCurrentMarker);
        baiduMap.setMyLocationConfigeration(config);

        //定位初始化
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        initLocation();
        mLocationClient.registerLocationListener(myListener);    //注册监听函数

        //开始定位
        mLocationClient.start();

        rl = (RelativeLayout) findViewById(R.id.rl);
        Back = (Button) findViewById(R.id.Back);
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapActivity.this.finish();
            }
        });


        //
        //导航按钮事件
        btnGo = (Button)findViewById(R.id.btn_go);
        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMarkers();
                mSearch = RoutePlanSearch.newInstance();
                //mSearch2 = RoutePlanSearch.newInstance();
                OnGetRoutePlanResultListener listener = new OnGetRoutePlanResultListener() {
                    @Override
                    public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
                        if (walkingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                            WalkingRouteOverlay overlay = new WalkingRouteOverlay(baiduMap);
                            baiduMap.setOnMarkerClickListener(overlay);
                            overlay.setData(walkingRouteResult.getRouteLines().get(0));
                            overlay.addToMap();
                            overlay.zoomToSpan();
                        } else Log.e("mxy", "no result");
                    }

                    @Override
                    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

                    }

                    @Override
                    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

                    }

                    @Override
                    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

                    }
                };
                mSearch.setOnGetRoutePlanResultListener(listener);
                //addMarkers();
                if(i == 0) routineDrawing(i);
                //记得这里需要销毁
                //mSearch.destroy();
            }
        });


        mClusterManager = new ClusterManager<MyItem>(this, baiduMap);
        // 添加Marker点
        //addMarkers();
        // 设置地图监听，当地图状态发生改变时，进行点聚合运算
        baiduMap.setOnMapStatusChangeListener(mClusterManager);
        baiduMap.setOnMarkerClickListener(markerListener);
        setButton();
        getPointDaoListBySceneId(1000);
    }

    private void routineDrawing(int i)
    {
       // PlanNode stNode = PlanNode.withCityNameAndPlaceName("北京", "北京交通大学逸夫楼");
        //PlanNode enNode = PlanNode.withCityNameAndPlaceName("北京", "北京交通大学思源楼");
        PlanNode stNode = PlanNode.withCityNameAndPlaceName("北京",points2Travel.get(i++).getPname());
        PlanNode enNode = PlanNode.withCityNameAndPlaceName("北京",points2Travel.get(i++).getPname());
        mSearch.walkingSearch((new WalkingRoutePlanOption())
                .from(stNode)
                .to(enNode));
        //mSearch.destroy();
    }

    private void setButton(){

        Sheet1 = (Button) findViewById(R.id.Sheet1);
        Sheet1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSweetSheet.toggle();
            }
        });

        Sheet2 = (Button) findViewById(R.id.Sheet2);
        Sheet2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSweetSheet2.toggle();
            }
        });

        Sheet3 = (Button) findViewById(R.id.Sheet3);
        Sheet3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSweetSheet3.toggle();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mapView.onDestroy();
        if (mLocationClient != null && mLocationClient.isStarted()) {
            mLocationClient.stop();
            mLocationClient = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mapView.onPause();
    }

    private  ArrayList getData(List<Point> points){
        final ArrayList<MenuEntity> list = new ArrayList<>();
        //添加假数据
        MenuEntity menuEntity1 = new MenuEntity();
        //menuEntity1.iconId = R.drawable.checkbox_empty;
        menuEntity1.titleColor = 0xff000000;
        menuEntity1.title = "                           确定路线";
        list.add(menuEntity1);
        for (int i=0;i<points.size();i++){
            MenuEntity menuEntity = new MenuEntity();
            menuEntity.iconId = R.drawable.checkbox_empty;
            menuEntity.titleColor = 0xff000000;
            menuEntity.title = points.get(i).getPname();
            list.add(menuEntity);
        }
        return  list;
    }

    //景点列表
    public void getPointDaoListBySceneId(int sceneId){
        BmobQuery<Point> pointBmobQuery=new BmobQuery<Point>();
        pointBmobQuery.addWhereEqualTo("sid",sceneId);
        pointBmobQuery.order("pid");
        pointBmobQuery.findObjects(MapActivity.this, new FindListener<Point>() {
            @Override
            public void onSuccess(final List<Point> object) {
                mSweetSheet = new SweetSheet(rl);

                final ArrayList<MenuEntity> list = getData(object);
                mSweetSheet.setMenuList(list);

                mSweetSheet.setDelegate(new RecyclerViewDelegate(true));
                //根据设置不同Effect 来显示背景效果BlurEffect:模糊效果.DimEffect 变暗效果
                mSweetSheet.setBackgroundEffect(new BlurEffect(8));
                //设置点击事件
                mSweetSheet.setOnMenuItemClickListener(new SweetSheet.OnMenuItemClickListener() {
                    @Override
                    public boolean onItemClick(int position, MenuEntity menuEntity1) {
                        //即时改变当前项的颜色
                        if (list.get(position).iconId == R.drawable.checkbox_empty && position != 0) {

                            list.get(position).titleColor = 0xff5823ff;
                            list.get(position).iconId = R.drawable.checkbox;
                            points2Travel.add(object.get(position - 1));
                            //Toast.makeText(MapActivity.this, points2Travel.get(0).getPname(), Toast.LENGTH_SHORT).show();
                        } else if (list.get(position).iconId == R.drawable.checkbox && position != 0) {
                            list.get(position).titleColor = 0xff000000;
                            list.get(position).iconId = R.drawable.checkbox_empty;
                        }
                        ((RecyclerViewDelegate) mSweetSheet.getDelegate()).notifyDataSetChanged();

                        //根据返回值, true 会关闭 SweetSheet ,false 则不会.
                        //Toast.makeText(MapActivity.this, menuEntity1.title + "  " + position, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });

                mSweetSheet2 = new SweetSheet(rl);

                final ArrayList<MenuEntity> list2 = getData(object);
                mSweetSheet2.setMenuList(list2);

                mSweetSheet2.setDelegate(new RecyclerViewDelegate(true));
                //根据设置不同Effect 来显示背景效果BlurEffect:模糊效果.DimEffect 变暗效果
                mSweetSheet2.setBackgroundEffect(new BlurEffect(8));
                //设置点击事件
                mSweetSheet2.setOnMenuItemClickListener(new SweetSheet.OnMenuItemClickListener() {
                    @Override
                    public boolean onItemClick(int position, MenuEntity menuEntity1) {
                        //即时改变当前项的颜色
                        if (list2.get(position).iconId == R.drawable.checkbox_empty && position != 0) {

                            list2.get(position).titleColor = 0xff5823ff;
                            //menuEntity1.titleColor = 0xffff00;
                            list2.get(position).iconId = R.drawable.checkbox;

                            points2Travel.add(object.get(position - 1));
                        } else if (list.get(position).iconId == R.drawable.checkbox && position != 0) {
                            list2.get(position).titleColor = 0xff000000;
                            list2.get(position).iconId = R.drawable.checkbox_empty;

                        }
                        ((RecyclerViewDelegate) mSweetSheet2.getDelegate()).notifyDataSetChanged();

                        //根据返回值, true 会关闭 SweetSheet ,false 则不会.
                        //Toast.makeText(MapActivity.this, menuEntity1.title + "  " + position, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });

                mSweetSheet3 = new SweetSheet(rl);

                final ArrayList<MenuEntity> list3 = getData(object);
                mSweetSheet3.setMenuList(list3);

                mSweetSheet3.setDelegate(new RecyclerViewDelegate(true));
                //根据设置不同Effect 来显示背景效果BlurEffect:模糊效果.DimEffect 变暗效果
                mSweetSheet3.setBackgroundEffect(new BlurEffect(8));
                //设置点击事件
                mSweetSheet3.setOnMenuItemClickListener(new SweetSheet.OnMenuItemClickListener() {
                    @Override
                    public boolean onItemClick(int position, MenuEntity menuEntity1) {
                        //即时改变当前项的颜色
                        if (list3.get(position).iconId == R.drawable.checkbox_empty && position != 0) {

                            list3.get(position).titleColor = 0xff5823ff;
                            list3.get(position).iconId = R.drawable.checkbox;
                            points2Travel.add(object.get(position - 1));
                        } else if ( list3.get(position).iconId == R.drawable.checkbox && position != 0) {
                            list3.get(position).titleColor = 0xff000000;
                            list3.get(position).iconId = R.drawable.checkbox_empty;
                        }
                        ((RecyclerViewDelegate) mSweetSheet3.getDelegate()).notifyDataSetChanged();

                        //根据返回值, true 会关闭 SweetSheet ,false 则不会.
                        //Toast.makeText(MapActivity.this, menuEntity1.title + "  " + position, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
            }

            @Override
            public void onError(int code, String msg) {
                // TODO Auto-generated method stub
                Toast.makeText(MapActivity.this, "查询失败：" + msg,
                        Toast.LENGTH_SHORT).show();
            }
        });
//        return pointList;
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        option.setScanSpan(UPDATE_TIME);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

    //位置监听器
    public class MyLocationListener implements BDLocationListener
    {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mapView == null) {
                return;
            }
            //保存位置信息
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 单位度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }
            Log.i("BaiduLocation", sb.toString());

            //在地图上显示位置信息
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            //设置定位数据
            baiduMap.setMyLocationData(locData);
            // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
            if (isFirstLoc)
            {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }
    }

    //地图加载，初始化MapStatus
//    @Override
//    public void onMapLoaded() {
//        ms = new MapStatus.Builder().zoom(9).build();
//        baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(ms));
//    }

    /**
     * 向地图添加Marker点！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
     * 在这里封装每个坐标点的数据
     */
    public void addMarkers() {
        // 添加Marker点
//        LatLng llA = new LatLng(39.963175, 116.400244);
//        LatLng llB = new LatLng(39.942821, 116.369199);
//        LatLng llC = new LatLng(39.939723, 116.425541);
//        LatLng llD = new LatLng(39.906965, 116.401394);
//        LatLng llE = new LatLng(39.956965, 116.331394);
//        LatLng llF = new LatLng(39.886965, 116.441394);
//        LatLng llG = new LatLng(39.996965, 116.411394);

        List<MyItem> items = new ArrayList<MyItem>();

        for(Point p : points2Travel)
        {
            items.add(new MyItem(new LatLng(
                    Double.parseDouble(p.getPlat()),Double.parseDouble(p.getPlong()))));
        }
//        items.add(new MyItem(llA));
//        items.add(new MyItem(llB));
//        items.add(new MyItem(llC));
//        items.add(new MyItem(llD));
//        items.add(new MyItem(llE));
//        items.add(new MyItem(llF));
//        items.add(new MyItem(llG));
        mClusterManager.addItems(items);
    }

    /**
     * 每个Marker点，包含Marker点坐标以及图标
     */
    public class MyItem implements ClusterItem {
        private final LatLng mPosition;

        public MyItem(LatLng latLng) {
            mPosition = latLng;
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }

        @Override
        public BitmapDescriptor getBitmapDescriptor() {
            return BitmapDescriptorFactory
                    .fromResource(R.drawable.icon_gcoding);
        }
    }

    /*Marker点击事件*/
    public class myMarkerListener implements BaiduMap.OnMarkerClickListener
    {
        @Override
        public boolean onMarkerClick(Marker marker) {
            Log.d("marker",marker.toString());
            Intent intent = new Intent(MapActivity.this, CommentActivity.class);
            startActivity(intent);
            return false;
        }
    }
}
