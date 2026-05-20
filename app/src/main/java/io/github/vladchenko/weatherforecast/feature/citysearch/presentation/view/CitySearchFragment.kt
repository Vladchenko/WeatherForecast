package io.github.vladchenko.weatherforecast.feature.citysearch.presentation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.navigation.WeatherNavigator
import io.github.vladchenko.weatherforecast.feature.citysearch.presentation.viewmodel.CitySearchViewModel
import io.github.vladchenko.weatherforecast.presentation.viewmodel.appBar.AppBarViewModel
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

/**
 * Fragment for selecting a city from a list of suggestions based on user input.
 *
 * Displays a search interface with auto-complete functionality, allowing users to type
 * and select a city name. Upon selection, navigates to the weather forecast screen.
 *
 * Uses [CitySelectionLayout] as its Compose UI root and shares ViewModels with the host activity.
 */
@AndroidEntryPoint
class CitySearchFragment : Fragment() {

    @Inject
    lateinit var resourceManager: ResourceManager

    @FlowPreview
    private val viewModel by viewModels<CitySearchViewModel>()
    private val appBarViewModel by activityViewModels<AppBarViewModel>()
    private val navigator by lazy { WeatherNavigator(findNavController()) }

    @FlowPreview
    @ExperimentalMaterial3Api
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CitySelectionLayout(
                    citySelectionTitle = getString(R.string.city_selection_hint),
                    queryLabel = getString(R.string.city_typing_begin),
                    onEvent = { event -> viewModel.onEvent(event) },
                    appBarViewModel = appBarViewModel,
                    viewModel = viewModel
                )
            }
        }
    }

    @FlowPreview
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigator.start(viewLifecycleOwner, viewModel.navigationEventFlow)
    }
}