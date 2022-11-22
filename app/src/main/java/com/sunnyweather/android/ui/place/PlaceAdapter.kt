package com.sunnyweather.android.ui.place

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.sunnyweather.android.R
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.ui.weather.WeatherActivity
import kotlinx.android.synthetic.main.activity_weather.*

//先把PlaceAdapter主构造函数中所传入的Fragment对象改成PlaceFragment，这样就可以调用PlaceFragment所对应的PlaceViewModel了
class PlaceAdapter(private val fragment: PlaceFragment, private val placeList: List<Place>) :
        RecyclerView.Adapter<PlaceAdapter.ViewHolder>(){

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val placeName: TextView = view.findViewById(R.id.placeName)
        val placeAddress: TextView = view.findViewById(R.id.placeAddress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //给place_item.xml的最外层布局注册了一个点击事件监听器
        val view = LayoutInflater.from(parent.context).inflate(R.layout.place_item, parent, false)
        val holder = ViewHolder(view)
        holder.itemView.setOnClickListener {
            //然后在点击事件中获取当前点击项的经纬度坐标和地区名称，并把它们传入Intent中
            val position = holder.adapterPosition
            val place = placeList[position]

            val activity = fragment.activity
            //对PlaceFragment所处的Activity进行了判断，如果是在WeatherActivity中
            if (activity is WeatherActivity) {
                //那么就关闭滑动菜单
                activity.drawerLayout.closeDrawers()
                //给WeatherActivity赋值新的经纬度坐标和地区名称
                activity.viewModel.locationLng = place.location.lng
                activity.viewModel.locationLat = place.location.lat
                activity.viewModel.placeName = place.name
                //然后刷新城市的天气信息
                activity.refreshWeather()
            } else {
                //如果是在MainActivity中，那么就保持之前的处理逻辑不变即可
                val intent = Intent(parent.context, WeatherActivity::class.java).apply {
                    putExtra("location_lng", place.location.lng)
                    putExtra("location_lat", place.location.lat)
                    putExtra("place_name", place.name)
                }
                //最后调用Fragment的startActivity()方法启动WeatherActivity
                fragment.startActivity(intent)
                activity?.finish()
            }
            //当点击了任何子项布局时，在跳转到WeatherActivity之前，先调用PlaceViewModel的savePlace()方法来存储选中的城市
            fragment.viewModel.savePlace(place)
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val place = placeList[position]
        holder.placeName.text = place.name
        holder.placeAddress.text = place.address
    }

    override fun getItemCount() = placeList.size

}