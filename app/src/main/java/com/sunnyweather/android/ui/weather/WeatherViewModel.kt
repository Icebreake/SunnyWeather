package com.sunnyweather.android.ui.weather

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Location

class WeatherViewModel : ViewModel() {

    private val locationLiveData = MutableLiveData<Location>()

    //以下3个都是和界面相关的数据，放在ViewModel中可以保证它们在手机屏幕发生旋转的时候不会丢失，稍后在编写UI层代码的时候会用到这几个变量
    var locationLng = ""

    var locationLat = ""

    var placeName = ""

    //3.然后调用Transformations的switchMap()方法来观察这个对象
    val weatherLiveData = Transformations.switchMap(locationLiveData) { location ->
        //4.并在switchMap()方法的转换函数中调用仓库层中定义的refreshWeather()方法，这样仓库层返回的LiveData对象就可以转换成一个可供Activity观察的LiveData对象了
        Repository.refreshWeather(location.lng, location.lat)
    }

    //1.刷新天气信息
    fun refreshWeather(lng: String, lat: String) {
        //2.将传入的经纬度参数封装成一个Location对象后赋值给locationLiveData对象
        locationLiveData.value = Location(lng, lat)
    }

}