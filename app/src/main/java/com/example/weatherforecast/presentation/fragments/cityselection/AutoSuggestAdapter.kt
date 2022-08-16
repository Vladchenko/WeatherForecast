package com.example.weatherforecast.presentation.fragments.cityselection

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import androidx.annotation.Nullable
import com.example.weatherforecast.data.models.domain.CityDomainModel

/**
 * Adapts the [List<CityDomainModel>] for appropriate presentation inside of [AutoCompleteTextView].
 */
class AutoSuggestAdapter(context: Context, resource: Int) : ArrayAdapter<String>(context, resource), Filterable {

    private val dataList: MutableList<CityDomainModel>
    
    fun setData(list: List<CityDomainModel>?) {
        dataList.clear()
        dataList.addAll(list!!)
    }

    override fun getCount() = dataList.size

    @Nullable
    override fun getItem(position: Int):String {
        return if (!dataList[position].state.isNullOrBlank() && dataList[position].state != dataList[position].name) {
            dataList[position].name + ", " + dataList[position].state + ", " + dataList[position].country
        } else {
            dataList[position].name + ", " + dataList[position].country
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint != null) {
                    filterResults.values = dataList
                    filterResults.count = dataList.size
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }

    init {
        dataList = ArrayList()
    }
}