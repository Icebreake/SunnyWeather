package com.sunnyweather.android.ui.weather

import android.content.Context
import android.graphics.Color
import android.hardware.input.InputManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sunnyweather.android.R
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import kotlinx.android.synthetic.main.activity_weather.*
import kotlinx.android.synthetic.main.forecast.*
import kotlinx.android.synthetic.main.life_index.*
import kotlinx.android.synthetic.main.now.*
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity() {
    
    val viewModel by lazy { ViewModelProvider(this).get(WeatherViewModel::class.java) }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*WindowInsetsControllerCompat(window, window.decorView).let {
            it.hide(WindowInsetsCompat.Type.navigationBars())
            it.systemBarsBehavior
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }*/

        //拿到当前Activity的DecorView
        val decorView = window.decorView
        //调用systemUiVisibility()方法来改变系统UI的显示
        //SYSTEM_UI_FLAG_LAYOUT_STABLE就表示Activity的布局会显示在状态栏上面
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        //将状态栏设置成透明色
        window.statusBarColor = Color.TRANSPARENT
        setContentView(R.layout.activity_weather)

        //先从Intent中取出经纬度坐标和地区名称，并赋值到WeatherViewModel的相应变量中
        if (viewModel.locationLng.isEmpty()) {
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }
        if (viewModel.locationLat.isEmpty()) {
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }
        if (viewModel.placeName.isEmpty()) {
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }

        //然后对weatherLiveData对象进行观察
        viewModel.weatherLiveData.observe(this, Observer { result ->
            val weather = result.getOrNull()
            //当获取到服务器返回的天气数据时，就调用showWeatherInfo()方法进行解析与展示
            if (weather != null) {
                showWeatherInfo(weather)
            } else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            //当请求结束后，将SwipeRefreshLayout的isRefreshing属性设置成false，表示刷新事件结束，并隐藏刷新进度条
            swipeRefresh.isRefreshing = false
        })
        //设置下拉刷新进度条的颜色，就使用colors.xml中的colorPrimary作为进度条的颜色
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        refreshWeather()
        //给SwipeRefreshLayout设置了一个下拉刷新的监听器，当触发了下拉刷新操作的时候，就在监听器的回调中调用refreshWeather()来刷新天气信息
        swipeRefresh.setOnRefreshListener {
            refreshWeather()
        }

        //在切换城市按钮的点击事件中调用DrawLayout的openDrawer()方法来打开滑动菜单
        navBtn.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        //监听DrawLayout的状态
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {}

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {}

            override fun onDrawerClosed(drawerView: View) {
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                //当滑动菜单被隐藏的时候，同时也有隐藏输入法，而如果滑动菜单隐藏后输入法却还显示在界面上，带来不好的用户体验
                manager.hideSoftInputFromWindow(drawerView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        })
    }

    fun refreshWeather() {
        //将之前用于刷新天气信息的代码提取到了一个新的refreshWeather()中
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
        //将SwipeRefreshLayout的isRefreshing属性设置成true，从而让下拉刷新进度条显示出来
        swipeRefresh.isRefreshing = true
    }

    //从Weather对象中获取数据，然后显示到相应的控件上
    private fun showWeatherInfo(weather: Weather) {
        placeName.text = viewModel.placeName
        val realtime = weather.realtime
        val daily = weather.daily
        //填充now.xml布局中的数据
        val currentTempText = "${realtime.temperature.toInt()} ℃"
        currentTemp.text = currentTempText
        currentSky.text = getSky(realtime.skycon).info
        val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        currentAQI.text = currentPM25Text
        nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)
        //填充forecast.xml布局中的数据
        forecastLayout.removeAllViews()
        val days = daily.skycon.size
        //使用循环来处理每天的天气信息，在循环中动态加载forecast.xml布局并设置相应的数据，然后添加到父布局中
        for (i in 0 until days) {
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false)
            val dateInfo = view.findViewById(R.id.dateInfo) as TextView
            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
            val skyInfo = view.findViewById(R.id.skyInfo) as TextView
            val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateInfo.text = simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
            temperatureInfo.text = tempText
            forecastLayout.addView(view)
        }
        //填充life_index.xml布局中的数据
        val lifeIndex = daily.lifeIndex
        //生活指数方面虽然服务器会返回很多天的数据，但是界面上只需要当天的数据即可，因此这里我们对所有的生活指数都取了下标为0的那个元素的数据
        coldRiskText.text = lifeIndex.coldRisk[0].desc
        dressingText.text = lifeIndex.dressing[0].desc
        ultravioletText.text = lifeIndex.ultraviolet[0].desc
        carWashingText.text = lifeIndex.carWashing[0].desc
        //让ScrollView变成可见状态
        weatherLayout.visibility = View.VISIBLE
    }

}