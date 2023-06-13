package ksu.katara.healthymealplanner.mvvm.screens.main.tabs.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import ksu.katara.healthymealplanner.R
import ksu.katara.healthymealplanner.databinding.FragmentHomeBinding
import ksu.katara.healthymealplanner.mvvm.Repositories
import ksu.katara.healthymealplanner.mvvm.model.dietTips.entities.DietTip
import ksu.katara.healthymealplanner.mvvm.model.meal.MealTypes
import ksu.katara.healthymealplanner.mvvm.screens.main.tabs.TabsFragmentDirections
import ksu.katara.healthymealplanner.mvvm.screens.main.tabs.home.diettips.DietTipsAdapter
import ksu.katara.healthymealplanner.mvvm.screens.main.tabs.home.diettips.DietTipsViewModel
import ksu.katara.healthymealplanner.mvvm.screens.main.tabs.mealplan.mealplanfordate.MealPlanViewModel
import ksu.katara.healthymealplanner.mvvm.tasks.EmptyResult
import ksu.katara.healthymealplanner.mvvm.tasks.ErrorResult
import ksu.katara.healthymealplanner.mvvm.tasks.PendingResult
import ksu.katara.healthymealplanner.mvvm.tasks.SuccessResult
import ksu.katara.healthymealplanner.mvvm.utils.findTopNavController
import ksu.katara.healthymealplanner.mvvm.utils.viewModelCreator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val sdf = SimpleDateFormat("dd-M-yyyy", Locale.ENGLISH)

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var dietTipsAdapter: DietTipsAdapter

    private val dietTipsViewModel by viewModelCreator {
        DietTipsViewModel(Repositories.dietTipsRepository)
    }

    private val mealPlanViewModel by viewModelCreator {
        MealPlanViewModel(Repositories.mealPlanForDateRecipesRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        initView()
    }

    private fun initView() {
        initProfile()
        initDietTips()
        initMealPlanForDate(currentDate = Date())
        initDiaryDetails()
    }

    private fun initProfile() {
        binding.profileDetailsButton.setOnClickListener { onProfileDetailsButtonPressed() }
    }

    private fun initDiaryDetails() {
        binding.diaryDetailsButton.setOnClickListener { onDiaryDetailsButtonPressed() }
    }

    private fun onDiaryDetailsButtonPressed() {
        Toast.makeText(requireContext(), R.string.toast_functionality_is_not_available, Toast.LENGTH_SHORT).show()
    }

    private fun onProfileDetailsButtonPressed() {
        Toast.makeText(requireContext(), R.string.toast_functionality_is_not_available, Toast.LENGTH_SHORT).show()
    }

    private fun initMealPlanForDate(currentDate: Date) = with(binding) {
        hideAllMealPlanForToday()
        mealPlanViewModel.isMealPlanLoaded.observe(viewLifecycleOwner) {
            if (it) {
                breakfastForDateDetailsButton.visibility = View.VISIBLE
                lunchForDateDetailsButton.visibility = View.VISIBLE
                dinnerForDateDetailsButton.visibility = View.VISIBLE
                snackForDateDetailsButton.visibility = View.VISIBLE

                mealPlanForTodayProgressBar.visibility = View.INVISIBLE
            } else {
                dietTipTryAgainContainer.visibility = View.VISIBLE
            }
        }
        mealPlanViewModel.actionShowToast.observe(viewLifecycleOwner) {
            it.getValue()?.let { messageRes -> Toast.makeText(requireContext(), messageRes, Toast.LENGTH_SHORT).show() }
        }
        breakfastForDateDetailsButton.setOnClickListener {
            onMealPlanForDateItemPressed(MealTypes.BREAKFAST, currentDate)
        }
        lunchForDateDetailsButton.setOnClickListener {
            onMealPlanForDateItemPressed(MealTypes.LUNCH, currentDate)
        }
        dinnerForDateDetailsButton.setOnClickListener {
            onMealPlanForDateItemPressed(MealTypes.DINNER, currentDate)
        }
        snackForDateDetailsButton.setOnClickListener {
            onMealPlanForDateItemPressed(MealTypes.SNACK, currentDate)
        }
    }

    private fun hideAllMealPlanForToday() {
        with(binding) {
            breakfastForDateDetailsButton.visibility = View.GONE
            lunchForDateDetailsButton.visibility = View.GONE
            dinnerForDateDetailsButton.visibility = View.GONE
            snackForDateDetailsButton.visibility = View.GONE
            mealPlanForTodayTryAgainContainer.visibility = View.GONE
        }
    }

    private fun onMealPlanForDateItemPressed(mealType: MealTypes, currentDate: Date) {
        val date = sdf.format(currentDate)
        val direction = TabsFragmentDirections.actionTabsFragmentToRecipesFragment(date, mealType)
        findTopNavController().navigate(direction)
    }

    private fun initDietTips() {
        binding.dietTipsMoreTextView.setOnClickListener {
            onMorePressed()
        }
        initDietTipsRecycleView()
    }

    private fun onMorePressed() {
        val directions = TabsFragmentDirections.actionTabsFragmentToDietTipsChaptersFragment()
        findTopNavController().navigate(directions)
    }

    private fun initDietTipsRecycleView() {
        dietTipsAdapter = DietTipsAdapter(dietTipsViewModel)
        dietTipsViewModel.dietTips.observe(viewLifecycleOwner) {
            hideAll()
            when (it) {
                is SuccessResult -> {
                    dietTipsAdapter.dietTips = it.data.slice(0..AMOUNT_OF_DIET_TIPS)
                    binding.dietTipsRecyclerView.visibility = View.VISIBLE
                }

                is ErrorResult -> {
                    binding.dietTipTryAgainContainer.visibility = View.VISIBLE
                }

                is PendingResult -> {
                    binding.dietTipsProgressBar.visibility = View.VISIBLE
                }

                is EmptyResult -> {
                    binding.noDietTipsTextView.visibility = View.VISIBLE
                }
            }
        }
        dietTipsViewModel.actionShowToast.observe(viewLifecycleOwner) {
            it.getValue()?.let { messageRes -> Toast.makeText(requireContext(), messageRes, Toast.LENGTH_SHORT).show() }
        }
        dietTipsViewModel.actionShowDetails.observe(viewLifecycleOwner) {
            it.getValue()?.let { dietTip -> onDietTipPressed(dietTip) }
        }
        val dietTipsLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.dietTipsRecyclerView.layoutManager = dietTipsLayoutManager
        binding.dietTipsRecyclerView.adapter = dietTipsAdapter
    }

    private fun onDietTipPressed(dietTip: DietTip) {
        val dietTipArg = dietTip.id
        val direction =
            TabsFragmentDirections.actionTabsFragmentToDietTipDetailsFragment(dietTipArg)
        findTopNavController().navigate(direction)
    }

    private fun hideAll() = with(binding) {
        dietTipsRecyclerView.visibility = View.GONE
        dietTipsProgressBar.visibility = View.GONE
        dietTipTryAgainContainer.visibility = View.GONE
        noDietTipsTextView.visibility = View.GONE
    }

    companion object {
        private const val AMOUNT_OF_DIET_TIPS = 4
    }
}