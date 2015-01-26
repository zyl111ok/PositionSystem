package org.dreamfly.positionsystem.Activity;

import android.content.SharedPreferences;


import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import org.dreamfly.positionsystem.Database.DataBase;
import org.dreamfly.positionsystem.R;
import org.dreamfly.positionsystem.Utils.LocationUtils;

/**
 * Created by zhengyl on 15-1-13.
 * 定位界面Activity类
 */
public class PositionActivity extends ActionBarActivity implements OnGetGeoCoderResultListener {

    private TextView txtPositionLatitute,txtPositionLongitute,txtPositionLocation;
    private Button btnPositionActivityGeo;
    private LocationClient locationClient = null;
    private DataBase mDataBase = new DataBase(this);
    private LocationUtils mLocationUtils;
    private MapView mMapView=null;
    private BaiduMap mBaiduMap;
    private String sb,sb1;
    com.baidu.mapapi.search.geocode.GeoCoder mcoder;
    private MyLocationConfiguration.LocationMode mCurrentMode =
            MyLocationConfiguration.LocationMode.NORMAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext() /*
        //定义坐标点
        LatLng point=new LatLng
                (Float.valueOf(txt1.getText().toString()),Float.valueOf(txt2.getText().toString()));

        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_marka);

        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap);
        //在地图上添加Marker，并显示
        mBaiduMap.addOverlay(option);*/);
        String libName="BaiduMapSDK_v3_2_0_11";
        System.loadLibrary(libName);

        this.setContentView(R.layout.position_layout);
        this.initial(); /*
        //定义坐标点
        LatLng point=new LatLng
                (Float.valueOf(txt1.getText().toString()),Float.valueOf(txt2.getText().toString()));

        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_marka);

        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap);
        //在地图上添加Marker，并显示
        mBaiduMap.addOverlay(option);*/
        this.bindListener();
      }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        mMapView.onDestroy();
        locationClient.stop();
    }
    @Override
    protected void onResume(){
        super.onResume();
        mMapView.onResume();
    }
    @Override
    protected void onPause(){
        super.onPause();
        mMapView.onPause();
    }

    private void initial(){
        this.bindID();
        mLocationUtils=new LocationUtils(this);
        mLocationUtils.LocationInfo();
        this.locationInfo();
        mcoder=GeoCoder.newInstance();
        mcoder.setOnGetGeoCodeResultListener(this);


    }

    /**
     * 显示系统得到的经纬度
     * @param txt
     * @param txt1
     * @param txt2
     */

    /**
     * 绑定控件ID
     */
    private void bindID(){
       txtPositionLatitute=(TextView)this.findViewById(R.id.txt_position_latitute);
       txtPositionLongitute=(TextView)this.findViewById(R.id.txt_position_longitute);
       txtPositionLocation=(TextView)this.findViewById(R.id.txt_position_location);
       btnPositionActivityGeo=(Button)this.findViewById(R.id.btn_positionactivity_geo);
       mMapView=(MapView)this.findViewById(R.id.bmapView);

    }

    /**
     * 初始化定位服务信息
     */
    private void locationInfo(){
        locationClient=mLocationUtils.getLocationClient();
        BDListener bdListener=new BDListener();
        locationClient.registerLocationListener(bdListener);

        locationClient.start();
        locationClient.requestLocation();


    }
    public class BDListener implements com.baidu.location.BDLocationListener{
        @Override
        public void onReceiveLocation(BDLocation location) {
            if(location == null){
                return;
            }

            sb=location.getLatitude()+"";

            sb1=location.getLongitude()+"";

            txtPositionLatitute.setText(sb);
            txtPositionLongitute.setText(sb1);
            MapInfo(txtPositionLatitute, txtPositionLongitute);

        }

        @Override
        public void onReceivePoi(BDLocation bdLocation) {

        }
    }


    /**
     * 绑定按钮监听
     */
    private void bindListener() {
        this.btnPositionActivityGeo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng ptCenter=new LatLng(
                        (Float.valueOf(txtPositionLatitute.getText().toString())),
                        Float.valueOf(txtPositionLongitute.getText().toString()));
                mcoder.reverseGeoCode(new ReverseGeoCodeOption().location(ptCenter));
            }
        });
    }

    /**
     * 百度地图服务
     */
    protected void MapInfo(TextView txt1,TextView txt2){
        mBaiduMap=mMapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setTrafficEnabled(true);
        mBaiduMap.setMyLocationEnabled(true);
        MyLocationData locData=new MyLocationData.Builder().accuracy(0).direction(100)
                .latitude(Float.valueOf(txt1.getText().toString())).
                        longitude(Float.valueOf(txt2.getText().toString())).build();
        mBaiduMap.setMyLocationData(locData);
        MyLocationConfiguration config = new MyLocationConfiguration
                (mCurrentMode, true, BitmapDescriptorFactory.fromResource(R.drawable.icon_marka));
       mBaiduMap.setMyLocationConfigeration(config);
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {

    }
    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(PositionActivity.this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        txtPositionLocation.setText(result.getAddress());

    }

}
