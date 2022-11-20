package com.sunnyweather.android.logic.model

import com.sunnyweather.android.SunnyWeatherApplication
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PlaceService {

    //这样当调用searchPlaces()方法的时候，Retrofit就会自动发起一条GET请求，去访问GET注解中配置的地址
    //搜素城市数据的API中只有query这个参数是需要动态指定的，我们使用@Query注解的方式来实现，另外两个参数是不会变的，因此固定写在@GET注解中即可
    @GET("v2/place?token=${SunnyWeatherApplication.TOKEN}&lang=zh_CN")
    //返回值被声明成了Call<PlaceResponse>，这样Retrofit就会将服务器返回的JSON数据自动解析成PlaceResponse对象了
    fun searchPlaces(@Query("query") query: String): Call<PlaceResponse>
}