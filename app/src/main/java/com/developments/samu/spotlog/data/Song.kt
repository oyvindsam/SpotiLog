package com.developments.samu.spotlog.data

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

fun Song.toPrettyString() =
        with (this) { "$track - $album - $artist"}

fun List<Song>.toPrettyString() =
        this.joinToString(separator = "\n") { song -> song.toPrettyString() }
