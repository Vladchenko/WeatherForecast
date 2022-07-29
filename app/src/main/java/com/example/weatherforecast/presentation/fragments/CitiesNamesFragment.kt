package com.example.weatherforecast.presentation.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.navigation.fragment.findNavController
import com.example.weatherforecast.R
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

    private lateinit var autoSuggestAdapter: AutoSuggestAdapter

    private lateinit var viewModel: CitiesNamesViewModel
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
        observeCitiesNamesResponse()
        initSearch()
    }

    private fun initSearch() {
        fragmentDataBinding.autocompleteCity.setAdapter(autoSuggestAdapter)
        fragmentDataBinding.autocompleteCity.threshold = 2
        fragmentDataBinding.autocompleteCity.setOnItemClickListener { parent, view, position, id ->
            city = autoSuggestAdapter.getItem(position)
            val bundle = Bundle().apply {
                putSerializable(CITY_ARGUMENT_KEY, city)
            }
            findNavController().navigate(
                R.id.action_currentTimeForecastFragment_to_citiesNamesFragment,
                bundle
            )
        }
        fragmentDataBinding.autocompleteCity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.getCitiesNames(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeCitiesNamesResponse() {
        viewModel.getCitiesNamesLiveData.observe(this) {
            autoSuggestAdapter.setData(it.cities)
            autoSuggestAdapter.notifyDataSetChanged()
        }
        viewModel.showErrorLiveData.observe(this) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }
        viewModel.gotoOutdatedForecastLiveData.observe(this) {
            val bundle = Bundle().apply {
                putSerializable(CITY_ARGUMENT_KEY, city)
            }
            findNavController().navigate(
                R.id.action_citiesNamesFragment_to_currentTimeForecastFragment,
                bundle
            )
        }
    }
}