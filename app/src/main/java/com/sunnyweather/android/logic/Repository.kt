package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.dao.PlaceDao
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext

object Repository {

    fun searchPlaces(query: String) = fire(Dispatchers.IO) {
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
    }

    /*
    在仓库层并没有提供两个分别用于获取实时天气信息和未来天气信息的方法，而是提供了一个refreshWeather()方法来刷新天气信息
    因为对于调用方而言，需要调用两次请求才能获得其想要的所有天气数据明显是比较烦琐的行为，因此最好的做法就是在仓库层再进行一次统一的封装
     */
    fun refreshWeather(lng: String, lat: String) = fire(Dispatchers.IO) {
        /*
        获取实时天气信息和获取未来天气信息这两个请求是没有先后顺序的，因此让它们并发执行可以提示程序的运行效率，但是要在同时得到它们的响应结果后才能进一步执行程序
        使用协程的async函数
        只需要分别在两个async函数中发起网络请求，然后再分别调用它们的await()方法，就可以保证只有再两个网络请求都成功响应之后，才会进一步执行程序
        由于async函数必须在协程作用域内才能调用，所以这里又使用coroutineScope函数创建了一个协程作用域
         */
        coroutineScope {
            val deferredRealtime = async {
                SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
            }
            val deferredDaily = async {
                SunnyWeatherNetwork.getDailyWeather(lng, lat)
            }
            val realtimeResponse = deferredRealtime.await()
            val dailyResponse = deferredDaily.await()
            //在同时获取到RealtimeResponse和DailyResponse之后，如果它们的响应状态都是ok
            if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                //那么就将Realtime和Daily对象取出并封装到一个Weather对象中
                val weather = Weather(realtimeResponse.result.realtime, dailyResponse.result.daily)
                Result.success(weather)
            } else {
                //否则就使用Result.failure()方法来包装一个异常信息
                Result.failure(
                    RuntimeException(
                        "realtime response status is ${realtimeResponse.status}" +
                                "daily response status is ${dailyResponse.status}"
                    )
                )
            }
        }
    }

    /*
    这是一个按照liveData()函数的参数接收标准定义的一个高阶函数
    在fire()函数的内部会先调用一下liveData()函数，然后在liveData()函数的代码块中统一进行了try catch处理
     */
    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) = liveData<Result<T>>(context) {
        //在try语句中调用传入的Lambda表达式中的代码
        val result = try {
            block()
        } catch (e: Exception) {
            Result.failure<T>(e)
        }
        //最终获取Lambda表达式的执行结果并调用emit()方法发射出去
        emit(result)
    }

    /*
    这里仓库层只是做了一层接口封装而已，其实这里的实现方式并不标准，因为即使是对SharedPreferences文件进行读写的操作
    也不建议在主线程中进行，虽然它的执行速度通常会很快
    最佳的实现方式是开启一个线程来执行这些比较耗时的任务，然后通过LiveData对象进行数据返回
    这里为了让代码看起来更简单一些，没有使用标准的写法
     */
    fun savePlace(place: Place) = PlaceDao.savePlace(place)

    fun getSavedPlace() = PlaceDao.getSavedPlace()

    fun isPlaceSaved() = PlaceDao.isPlaceSaved()

}