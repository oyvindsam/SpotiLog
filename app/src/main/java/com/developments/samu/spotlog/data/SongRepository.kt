package com.developments.samu.spotlog.data

import android.arch.lifecycle.LiveData
import com.developments.samu.spotlog.utilities.runOnIoThread
import javax.inject.Inject


private val LOG_TAG: String = SongRepository::class.java.simpleName

class SongRepository @Inject constructor(val songDao: SongDao) {

    private var lastLogged: Song? = null

    fun getSongsLatest(fromTime: Long): LiveData<List<Song>> {
        return songDao.getLatest(System.currentTimeMillis() - fromTime)
    }

    fun getSongsAll(): LiveData<List<Song>> =  songDao.getAll()

    fun getLastLoggedSong(callback: ((Song?) -> Unit)) {
        runOnIoThread {
            lastLogged = songDao.getLastLoggedSong()
            callback(lastLogged)
        }
    }

    // log song if it is not recently logged or is last logged song
    fun logSong(fromTime: Long, logSize: Int, song: Song, callback: ((Song) -> Unit)) {
        runOnIoThread {
            val recentSongs = songDao.getLatestNoLiveData(fromTime)
            val lastSong = songDao.getLastLoggedSong()  // TODO: update logged song if not recent? might cause weird refresh in recyclerView
            if (!(recentSongs.any { s -> s.trackId == song.trackId }) && lastSong?.trackId != song.trackId) {
                songDao.deleteOld(logSize - 1)
                songDao.insertSong(song)
                callback(song)
            }
        }
    }

    // save song
    fun saveSong(song: Song) = runOnIoThread { songDao.insertSong(song) }

    fun removeSong(song: Song) = runOnIoThread { songDao.deleteSong(song) }

    fun clearSongs() = runOnIoThread { songDao.clearTable() }

    fun removeOldSongs(logSize: Int) = runOnIoThread { songDao.deleteOld(logSize) }

}