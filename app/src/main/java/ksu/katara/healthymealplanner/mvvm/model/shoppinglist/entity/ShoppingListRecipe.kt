package ksu.katara.healthymealplanner.mvvm.model.shoppinglist.entity

import ksu.katara.healthymealplanner.mvvm.model.recipes.entities.Recipe
import ksu.katara.healthymealplanner.mvvm.model.recipes.entities.RecipeIngredient

/**
 * Represents shopping list recipe data
 */
data class ShoppingListRecipe(
    val recipe: Recipe,
    var shoppingListIngredients: MutableList<ShoppingListRecipeIngredient>,
)

/**
 * Represents shopping list recipe ingredient data
 */
data class ShoppingListRecipeIngredient(
    val recipeIngredient: RecipeIngredient,
    var isSelectAndCross: Boolean
)