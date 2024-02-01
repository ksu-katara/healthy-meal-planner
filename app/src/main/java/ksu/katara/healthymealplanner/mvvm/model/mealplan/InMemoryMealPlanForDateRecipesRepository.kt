package ksu.katara.healthymealplanner.mvvm.model.mealplan

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import ksu.katara.healthymealplanner.foundation.model.coroutines.IoDispatcher
import ksu.katara.healthymealplanner.mvvm.model.mealplan.entities.MealPlanRecipes
import ksu.katara.healthymealplanner.mvvm.model.recipes.entities.Recipe
import ksu.katara.healthymealplanner.mvvm.model.sqlite.AppSQLiteContract.MealPlanMealTypesJoinTable
import ksu.katara.healthymealplanner.mvvm.model.sqlite.AppSQLiteContract.MealPlanRecipesJoinTable
import ksu.katara.healthymealplanner.mvvm.model.sqlite.AppSQLiteContract.MealPlanTable
import ksu.katara.healthymealplanner.mvvm.model.sqlite.AppSQLiteContract.MealTypesTable
import ksu.katara.healthymealplanner.mvvm.model.sqlite.AppSQLiteContract.RecipesTable
import ksu.katara.healthymealplanner.mvvm.views.main.tabs.home.MealTypes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Simple in-memory implementation of [MealPlanForDateRecipesRepository]
 */
class InMemoryMealPlanForDateRecipesRepository(
    private val db: SQLiteDatabase,
    private val ioDispatcher: IoDispatcher
) : MealPlanForDateRecipesRepository {

    private var mealPlanForDateRecipes: MealPlanRecipes? = null
    private var mealPlanForDateRecipesLoaded = false
    private val mealPlanForDateRecipesListeners = mutableSetOf<MealPlanForDateRecipesListener>()

    private lateinit var addRecipes: MutableList<Recipe>
    private var loadedAddRecipes = false
    private val addRecipesListeners = mutableListOf<AddRecipesListener>()

    private val dataSDF = SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH)

    override suspend fun loadMealPlanForDate(
        selectedDate: Date,
        mealType: MealTypes
    ): MealPlanRecipes? = withContext(ioDispatcher.value) {
        delay(1000L)
        mealPlanForDateRecipes = findMealPlanRecipes(selectedDate, mealType)
        mealPlanForDateRecipesLoaded = true
        mealPlanForDateNotifyChanges()
        return@withContext mealPlanForDateRecipes
    }

    private fun findMealPlanRecipes(selectedDate: Date, mealType: MealTypes): MealPlanRecipes? {
        val cursor = queryMealPlanRecipes(selectedDate, mealType)
        cursor.use {
            val recipes = mutableListOf<Recipe>()
            while (cursor.moveToNext()) {
                recipes.add(parseRecipe(cursor))
            }
            return if (recipes.isEmpty()) null else MealPlanRecipes(
                mealType = mealType,
                recipes = recipes
            )
        }
    }

    private fun queryMealPlanRecipes(selectedDate: Date, mealType: MealTypes): Cursor {
        val sql = buildString {
            append("SELECT ${RecipesTable.TABLE_NAME}.* ")
            append("FROM ${MealPlanTable.TABLE_NAME} ")
            append("INNER JOIN ${MealPlanRecipesJoinTable.TABLE_NAME} ON ${MealPlanRecipesJoinTable.TABLE_NAME}.${MealPlanRecipesJoinTable.COLUMN_MEAL_PLAN_ID} = ${MealPlanTable.TABLE_NAME}.${MealPlanTable.COLUMN_ID} ")
            append("INNER JOIN ${RecipesTable.TABLE_NAME} ON ${RecipesTable.TABLE_NAME}.${RecipesTable.COLUMN_ID} = ${MealPlanRecipesJoinTable.TABLE_NAME}.${MealPlanRecipesJoinTable.COLUMN_RECIPE_ID} ")
            append("INNER JOIN ${MealPlanMealTypesJoinTable.TABLE_NAME} ON ${MealPlanMealTypesJoinTable.TABLE_NAME}.${MealPlanMealTypesJoinTable.COLUMN_MEAL_PLAN_ID} = ${MealPlanTable.TABLE_NAME}.${MealPlanTable.COLUMN_ID} ")
            append("INNER JOIN ${MealTypesTable.TABLE_NAME} ON ${MealTypesTable.TABLE_NAME}.${MealTypesTable.COLUMN_ID} = ${MealPlanMealTypesJoinTable.TABLE_NAME}.${MealPlanMealTypesJoinTable.COLUMN_MEAL_TYPE_ID} ")
            append("WHERE ${MealTypesTable.TABLE_NAME}.${MealTypesTable.COLUMN_NAME} = \"${mealType.mealName}\" ")
            append("AND ")
            append(
                "${MealPlanTable.TABLE_NAME}.${MealPlanTable.COLUMN_DATE} = \"${
                    dataSDF.format(
                        selectedDate
                    )
                }\""
            )
        }
        return db.rawQuery(sql, null)
    }

    private fun parseRecipe(cursor: Cursor): Recipe {
        return Recipe(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(RecipesTable.COLUMN_ID)),
            photo = cursor.getString(cursor.getColumnIndexOrThrow(RecipesTable.COLUMN_PHOTO)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(RecipesTable.COLUMN_NAME)),
            categoryId = cursor.getLong(cursor.getColumnIndexOrThrow(RecipesTable.COLUMN_CATEGORY_ID))
        )
    }

    override fun addMealPlanForDateListener(listener: MealPlanForDateRecipesListener) {
        mealPlanForDateRecipesListeners.add(listener)
        if (mealPlanForDateRecipesLoaded) {
            listener.invoke(mealPlanForDateRecipes)
        }
    }

    override fun removeMealPlanForDateListener(listener: MealPlanForDateRecipesListener) {
        mealPlanForDateRecipesListeners.remove(listener)
    }

    override suspend fun addRecipeToMealPlanForDate(
        selectedDate: Date,
        mealType: MealTypes,
        recipe: Recipe
    ) = withContext(ioDispatcher.value) {
        delay(1000L)
        addRecipeToMealPlanForDateRecipes(selectedDate, mealType, recipe)
        mealPlanForDateNotifyChanges()
    }

    private fun addRecipeToMealPlanForDateRecipes(selectedDate: Date, mealType: MealTypes, recipe: Recipe) {
        insertRecipeToMealPlanForDateRecipes(selectedDate, mealType, recipe)
        mealPlanForDateRecipes = findMealPlanRecipes(selectedDate, mealType)
        mealPlanForDateNotifyChanges()
    }

    private fun insertRecipeToMealPlanForDateRecipes(
        selectedDate: Date,
        mealType: MealTypes,
        recipe: Recipe
    ) {
        var mealPlanId = findMealPlanId(selectedDate)
        if (mealPlanId != null) {
            insertDataIntoMealPlanRecipesJoinTable(mealPlanId, recipe.id)
            val mealTypeId = findMealTypeId(mealType.mealName)
            insertDataIntoMealPlanMealTypesJoinTable(mealPlanId, mealTypeId)
        } else {
            insertDataIntoMealPlanTable(selectedDate)
            mealPlanId = findMealPlanId(selectedDate)
            if (mealPlanId != null ) {
                insertDataIntoMealPlanRecipesJoinTable(mealPlanId, recipe.id)
                val mealTypeId = findMealTypeId(mealType.mealName)
                insertDataIntoMealPlanMealTypesJoinTable(mealPlanId, mealTypeId)
            } else {
                throw Exception("Can't insert data into MealPlanTable")
            }
        }
    }

    private fun insertDataIntoMealPlanTable(selectedDate: Date) {
        val sql = buildString {
            append("INSERT INTO ${MealPlanTable.TABLE_NAME} (")
            append("${MealPlanTable.COLUMN_DATE}) ")
            append("VALUES")
            append("(\"${dataSDF.format(selectedDate)}\")")
        }
        db.execSQL(sql)
    }

    private fun insertDataIntoMealPlanMealTypesJoinTable(mealPlanId: Long, mealTypeId: Long) {
        val sql = buildString {
            append("INSERT INTO ${MealPlanMealTypesJoinTable.TABLE_NAME} (")
            append("${MealPlanMealTypesJoinTable.COLUMN_MEAL_PLAN_ID}, ")
            append("${MealPlanMealTypesJoinTable.COLUMN_MEAL_TYPE_ID}) ")
            append("VALUES ")
            append("($mealPlanId, $mealTypeId)")
        }
        db.execSQL(sql)
    }

    private fun insertDataIntoMealPlanRecipesJoinTable(mealPlanId: Long, recipeId: Long) {
        val sql = buildString {
            append("INSERT INTO ${MealPlanRecipesJoinTable.TABLE_NAME} (")
            append("${MealPlanRecipesJoinTable.COLUMN_MEAL_PLAN_ID}, ")
            append("${MealPlanRecipesJoinTable.COLUMN_RECIPE_ID}) ")
            append("VALUES")
            append("($mealPlanId, ${recipeId})")
        }
        db.execSQL(sql)
    }

    private fun findMealTypeId(mealName: String): Long {
        val cursor = queryMealTypeId(mealName)
        cursor.use {
            cursor.moveToFirst()
            return parseMealTypeId(cursor)
        }
    }

    private fun parseMealTypeId(cursor: Cursor): Long {
        return cursor.getLong(cursor.getColumnIndexOrThrow(MealTypesTable.COLUMN_ID))
    }

    private fun queryMealTypeId(mealName: String): Cursor {
        val sql = buildString {
            append("SELECT ${MealTypesTable.COLUMN_ID} FROM ${MealTypesTable.TABLE_NAME} ")
            append("WHERE ${MealTypesTable.COLUMN_NAME} = \"$mealName\"")
        }
        return db.rawQuery(sql, null)
    }

    private fun findMealPlanId(selectedDate: Date): Long? {
        val cursor = queryMealPlanId(selectedDate)
        cursor.use {
            cursor.moveToFirst()
            if (cursor.count == 0) return null
            return parseMealPlanId(cursor)
        }
    }

    private fun parseMealPlanId(cursor: Cursor): Long {
        return cursor.getLong(cursor.getColumnIndexOrThrow(MealPlanTable.COLUMN_ID))
    }

    private fun queryMealPlanId(selectedDate: Date): Cursor {
        val sql = buildString {
            append("SELECT * FROM ${MealPlanTable.TABLE_NAME} ")
            append("WHERE ${MealPlanTable.COLUMN_DATE} = \"${dataSDF.format(selectedDate)}\"")
        }
        return db.rawQuery(sql, null)
    }

    override suspend fun deleteRecipeFromMealPlanForDate(
        selectedDate: Date,
        mealType: MealTypes,
        recipe: Recipe
    ): MealPlanRecipes? = withContext(ioDispatcher.value) {
        delay(1000L)
        deleteRecipeFromMealPlanForDateRecipes(selectedDate, mealType, recipe)
        mealPlanForDateRecipes = findMealPlanRecipes(selectedDate, mealType)
        mealPlanForDateNotifyChanges()
        return@withContext mealPlanForDateRecipes
    }

    private fun deleteRecipeFromMealPlanForDateRecipes(selectedDate: Date, mealType: MealTypes, recipe: Recipe) {
        val sql = buildString {
            append("DELETE FROM ${MealPlanRecipesJoinTable.TABLE_NAME} ")
            append("WHERE ${MealPlanRecipesJoinTable.TABLE_NAME}.${MealPlanRecipesJoinTable.COLUMN_RECIPE_ID} IN (")
            append("SELECT ${MealPlanRecipesJoinTable.TABLE_NAME}.${MealPlanRecipesJoinTable.COLUMN_RECIPE_ID} ")
            append("FROM ${MealPlanRecipesJoinTable.TABLE_NAME} ")
            append("INNER JOIN ${MealPlanTable.TABLE_NAME} ON ${MealPlanTable.TABLE_NAME}.${MealPlanTable.COLUMN_ID} = ${MealPlanRecipesJoinTable.TABLE_NAME}.${MealPlanRecipesJoinTable.COLUMN_MEAL_PLAN_ID} ")
            append("INNER JOIN ${RecipesTable.TABLE_NAME} ON ${RecipesTable.TABLE_NAME}.${RecipesTable.COLUMN_ID} = ${MealPlanRecipesJoinTable.TABLE_NAME}.${MealPlanRecipesJoinTable.COLUMN_RECIPE_ID} ")
            append("INNER JOIN ${MealPlanMealTypesJoinTable.TABLE_NAME} ON ${MealPlanMealTypesJoinTable.TABLE_NAME}.${MealPlanMealTypesJoinTable.COLUMN_MEAL_PLAN_ID} = ${MealPlanTable.TABLE_NAME}.${MealPlanTable.COLUMN_ID} ")
            append("INNER JOIN ${MealTypesTable.TABLE_NAME} ON ${MealTypesTable.TABLE_NAME}.${MealTypesTable.COLUMN_ID} = ${MealPlanMealTypesJoinTable.TABLE_NAME}.${MealPlanMealTypesJoinTable.COLUMN_MEAL_TYPE_ID} ")
            append("WHERE ${MealTypesTable.TABLE_NAME}.${MealTypesTable.COLUMN_NAME} = \"${mealType.mealName}\" ")
            append("AND ")
            append(
                "${MealPlanTable.TABLE_NAME}.${MealPlanTable.COLUMN_DATE} = \"${
                    dataSDF.format(
                        selectedDate
                    )
                }\" "
            )
            append("AND ${RecipesTable.TABLE_NAME}.${RecipesTable.COLUMN_ID} = ${recipe.id})")
        }
        db.execSQL(sql)
    }

    private fun mealPlanForDateNotifyChanges() {
        if (!mealPlanForDateRecipesLoaded) return
        mealPlanForDateRecipesListeners.forEach { it.invoke(mealPlanForDateRecipes) }
    }

    override suspend fun loadAddRecipes(selectedDate: Date, mealType: MealTypes): List<Recipe> =
        withContext(ioDispatcher.value) {
            delay(1000L)
            addRecipes = findAddRecipes()
            loadedAddRecipes = true
            notifyChanges()
            return@withContext addRecipes
        }

    private fun findAddRecipes(): MutableList<Recipe> {
        val mealPlanForDateRecipes = mealPlanForDateRecipes?.recipes ?: mutableListOf<Recipe>()
        val allRecipes = findAllRecipes()
        allRecipes.removeAll(mealPlanForDateRecipes)
        return if (mealPlanForDateRecipes.isEmpty()) allRecipes else allRecipes
    }

    private fun findAllRecipes(): MutableList<Recipe> {
        val cursor = queryRecipes()
        return cursor.use {
            val list = mutableListOf<Recipe>()
            while (cursor.moveToNext()) {
                list.add(parseRecipe(cursor))
            }
            return@use list
        }
    }

    private fun queryRecipes(): Cursor {
        val sql = buildString {
            append("SELECT * FROM ")
            append("${RecipesTable.TABLE_NAME} ")
        }
        return db.rawQuery(sql, null)
    }

    override fun addAddRecipesListener(listener: AddRecipesListener) {
        addRecipesListeners.add(listener)
        if (loadedAddRecipes) {
            listener.invoke(addRecipes)
        }
    }

    override fun removeAddRecipesListener(listener: AddRecipesListener) {
        addRecipesListeners.remove(listener)
    }

    override suspend fun deleteRecipeFromAddRecipes(recipe: Recipe) =
        withContext(ioDispatcher.value) {
            delay(1000L)
            val indexToDelete = addRecipes.indexOfFirst { it == recipe }
            if (indexToDelete != -1) {
                addRecipes.removeAt(indexToDelete)
            }
            notifyChanges()
        }

    private fun notifyChanges() {
        if (!loadedAddRecipes) return
        addRecipesListeners.forEach { it.invoke(addRecipes) }
    }
}