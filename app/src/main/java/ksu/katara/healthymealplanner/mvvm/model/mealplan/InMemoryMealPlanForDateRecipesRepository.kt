package ksu.katara.healthymealplanner.mvvm.model.mealplan

import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import ksu.katara.healthymealplanner.foundation.model.coroutines.IoDispatcher
import ksu.katara.healthymealplanner.mvvm.model.mealplan.entities.MealPlanRecipes
import ksu.katara.healthymealplanner.mvvm.model.recipes.entities.Recipe
import ksu.katara.healthymealplanner.mvvm.views.main.tabs.home.MealTypes
import ksu.katara.healthymealplanner.mvvm.views.main.tabs.home.sdf
import java.util.Date

/**
 * Simple in-memory implementation of [MealPlanForDateRecipesRepository]
 */
class InMemoryMealPlanForDateRecipesRepository(
    private val ioDispatcher: IoDispatcher
) : MealPlanForDateRecipesRepository {

    private var mealPlanForDate: MutableMap<String, MutableList<MealPlanRecipes?>> = mutableMapOf()

    private var mealPlanForDateRecipes: MealPlanRecipes? = null
    private var mealPlanForDateRecipesLoaded = false
    private val mealPlanForDateRecipesListeners = mutableSetOf<MealPlanForDateRecipesListener>()

    override fun getMealPlan() = mealPlanForDate

    override suspend fun loadMealPlanForDateRecipes(selectedDate: Date, mealType: MealTypes): MealPlanRecipes? = withContext(ioDispatcher.value) {
        delay(1000L)
        mealPlanForDateRecipes = getMealPlanForDateRecipes(selectedDate, mealType)
        mealPlanForDateRecipesLoaded = true
        notifyMealPlanForDateChanges()
        return@withContext mealPlanForDateRecipes
    }

    private fun getMealPlanForDateRecipes(selectedDate: Date, mealType: MealTypes): MealPlanRecipes? {
        val date = sdf.format(selectedDate)
        val mealPlanForDateRecipes: MealPlanRecipes? = if (mealPlanForDate.containsKey(date)) {
            mealPlanForDate
                .getValue(date)
                .firstOrNull { it?.mealType == mealType }
        } else {
            null
        }
        return mealPlanForDateRecipes
    }

    override fun addMealPlanForDateRecipesItemListener(listener: MealPlanForDateRecipesListener) {
        mealPlanForDateRecipesListeners.add(listener)
        if (mealPlanForDateRecipesLoaded) {
            listener.invoke(mealPlanForDateRecipes)
        }
    }

    override fun removeMealPlanForDateRecipesItemListener(listener: MealPlanForDateRecipesListener) {
        mealPlanForDateRecipesListeners.remove(listener)
    }

    override suspend fun mealPlanForDateRecipesAddRecipe(selectedDate: Date, mealType: MealTypes, recipe: Recipe) = withContext(ioDispatcher.value) {
        delay(1000L)
        addRecipeToMealPlanForDate(selectedDate, mealType, recipe)
        notifyMealPlanForDateChanges()
    }

    private fun addRecipeToMealPlanForDate(selectedDate: Date, mealType: MealTypes, recipe: Recipe) {
        val newMealPlanForTodayRecipesItem = MealPlanRecipes(mealType, mutableListOf(recipe))
        val mealPlanForDateRecipesListItem: MealPlanRecipes?
        val date = sdf.format(selectedDate)
        if (mealPlanForDate.containsKey(date)) {
            mealPlanForDateRecipesListItem = mealPlanForDate
                .getValue(date)
                .firstOrNull { it?.mealType == mealType }
            if (mealPlanForDateRecipesListItem == null) {
                mealPlanForDateRecipes = newMealPlanForTodayRecipesItem
                mealPlanForDate[date]?.add(newMealPlanForTodayRecipesItem)
            } else {
                val index = mealPlanForDate[date]?.indexOfFirst { it == mealPlanForDateRecipesListItem }
                mealPlanForDateRecipesListItem.recipesList.add(recipe)
                mealPlanForDateRecipes = mealPlanForDateRecipesListItem
                mealPlanForDate[date]?.set(index!!, mealPlanForDateRecipesListItem)
            }
        } else {
            mealPlanForDateRecipes = newMealPlanForTodayRecipesItem
            mealPlanForDate[date] = mutableListOf(newMealPlanForTodayRecipesItem)
        }
    }

    override suspend fun mealPlanForDateRecipesDeleteRecipe(selectedDate: Date, mealType: MealTypes, recipe: Recipe): MealPlanRecipes? = withContext(ioDispatcher.value) {
        delay(1000L)
        deleteRecipeFromMealPlanForDate(selectedDate, mealType, recipe)
        notifyMealPlanForDateChanges()
        return@withContext mealPlanForDateRecipes
    }

    private fun deleteRecipeFromMealPlanForDate(selectedDate: Date, mealType: MealTypes, recipe: Recipe) {
        var recipeList: MutableList<Recipe>
        val date = sdf.format(selectedDate)
        if (mealPlanForDate.containsKey(date)) {
            val mealPlanForDateRecipesList = mealPlanForDate.getValue(date)
            mealPlanForDateRecipesList.forEach { mealPlanForDateRecipesListItem ->
                if (mealPlanForDateRecipesListItem?.mealType == mealType) {
                    recipeList = mealPlanForDateRecipesListItem.recipesList
                    val indexToDelete = recipeList.indexOfFirst { it == recipe }
                    if (indexToDelete != -1) {
                        recipeList.removeAt(indexToDelete)
                    }
                    mealPlanForDateRecipes = if (recipeList.isNotEmpty()) {
                        mealPlanForDateRecipesListItem
                    } else {
                        mealPlanForDate.remove(date)
                        null
                    }
                }
            }
        }
    }

    private fun notifyMealPlanForDateChanges() {
        if (!mealPlanForDateRecipesLoaded) return
        mealPlanForDateRecipesListeners.forEach { it.invoke(mealPlanForDateRecipes) }
    }
}