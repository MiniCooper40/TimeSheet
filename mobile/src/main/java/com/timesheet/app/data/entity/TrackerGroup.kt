package com.timesheet.app.data.entity

import android.graphics.Color
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity
data class TrackerGroup(
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "start_time") val startTime: Long = 0L,
    @ColumnInfo(name = "color") val color: Int = Color.BLACK,
    @ColumnInfo(name = "description") val description: String? = null,
    @PrimaryKey(autoGenerate = true) val uid: Int = 0
)

@Entity(
    indices = [
        Index(
            value = ["group_uid", "tracker_uid"] ,
            unique = true
        )
    ]
)
data class TrackerGroupItem(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "group_uid") val groupUid: Int,
    @ColumnInfo(name = "tracker_uid") val trackerUid: Int
)

//@Entity(primaryKeys = ["group_uid", "tracker_uid"])
//data class TrackerGroupItem(
//    //@PrimaryKey(autoGenerate = true) val uid: Int = 0,
//    @ColumnInfo(name = "group_uid") val groupUid: Int,
//    @ColumnInfo(name = "tracker_uid") val trackerUid: Int
//)

data class GroupWithTrackers(
    @Embedded val group: TrackerGroup,
    @Relation(
        parentColumn = "uid",
        entityColumn = "uid",
        associateBy = Junction(
            TrackerGroupItem::class,
            parentColumn = "group_uid",
            entityColumn = "tracker_uid"
        )
    )
    val trackers: List<TimeTracker>
)