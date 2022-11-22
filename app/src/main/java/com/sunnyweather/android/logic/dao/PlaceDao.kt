package com.sunnyweather.android.logic.dao

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.Place

object PlaceDao {

    //封装了几个必要的存储和读取数据的接口

    //用于将Place对象存储到SharedPreferences文件中
    fun savePlace(place: Place) {
        sharedPreferences().edit {
            //先通过GSON将Place对象转成一个JSON字符串，然后就可以用字符串存储的方式来保存数据
            putString("place", Gson().toJson(place))
        }
    }

    fun getSavedPlace(): Place {
        val placeJson = sharedPreferences().getString("place", "")
        return Gson().fromJson(placeJson, Place::class.java)
    }

    //判断是否有数据已被存储
    fun isPlaceSaved() = sharedPreferences().contains("place")

    private fun sharedPreferences() = SunnyWeatherApplication.context.getSharedPreferences("sunny_weather", Context.MODE_PRIVATE)

}