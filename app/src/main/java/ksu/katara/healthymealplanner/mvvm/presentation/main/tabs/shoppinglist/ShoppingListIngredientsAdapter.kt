package ksu.katara.healthymealplanner.mvvm.presentation.main.tabs.shoppinglist

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEach
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ksu.katara.healthymealplanner.R
import ksu.katara.healthymealplanner.databinding.ItemShoppingListIngredientsBinding
import ksu.katara.healthymealplanner.foundation.model.getPercentage
import ksu.katara.healthymealplanner.foundation.model.isInProgress
import ksu.katara.healthymealplanner.mvvm.domain.shoppinglist.entity.ShoppingListRecipe
import ksu.katara.healthymealplanner.mvvm.domain.shoppinglist.entity.ShoppingListRecipeIngredient

class ShoppingListIngredientsDiffCallback(
    private val oldList: List<ShoppingListIngredientsItem>,
    private val newList: List<ShoppingListIngredientsItem>,
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldShoppingListIngredientsItem = oldList[oldItemPosition]
        val newShoppingListIngredientsItem = newList[newItemPosition]
        return oldShoppingListIngredientsItem.shoppingListRecipeIngredient.ingredient.id == newShoppingListIngredientsItem.shoppingListRecipeIngredient.ingredient.id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldShoppingListIngredientsItem = oldList[oldItemPosition]
        val newShoppingListIngredientsItem = newList[newItemPosition]
        return oldShoppingListIngredientsItem == newShoppingListIngredientsItem
    }
}

class ShoppingListIngredientsAdapter(
    private val shoppingListRecipe: ShoppingListRecipe,
    private val shoppingListViewModel: ShoppingListViewModel,
) : RecyclerView.Adapter<ShoppingListIngredientsAdapter.ShoppingListIngredientsViewHolder>(),
    View.OnClickListener {
    var shoppingListIngredients: MutableList<ShoppingListIngredientsItem> = mutableListOf()
        set(newValue) {
            val diffCallback = ShoppingListIngredientsDiffCallback(field, newValue)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            field = newValue
            diffResult.dispatchUpdatesTo(this)
        }

    override fun onClick(v: View) {
        val shoppingListRecipeIngredient = v.tag as ShoppingListRecipeIngredient

        when (v.id) {
            R.id.shoppingListIngredientsItemDeleteViewButton -> {
                shoppingListViewModel.onShoppingListIngredientsRecipeDelete(
                    shoppingListRecipe,
                    shoppingListRecipeIngredient
                )
            }

            R.id.isShoppingListIngredientsItemSelectedCheckBox -> {
                val checkBox = v as CheckBox
                shoppingListViewModel.onShoppingListIngredientsRecipeSelect(
                    shoppingListRecipe,
                    shoppingListRecipeIngredient,
                    checkBox.isChecked
                )
            }

            else -> {
                val container = v as ConstraintLayout
                var checkBox: CheckBox? = null
                container.forEach { if (it is CheckBox) checkBox = it }
                shoppingListViewModel.onShoppingListIngredientsRecipeSelect(
                    shoppingListRecipe,
                    shoppingListRecipeIngredient,
                    !checkBox!!.isChecked
                )
            }
        }
    }

    override fun getItemCount(): Int = shoppingListIngredients.size

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ShoppingListIngredientsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemShoppingListIngredientsBinding.inflate(inflater, parent, false)
        binding.root.setOnClickListener(this)
        binding.shoppingListIngredientsItemDeleteProgressBar.progressDrawable.setColorFilter(
            Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN
        )
        binding.isShoppingListIngredientsItemSelectedCheckBox.setOnClickListener(this)
        binding.shoppingListIngredientsItemDeleteViewButton.setOnClickListener(this)
        return ShoppingListIngredientsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShoppingListIngredientsViewHolder, position: Int) {
        val shoppingListIngredientsItem = shoppingListIngredients[position]
        val shoppingListRecipeIngredient = shoppingListIngredientsItem.shoppingListRecipeIngredient
        with(holder.binding) {
            holder.itemView.tag = shoppingListRecipeIngredient
            isShoppingListIngredientsItemSelectedCheckBox.tag = shoppingListRecipeIngredient
            shoppingListIngredientsItemDeleteViewButton.tag = shoppingListRecipeIngredient
            if (shoppingListIngredientsItem.isSelectInProgress) {
                isShoppingListIngredientsItemSelectedCheckBox.visibility = View.INVISIBLE
                isShoppingListIngredientsItemSelectedProgressBar.visibility = View.VISIBLE
                holder.binding.root.setOnClickListener(null)
            } else {
                isShoppingListIngredientsItemSelectedCheckBox.visibility = View.VISIBLE
                isShoppingListIngredientsItemSelectedProgressBar.visibility = View.GONE
                holder.binding.root.setOnClickListener(this@ShoppingListIngredientsAdapter)
                isShoppingListIngredientsItemSelectedCheckBox.isChecked =
                    shoppingListRecipeIngredient.isSelectAndCross
                if (shoppingListRecipeIngredient.isSelectAndCross) {
                    shoppingListIngredientsItemCrossView.visibility = View.VISIBLE
                } else {
                    shoppingListIngredientsItemCrossView.visibility = View.INVISIBLE
                }
            }
            if (shoppingListIngredientsItem.isDeleteInProgress.isInProgress()) {
                shoppingListIngredientsItemCrossView.visibility = View.INVISIBLE
                componentWithoutDeleteGroup.visibility = View.INVISIBLE
                deleteProgressGroup.visibility = View.VISIBLE
                shoppingListIngredientsItemDeleteProgressBar.progress =
                    shoppingListIngredientsItem.isDeleteInProgress.getPercentage()
                deletingPercentageTextView.text =
                    shoppingListIngredientsItem.isDeleteInProgress.getPercentage().toString()
                holder.binding.root.setOnClickListener(null)
            } else {
                componentWithoutDeleteGroup.visibility = View.VISIBLE
                deleteProgressGroup.visibility = View.GONE
                holder.binding.root.setOnClickListener(this@ShoppingListIngredientsAdapter)
            }
            shoppingListIngredientsItemNameTextView.text =
                shoppingListRecipeIngredient.ingredient.product
            if (shoppingListRecipeIngredient.ingredient.amount == 0.0) {
                shoppingListIngredientsItemAmountTextView.text = ""
            } else {
                shoppingListIngredientsItemAmountTextView.text =
                    shoppingListRecipeIngredient.ingredient.amount.toString()
            }
            shoppingListIngredientsItemMeasureTextView.text =
                shoppingListRecipeIngredient.ingredient.measure
        }
    }

    class ShoppingListIngredientsViewHolder(
        val binding: ItemShoppingListIngredientsBinding
    ) : RecyclerView.ViewHolder(binding.root)
}