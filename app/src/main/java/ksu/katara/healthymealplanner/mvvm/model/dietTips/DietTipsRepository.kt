package ksu.katara.healthymealplanner.mvvm.model.dietTips

import ksu.katara.healthymealplanner.foundation.model.Repository
import ksu.katara.healthymealplanner.foundation.tasks.Task
import ksu.katara.healthymealplanner.mvvm.model.dietTips.entities.DietTip
import ksu.katara.healthymealplanner.mvvm.model.dietTips.entities.DietTipDetails
import ksu.katara.healthymealplanner.mvvm.model.dietTips.entities.DietTipsChapter

typealias DietTipsChaptersListener = (dietTipsChapters: List<DietTipsChapter>) -> Unit
typealias DietTipsListener = (dietTips: List<DietTip>) -> Unit

/**
 * Repository of diet tips interface.
 *
 * Provides access to the available diet tips.
 */
interface DietTipsRepository : Repository {

    /**
     * Load the list of all available diet tips chapters that may be chosen by the user.
     */
    fun loadDietTipsChapters(): Task<List<DietTipsChapter>>

    /**
     * Load the list of all available diet tips that may be chosen by the user.
     */
    fun loadDietTips(): Task<List<DietTip>>

    /**
     * Listen for the current diet tips changes.
     * The listener is triggered immediately with the current value when calling this method.
     */
    fun addDietTipsListener(listener: DietTipsListener)

    /**
     * Stop listening for the current diet tips changes.
     */
    fun removeDietTipsListener(listener: DietTipsListener)

    /**
     * Load the list of all available diet tips details that may be chosen by the user.
     */
    fun loadDietTipDetails(id: Long): Task<DietTipDetails>

    /**
     * Listen for the current diet tips chapters changes.
     * The listener is triggered immediately with the current value when calling this method.
     */
    fun addDietTipsChaptersListener(listener: DietTipsChaptersListener)

    /**
     * Stop listening for the current diet tips chapters changes.
     */
    fun removeDietTipsChaptersListener(listener: DietTipsChaptersListener)

}
