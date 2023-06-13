package ksu.katara.healthymealplanner.mvvm.screens.main.tabs.home.recipedetails.preparationsteps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ksu.katara.healthymealplanner.R
import ksu.katara.healthymealplanner.mvvm.model.recipes.RecipesRepository
import ksu.katara.healthymealplanner.mvvm.model.recipes.entities.RecipePreparationStep
import ksu.katara.healthymealplanner.mvvm.screens.base.BaseViewModel
import ksu.katara.healthymealplanner.mvvm.screens.base.Event
import ksu.katara.healthymealplanner.mvvm.tasks.EmptyResult
import ksu.katara.healthymealplanner.mvvm.tasks.PendingResult
import ksu.katara.healthymealplanner.mvvm.tasks.StatusResult
import ksu.katara.healthymealplanner.mvvm.tasks.SuccessResult

class RecipePreparationStepsListViewModel(
    private val recipeId: Long,
    private val recipesRepository: RecipesRepository,
) : BaseViewModel() {

    private val _preparationSteps = MutableLiveData<StatusResult<List<RecipePreparationStep>>>()
    val preparationSteps: LiveData<StatusResult<List<RecipePreparationStep>>> = _preparationSteps

    private val _actionShowToast = MutableLiveData<Event<Int>>()
    val actionShowToast: LiveData<Event<Int>> = _actionShowToast

    private var preparationStepsResult: StatusResult<List<RecipePreparationStep>> = EmptyResult()
        set(value) {
            field = value
            notifyUpdates()
        }

    init {
        loadPreparationSteps()
    }

    private fun loadPreparationSteps() {
        preparationStepsResult = PendingResult()
        recipesRepository.loadPreparationSteps(recipeId)
            .onSuccess {
                _preparationSteps.value = SuccessResult(it)
            }
            .onError {
                _actionShowToast.value = Event(R.string.cant_load_preparation_steps)
            }
            .autoCancel()
    }

    private fun notifyUpdates() {
        _preparationSteps.postValue(preparationStepsResult)
    }
}