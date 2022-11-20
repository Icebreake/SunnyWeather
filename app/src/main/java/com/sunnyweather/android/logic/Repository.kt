package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers

object Repository {

    /*
    这个liveData()函数时lifecycle-livedata-ktx库提供的一个非常强大且好用的功能，它可以自动构建并返回一个LiveData对象
    然后在它的代码块中提供一个挂起函数的上下文，这样我们就可以在liveData()函数的代码块中调用任意的挂起函数了
     */
    fun searchPlaces(query: String) = liveData(Dispatchers.IO) {
        val result = try {
            //调用了SunnyWeatherNetwork的searchPlaces()函数来搜索城市数据
            val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
            //判断如果服务器响应的状态是ok，那么就使用Kotlin内置的Result.success()方法来包装获取的城市数据列表
            if (placeResponse.status == "ok") {
                val places = placeResponse.places
                Result.success(places)
            } else {
                //否则使用Result.failure()方法来包装一个异常信息
                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
            }
        } catch (e : Exception) {
            Result.failure<List<Place>>(e)
        }
        //最后使用一个emit()方法将包装的结果发射出去，这个方法其实类似于调用LiveData的setValue()方法来通知数据变化，只不过这里我们无法直接取得返回的LiveData对象
        emit(result)
    }

}