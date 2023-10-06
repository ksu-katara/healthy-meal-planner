package ksu.katara.healthymealplanner.mvvm.model.recipecategories

import ksu.katara.healthymealplanner.foundation.model.Repository
import ksu.katara.healthymealplanner.mvvm.model.recipecategories.entities.Category

typealias RecipeCategoriesListener = (recipeCategories: List<Category>) -> Unit

/**
 * Repository of recipe categories interface.
 *
 * Provides access to the available recipe categories.
 */
interface CategoriesRepository : Repository {

    /**
     * Load the list of all available recipe categories that may be chosen by the user.
     */
    suspend fun load(): List<Category>

    /**
     * Get available recipe categories by id.
     */
    suspend fun getById(id: Long): Category

    /**
     * Listen for the current recipe categories changes.
     * The listener is triggered immediately with the current value when calling this method.
     */
    fun addListener(listener: RecipeCategoriesListener)

    /**
     * Stop listening for the current recipe categories.
     */
    fun removeListener(listener: RecipeCategoriesListener)

}