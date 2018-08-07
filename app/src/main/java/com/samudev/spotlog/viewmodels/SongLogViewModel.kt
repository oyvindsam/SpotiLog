package com.samudev.spotlog.viewmodels

import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.samudev.spotlog.data.Song
import com.samudev.spotlog.data.SongRepository
import com.samudev.spotlog.history.LogTimeFilter


class SongLogViewModel(private val songRepository: SongRepository) : ViewModel() {

    private val LOG_TAG: String = SongLogViewModel::class.java.simpleName

    private val logFilter = MutableLiveData<Long>()
    private val songLog = MediatorLiveData<List<Song>>()

    init {
        logFilter.value = LogTimeFilter.ONE_HOUR

        val filteredSongLog = Transformations.switchMap(logFilter) {
            if (it == LogTimeFilter.ALL) songRepository.getSongsAll()
            else songRepository.getSongsLatest(it)
        }
        songLog.addSource(filteredSongLog, songLog::setValue)
    }

    fun getSongs() = songLog

    fun removeSong(song: Song) = songRepository.removeSong(song)

    fun clearSongs() = songRepository.clearSongs()

    fun setLogFilter(value: Long) {
        logFilter.value = when(value) {
            LogTimeFilter.FIFTEEN_MINUTES -> LogTimeFilter.FIFTEEN_MINUTES
            LogTimeFilter.ONE_HOUR -> LogTimeFilter.ONE_HOUR
            LogTimeFilter.TWELVE_HOURS -> LogTimeFilter.TWELVE_HOURS
            else -> LogTimeFilter.ALL
        }
    }
}