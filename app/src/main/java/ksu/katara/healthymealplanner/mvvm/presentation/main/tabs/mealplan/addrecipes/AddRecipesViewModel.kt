package ksu.katara.healthymealplanner.mvvm.presentation.main.tabs.mealplan.addrecipes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import ksu.katara.healthymealplanner.R
import ksu.katara.healthymealplanner.foundation.model.EmptyResult
import ksu.katara.healthymealplanner.foundation.model.ErrorResult
import ksu.katara.healthymealplanner.foundation.model.PendingResult
import ksu.katara.healthymealplanner.foundation.model.StatusResult
import ksu.katara.healthymealplanner.foundation.model.SuccessResult
import ksu.katara.healthymealplanner.foundation.uiactions.UiActions
import ksu.katara.healthymealplanner.foundation.views.BaseScreen
import ksu.katara.healthymealplanner.foundation.views.BaseViewModel
import ksu.katara.healthymealplanner.foundation.views.LiveResult
import ksu.katara.healthymealplanner.foundation.views.MutableLiveResult
import ksu.katara.healthymealplanner.mvvm.domain.mealplan.AddRecipesListener
import ksu.katara.healthymealplanner.mvvm.domain.mealplan.MealPlanForDateRecipesRepository
import ksu.katara.healthymealplanner.mvvm.domain.recipes.entities.Recipe
import ksu.katara.healthymealplanner.mvvm.presentation.main.tabs.home.MealTypes
import ksu.katara.healthymealplanner.mvvm.presentation.main.tabs.mealplan.addrecipes.AddRecipesFragment.Screen
import java.util.Date

data class AddRecipesItem(
    val recipe: Recipe,
    val isDeleteInProgress: Boolean,
)

class AddRecipesListViewModel @AssistedInject constructor(
    @Assisted screen: BaseScreen,
    private val uiActions: UiActions,
    private val mealPlanForDateRecipesRepository: MealPlanForDateRecipesRepository,
) : BaseViewModel(), AddRecipesActionListener {

    private val _addRecipes = MutableLiveResult<List<AddRecipesItem>>()
    val addRecipes: LiveResult<List<AddRecipesItem>> = _addRecipes

    private val _screenTitle = MutableLiveData<String>()
    val screenTitle: LiveData<String> = _screenTitle

    private val addRecipesDeleteItemIdsInProgress = mutableSetOf<Long>()

    private var addRecipesResult: StatusResult<List<Recipe>> = EmptyResult()
        set(value) {
            field = value
            notifyUpdates()
        }

    private val addRecipesListener: AddRecipesListener = {
        addRecipesResult = if (it.isEmpty()) {
            EmptyResult()
        } else {
            SuccessResult(it)
        }
    }

    private val mealType = (screen as Screen).mealType
    private val selectedDate = (screen as Screen).selectedDate

    init {
        _screenTitle.value = uiActions.getString(R.string.add_recipe_title)
        mealPlanForDateRecipesRepository.addAddRecipesListener(addRecipesListener)
        loadAddRecipes(selectedDate, mealType)
    }

    private fun loadAddRecipes(selectedDate: Date, mealType: MealTypes) {
        addRecipesResult = PendingResult()
        viewModelScope.launch {
            try {
                mealPlanForDateRecipesRepository.loadAddRecipes(selectedDate, mealType)
            } catch (e: Exception) {
                if (e !is CancellationException) addRecipesResult = ErrorResult(e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mealPlanForDateRecipesRepository.removeAddRecipesListener(addRecipesListener)
    }

    override fun onAddRecipePressed(recipe: Recipe) {
        if (isDeleteInProgress(recipe)) return
        addDeleteProgressTo(recipe)
        viewModelScope.launch {
            try {
                mealPlanForDateRecipesRepository.addRecipeToMealPlanForDate(
                    selectedDate,
                    mealType,
                    recipe
                )
                viewModelScope.launch {
                    try {
                        mealPlanForDateRecipesRepository.deleteRecipeFromAddRecipes(recipe)
                        removeDeleteProgressFrom(recipe)
                    } catch (e: Exception) {
                        if (e !is CancellationException) {
                            val message =
                                uiActions.getString(R.string.cant_delete_recipe_from_add_recipes)
                            uiActions.toast(message)
                        }
                    }
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    removeDeleteProgressFrom(recipe)
                    val message = uiActions.getString(R.string.cant_add_recipe_to_meal_plan)
                    uiActions.toast(message)
                }
            }
        }
    }

    private fun addDeleteProgressTo(recipe: Recipe) {
        addRecipesDeleteItemIdsInProgress.add(recipe.id)
        notifyUpdates()
    }

    private fun removeDeleteProgressFrom(recipe: Recipe) {
        addRecipesDeleteItemIdsInProgress.remove(recipe.id)
        notifyUpdates()
    }

    private fun isDeleteInProgress(recipe: Recipe): Boolean {
        return addRecipesDeleteItemIdsInProgress.contains(recipe.id)
    }

    private fun notifyUpdates() {
        _addRecipes.postValue(addRecipesResult.resultMap { addRecipes ->
            addRecipes.map { recipe -> AddRecipesItem(recipe, isDeleteInProgress(recipe)) }
                .toMutableList()
        })
    }

    fun tryAgain() {
        loadAddRecipes(selectedDate, mealType)
    }

    @AssistedFactory
    interface Factory {
        fun create(screen: BaseScreen): AddRecipesListViewModel
    }
}