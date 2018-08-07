package com.samudev.spotlog.data

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity

@Entity(primaryKeys = ["track_id", "registered_time"])
data class Song(
        @ColumnInfo(name = "track_id") val trackId: String,
        @ColumnInfo(name = "artist") val artist: String,
        @ColumnInfo(name = "album") val album: String,
        @ColumnInfo(name = "track") val track: String,
        @ColumnInfo(name = "track_length") val trackLengthInSec: Int,
        @ColumnInfo(name = "registered_time") val registeredTime: Long)