package com.chengziweather.hoperun.view.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.chengziweather.hoperun.R;
import com.chengziweather.hoperun.db.City;
import com.chengziweather.hoperun.db.County;
import com.chengziweather.hoperun.db.Province;
import com.chengziweather.hoperun.utils.HttpUtil;
import com.chengziweather.hoperun.utils.Utility;
import com.chengziweather.hoperun.view.MainActivity;
import com.chengziweather.hoperun.view.WeatherActivity;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by hoperun on 18-4-20.
 * 由于遍历全国省市县的功能在后面会复用（因此不写在活动中而是写在碎片里），所以写在碎片里面，复用时只需要引用碎片即可。
 */

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backBtn;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList=new ArrayList<>();

    private List<Province> provinceList;    //省列表
    private List<City> cityList;            //城市列表
    private List<County> countyList;        //县列表
    private Province selectedProvince;      //选中的省份
    private City selectedCity;       //选中的城市
    private int currentLevel;        //当前选中的级别

    /**
     *获取部分控件的实例，初始化ArrayAdapter并设置其为ListView的适配器
     */
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_area,container,false);
        titleText=(TextView)view.findViewById(R.id.title_text);
        listView=(ListView)view.findViewById(R.id.list_view);
        backBtn=(Button)view.findViewById(R.id.back_btn);
        adapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }
    /**
     *为ListView和Button设置点击事件。点击了某个省后，就会进入点击事件，然后根据当前级别来判断去调用queryXxx();
     * 返回按钮的点击事件则是对当前级别进行判断，当前为县级则返回市级，当前为市级，则返回省级，当前为省级则隐藏
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel==LEVEL_PROVINCE){
                    selectedProvince=provinceList.get(position);
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(position);
                    queryCounties();
                }else if(currentLevel==LEVEL_COUNTY){
                    /**
                     *通过Intent完成从省市县列表界面跳转到天气界面
                     *城市切换后的逻辑，使用了java中的instanceof关键字，轻易的判断出碎片在哪个活动中，
                     * 在MainActivity中处理逻辑不变，若是在WeatherActivity中则关闭滑动菜单，显示下拉进度条，
                     * 然后请求新城市的天气信息。
                     */
                    String weatherId=countyList.get(position).getWeatherId();
                    if(getActivity() instanceof MainActivity){
                        Intent intent=new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id",weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if(getActivity() instanceof WeatherActivity){
                        WeatherActivity activity=(WeatherActivity)getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefresh.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }
                }
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel==LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();       //从这里开始加载省级数据的
    }
   /**
    * 查询全国所有的省,优先从数据库查询,如果没有查询到再去服务器上查询
    */
    private void queryProvinces() {
        titleText.setText("中国");        //将头布局的标题设置为中国
        backBtn.setVisibility(View.GONE);       //将返回按钮隐藏起来，省级列表已经不能返回了，是最顶级的数据了
        provinceList= DataSupport.findAll(Province.class);  //调用LitePal查询接口从数据库中读取省级数据
        if(provinceList.size()>0){  //读取到数据就将其显示在界面上
            dataList.clear();
            for(Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else {         //若没有读取到就组装出一个请求地址，调用queryFromServer()方法来从服务器上查询数据
            String address="http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }
    /**
     * 查询选中省内所有的市,优先从数据库查询,如果没有查询到再去服务器上查询
     */
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backBtn.setVisibility(View.VISIBLE);
        cityList= DataSupport.where("provinceid=?",String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size()>0){
            dataList.clear();
            for(City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else {
            int provinceCode=selectedProvince.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }
    }
    /**
     * 查询选中市内所有的县,优先从数据库查询,如果没有查询到再去服务器上查询
     */
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backBtn.setVisibility(View.VISIBLE);
        countyList= DataSupport.where("cityid=?",String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size()>0){
            dataList.clear();
            for(County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }else {
            int provinceCode=selectedProvince.getProvinceCode();
            int cityCode=selectedCity.getCityCode();
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }
    /**
     * 根据传入的地址和类型从服务器上查询省市县数据
     */
    private void queryFromServer(String address,final String type) {
        showProgressDialog();       //显示进度条对话框
        /**
         *调用HttpUtil.sendOkHttpRequest()方法来向服务器发送请求，响应的数据会回调到onResponse()方法中，
         *然后调用Utility.handleProvinceResponse()方法来解析和处理服务器返回的数据，并储存到数据库中。
         * 然后再次调用queryProvinces()方法重新加载省级数据，由于涉及UI操作，因此必须在主线程中调用，
         * 所以这里借助了runOnUiThread()来实现从子线程切换到主线程。
         */
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                if("province".equals(type)){
                    result= Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type)){
                    result=Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if("county".equals(type)){
                    result=Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败!",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }
    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if(progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}
