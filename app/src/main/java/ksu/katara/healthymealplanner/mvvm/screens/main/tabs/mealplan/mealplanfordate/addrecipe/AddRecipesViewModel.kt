package ksu.katara.healthymealplanner.mvvm.screens.main.tabs.mealplan.mealplanfordate.addrecipe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ksu.katara.healthymealplanner.R
import ksu.katara.healthymealplanner.mvvm.model.addrecipes.AddRecipesListener
import ksu.katara.healthymealplanner.mvvm.model.addrecipes.AddRecipesRepository
import ksu.katara.healthymealplanner.mvvm.model.meal.MealTypes
import ksu.katara.healthymealplanner.mvvm.model.mealplan.MealPlanForDateRecipesRepository
import ksu.katara.healthymealplanner.mvvm.model.recipes.entities.Recipe
import ksu.katara.healthymealplanner.mvvm.screens.base.BaseViewModel
import ksu.katara.healthymealplanner.mvvm.screens.base.Event
import ksu.katara.healthymealplanner.mvvm.tasks.EmptyResult
import ksu.katara.healthymealplanner.mvvm.tasks.PendingResult
import ksu.katara.healthymealplanner.mvvm.tasks.StatusResult
import ksu.katara.healthymealplanner.mvvm.tasks.SuccessResult

data class AddRecipesItem(
    val recipe: Recipe,
    val isDeleteInProgress: Boolean,
)

class AddRecipesListViewModel(
    private val selectedDate: String,
    private val mealType: MealTypes,
    private val addRecipesRepository: AddRecipesRepository,
    private val mealPlanForDateRecipesRepository: MealPlanForDateRecipesRepository,
) : BaseViewModel(), AddRecipesActionListener {

    private val _addRecipes = MutableLiveData<StatusResult<MutableList<AddRecipesItem>>>()
    val addRecipes: LiveData<StatusResult<MutableList<AddRecipesItem>>> = _addRecipes

    private val _actionShowToast = MutableLiveData<Event<Int>>()
    val actionShowToast: LiveData<Event<Int>> = _actionShowToast

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

    init {
        addRecipesRepository.addAddRecipesListener(addRecipesListener)
        loadAddRecipes(selectedDate, mealType)
    }

    private fun loadAddRecipes(selectedDate: String, mealType: MealTypes) {
        addRecipesResult = PendingResult()
        addRecipesRepository.loadAddRecipes(selectedDate, mealType)
            .onError {
                _actionShowToast.value = Event(R.string.cant_load_recipes)
            }
            .autoCancel()
    }

    override fun onCleared() {
        super.onCleared()
        addRecipesRepository.removeAddRecipesListener(addRecipesListener)
    }

    override fun invoke(recipe: Recipe) {
        if (isDeleteInProgress(recipe)) return
        addDeleteProgressTo(recipe)
        mealPlanForDateRecipesRepository.mealPlanForDateRecipesAddRecipe(selectedDate, mealType, recipe)
            .onError {
                _actionShowToast.value = Event(R.string.cant_add_recipe_to_meal_plan)
            }
            .autoCancel()

        addRecipesRepository.addRecipesDeleteRecipe(recipe)
            .onSuccess {
                removeDeleteProgressFrom(recipe)
            }
            .onError {
                removeDeleteProgressFrom(recipe)
                _actionShowToast.value = Event(R.string.cant_delete_recipe_from_meal_plan)
            }
            .autoCancel()
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
            addRecipes.map { recipe -> AddRecipesItem(recipe, isDeleteInProgress(recipe)) }.toMutableList()
        })
    }
}