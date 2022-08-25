package com.example.weatherforecast.presentation.fragments.cityselection

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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.weatherforecast.R
import com.example.weatherforecast.databinding.FragmentCitiesNamesBinding
import com.example.weatherforecast.models.domain.CitiesNamesDomainModel
import com.example.weatherforecast.network.NetworkMonitor
import com.example.weatherforecast.presentation.PresentationUtils
import com.example.weatherforecast.presentation.viewmodel.cityselection.CitiesNamesViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Represents a feature of choosing a city to further have a weather forecast on.
 */
@AndroidEntryPoint
class CitiesNamesFragment : Fragment() {

    private var chosenCity = ""

    private val viewModel by activityViewModels<CitiesNamesViewModel>()

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
        autoSuggestAdapter = AutoSuggestAdapter(activity as Context, android.R.layout.select_dialog_item)
        fragmentDataBinding.toolbar.title = getString(R.string.app_name)
        fragmentDataBinding.toolbar.subtitle = getString(R.string.city_selection_title)
        initLiveDataObservers()
        initSearch()
        NetworkMonitor(requireContext(), viewModel)
    }

    private fun initLiveDataObservers() {
        viewModel.onGetCitiesNamesLiveData.observe(viewLifecycleOwner) { showCitiesList(it) }
        viewModel.onShowErrorLiveData.observe(viewLifecycleOwner) { showError(it) }
        viewModel.onUpdateStatusLiveData.observe(viewLifecycleOwner) { updateStatus(it) }
    }

    private fun initSearch() {
        fragmentDataBinding.autocompleteCity.setAdapter(autoSuggestAdapter)
        fragmentDataBinding.autocompleteCity.threshold = 2
        fragmentDataBinding.autocompleteCity.onItemClickListener = clickListener
        fragmentDataBinding.autocompleteCity.addTextChangedListener(textChangeListener)
    }

    private fun showError(errorMessage: String) {
        Log.e("CitiesNamesFragment", errorMessage)
        fragmentDataBinding.toolbar.subtitle = errorMessage
        PresentationUtils.setToolbarSubtitleFontSize(fragmentDataBinding.toolbar, errorMessage)
        fragmentDataBinding.toolbar.setBackgroundColor((activity as Context).getColor(R.color.colorAccent))
    }

    private fun updateStatus(statusMessage: String) {
        fragmentDataBinding.toolbar.subtitle = statusMessage
        PresentationUtils.setToolbarSubtitleFontSize(fragmentDataBinding.toolbar, statusMessage)
        fragmentDataBinding.toolbar.setBackgroundColor((activity as Context).getColor(R.color.colorPrimary))
    }

    private fun showCitiesList(citiesModel: CitiesNamesDomainModel) {
        updateStatus(getString(R.string.choose_city_from_dropdown))
        autoSuggestAdapter.setData(citiesModel.cities)
        autoSuggestAdapter.notifyDataSetChanged()
    }

    private val textChangeListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Not used
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (!s.isNullOrBlank()) {
                viewModel.getCitiesNamesForMask(s.toString())
            }
        }

        override fun afterTextChanged(s: Editable?) {
            // Not used
        }
    }

    private val clickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
        chosenCity = autoSuggestAdapter.getItem(position)
        gotoForecastFragment(chosenCity)
    }

    private fun gotoForecastFragment(chosenCity: String) {
        val action = CitiesNamesFragmentDirections.actionCitiesNamesFragmentToCurrentTimeForecastFragment(chosenCity)
        findNavController().navigate(action)
    }
}