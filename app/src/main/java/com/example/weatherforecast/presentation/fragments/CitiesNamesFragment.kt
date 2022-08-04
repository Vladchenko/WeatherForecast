package com.example.weatherforecast.presentation.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.weatherforecast.R
import com.example.weatherforecast.data.models.domain.CityDomainModel
import com.example.weatherforecast.databinding.FragmentCitiesNamesBinding
import com.example.weatherforecast.presentation.WeatherForecastActivity
import com.example.weatherforecast.presentation.fragments.CurrentTimeForecastFragment.Companion.CITY_ARGUMENT_KEY
import com.example.weatherforecast.presentation.viewmodel.CitiesNamesViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Represents a feature of choosing a city to further have a weather forecast on.
 */
@AndroidEntryPoint
class CitiesNamesFragment : Fragment() {

    private var city = ""

    private lateinit var viewModel: CitiesNamesViewModel
    private lateinit var autoSuggestAdapter: AutoSuggestAdapter
    private lateinit var fragmentDataBinding: FragmentCitiesNamesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cities_names, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentDataBinding = FragmentCitiesNamesBinding.bind(view)
        viewModel = (activity as WeatherForecastActivity).citiesNamesViewModel
        autoSuggestAdapter = AutoSuggestAdapter(activity as Context, android.R.layout.select_dialog_item)
        fragmentDataBinding.errorTextView.visibility =View.INVISIBLE
        observeCitiesNamesResponse()
        initSearch()
    }

    private fun observeCitiesNamesResponse() {
        viewModel.getCitiesNamesLiveData.observe(viewLifecycleOwner) {
            autoSuggestAdapter.setData(it.cities)
            autoSuggestAdapter.notifyDataSetChanged()
        }
        viewModel.showErrorLiveData.observe(viewLifecycleOwner) {
            Log.e("CitiesNamesFragment",it)
            fragmentDataBinding.errorTextView.text = it
            fragmentDataBinding.errorTextView.visibility =View.VISIBLE
        }
        viewModel.gotoOutdatedForecastLiveData.observe(viewLifecycleOwner) {
            val bundle = Bundle().apply {
                putSerializable(CITY_ARGUMENT_KEY, city)
            }
            findNavController().navigate(
                R.id.action_citiesNamesFragment_to_currentTimeForecastFragment,
                bundle
            )
        }
    }

    private fun initSearch() {
        fragmentDataBinding.autocompleteCity.setAdapter(autoSuggestAdapter)
        fragmentDataBinding.autocompleteCity.threshold = 2
        fragmentDataBinding.autocompleteCity.onItemClickListener = clickListener
        fragmentDataBinding.autocompleteCity.addTextChangedListener(textChangeListener)
    }

    private val textChangeListener = object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not used
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrBlank()) {
                    viewModel.getCitiesNames(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Not used
            }
        }

    private val clickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
        city = autoSuggestAdapter.getItem(position)
        viewModel.saveChosenCity(CityDomainModel(city,0.0,0.0,"",""))
        val bundle = Bundle().apply {
            putSerializable(CITY_ARGUMENT_KEY, city)
        }
        findNavController().navigate(
            R.id.action_citiesNamesFragment_to_currentTimeForecastFragment,
            bundle
        )
    }
}