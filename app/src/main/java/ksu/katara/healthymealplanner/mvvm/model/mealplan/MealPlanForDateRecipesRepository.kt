package ksu.katara.healthymealplanner.mvvm.model.mealplan

import ksu.katara.healthymealplanner.foundation.model.Repository
import ksu.katara.healthymealplanner.foundation.tasks.Task
import ksu.katara.healthymealplanner.mvvm.model.mealplan.entities.MealPlanRecipes
import ksu.katara.healthymealplanner.mvvm.model.recipes.entities.Recipe
import ksu.katara.healthymealplanner.mvvm.views.main.tabs.home.MealTypes
import java.util.Date

typealias MealPlanForDateRecipesListener = (mealPlanRecipes: MealPlanRecipes?) -> Unit

/**
 * Repository of meal plan recipes interface.
 *
 * Provides access to the available meal plan recipes.
 */
interface MealPlanForDateRecipesRepository : Repository {

    /**
     * Load available meal plan.
     */
    fun loadMealPlan(): Task<Unit>

    /**
     * Get available meal plan.
     */
    fun getMealPlan(): MutableMap<String, MutableList<MealPlanRecipes?>>

    /**
     * Load the list of all available meal plan recipes that may be chosen by the user.
     */
    fun loadMealPlanForDateRecipes(selectedDate: Date, mealType: MealTypes): Task<Unit>

    /**
     * Listen for the current meal plan recipes changes.
     * The listener is triggered immediately with the current value when calling this method.
     */
    fun addMealPlanForDateRecipesItemListener(listener: MealPlanForDateRecipesListener)

    /**
     * Stop listening for the current meal plan recipes.
     */
    fun removeMealPlanForDateRecipesItemListener(listener: MealPlanForDateRecipesListener)

    /**
     * Add recipe to meal plan for selected date and meal type.
     */
    fun mealPlanForDateRecipesAddRecipe(selectedDate: Date, mealType: MealTypes, recipe: Recipe): Task<Unit>

    /**
     * Delete recipe from meal plan for selected date and meal type.
     */
    fun mealPlanForDateRecipesDeleteRecipe(selectedDate: Date, mealType: MealTypes, recipe: Recipe): Task<MealPlanRecipes?>

}
