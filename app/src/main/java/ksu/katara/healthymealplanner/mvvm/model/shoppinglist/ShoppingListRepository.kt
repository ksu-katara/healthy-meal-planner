package ksu.katara.healthymealplanner.mvvm.model.shoppinglist

import kotlinx.coroutines.flow.Flow
import ksu.katara.healthymealplanner.foundation.model.Repository
import ksu.katara.healthymealplanner.mvvm.model.recipes.entities.RecipeIngredient
import ksu.katara.healthymealplanner.mvvm.model.shoppinglist.entity.ShoppingListRecipe
import ksu.katara.healthymealplanner.mvvm.model.shoppinglist.entity.ShoppingListRecipeIngredient

typealias ShoppingListListener = (shoppingList: MutableList<ShoppingListRecipe>) -> Unit

interface ShoppingListRepository : Repository {

    /**
     * Load the list of all available shopping list recipes that may be chosen by the user.
     */
    suspend fun load(): MutableList<ShoppingListRecipe>

    /**
     * Listen for for the current shopping list recipes changes.
     * @return [Flow] which emits a new item whenever [deleteIngredient] call
     * changes the current color.
     */
    fun listener(): Flow<MutableList<ShoppingListRecipe>>

    /**
     * Add ingredient to shopping list for recipe with id.
     */
    suspend fun addIngredient(recipeId: Long, ingredient: RecipeIngredient)

    /**
     * Add all ingredients to shopping list for recipe with id.
     */
    suspend fun addAllIngredients(recipeId: Long, isSelected: Boolean)

    /**
     * Set for ingredient property isInShoppingList equals isChecked for recipe in shopping list.
     */
    suspend fun selectIngredient(recipe: ShoppingListRecipe,
        ingredient: ShoppingListRecipeIngredient,
        isChecked: Boolean
    )

    /**
     * Delete ingredient to shopping list for recipe with id.
     */
    fun deleteIngredient(recipeId: Long, ingredient: RecipeIngredient): Flow<Int>

}
