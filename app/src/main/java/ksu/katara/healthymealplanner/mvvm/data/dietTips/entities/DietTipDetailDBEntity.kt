package ksu.katara.healthymealplanner.mvvm.data.dietTips.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ksu.katara.healthymealplanner.mvvm.domain.dietTips.entities.DietTipDetails

@Entity(
    tableName = "diet_tip_details"
)
data class DietTipDetailDBEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "details_id") val id: Long,
    @ColumnInfo(name = "details_background") val background: String
) {

    fun toDietTipDetails() = DietTipDetails(
        id = id,
        background = background
    )

}
