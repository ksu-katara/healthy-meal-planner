package ksu.katara.healthymealplanner.mvvm.model.shoppinglist

import ksu.katara.healthymealplanner.foundation.model.Repository
import ksu.katara.healthymealplanner.foundation.tasks.Task
import ksu.katara.healthymealplanner.mvvm.model.recipes.entities.RecipeIngredient
import ksu.katara.healthymealplanner.mvvm.model.shoppinglist.entity.ShoppingListRecipe
import ksu.katara.healthymealplanner.mvvm.model.shoppinglist.entity.ShoppingListRecipeIngredient

typealias ShoppingListListener = (shoppingListItemList: MutableList<ShoppingListRecipe>) -> Unit

interface ShoppingListRepository : Repository {

    /**
     * Load the list of all available shopping list recipes that may be chosen by the user.
     */
    fun loadShoppingList(): Task<MutableList<ShoppingListRecipe>>

    /**
     * Listen for the current shopping list recipes changes.
     * The listener is triggered immediately with the current value when calling this method.
     */
    fun addShoppingListListener(listener: ShoppingListListener)

    /**
     * Stop listening for the current shopping list recipes.
     */
    fun removeShoppingListListener(listener: ShoppingListListener)

    /**
     * Add ingredient to shopping list for recipe with id.
     */
    fun shoppingListIngredientsAddIngredient(recipeId: Long, ingredient: RecipeIngredient): Task<Unit>

    /**
     * Add all ingredients to shopping list for recipe with id.
     */
    fun shoppingListIngredientsAddAllIngredients(recipeId: Long, isSelected: Boolean): Task<Unit>

    /**
     * Set for ingredient property isInShoppingList equals isChecked for recipe in shopping list.
     */
    fun shoppingListIngredientsSelectIngredient(
        shoppingListRecipe: ShoppingListRecipe,
        shoppingListRecipeIngredient: ShoppingListRecipeIngredient,
        isChecked: Boolean
    ): Task<Unit>

    /**
     * Delete ingredient to shopping list for recipe with id.
     */
    fun shoppingListIngredientsDeleteIngredient(recipeId: Long, ingredient: RecipeIngredient): Task<Unit>

}
