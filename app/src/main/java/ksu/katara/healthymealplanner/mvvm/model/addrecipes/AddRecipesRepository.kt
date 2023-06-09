package ksu.katara.healthymealplanner.mvvm.model.addrecipes

import ksu.katara.healthymealplanner.foundation.model.Repository
import ksu.katara.healthymealplanner.foundation.tasks.Task
import ksu.katara.healthymealplanner.mvvm.model.recipes.entities.Recipe
import ksu.katara.healthymealplanner.mvvm.views.main.tabs.home.MealTypes
import java.util.Date

typealias AddRecipesListener = (addRecipes: List<Recipe>) -> Unit

/**
 * Repository of added recipes interface.
 *
 * Provides access to the available added recipes.
 */
interface AddRecipesRepository : Repository {

    /**
     * Load the list of all added available recipes that may be chosen by the user.
     */
    fun loadAddRecipes(selectedDate: Date, mealTypes: MealTypes): Task<List<Recipe>>

    /**
     * Listen for the current added recipes changes.
     * The listener is triggered immediately with the current value when calling this method.
     */
    fun addAddRecipesListener(listener: AddRecipesListener)

    /**
     * Stop listening for the current added recipe changes.
     */
    fun removeAddRecipesListener(listener: AddRecipesListener)

    /**
     * Delete recipe from  available added recipes.
     */
    fun addRecipesDeleteRecipe(recipe: Recipe): Task<Unit>

}