package com.samudev.spotlog.history


/**
 * Filter songs based on last logged time
 */
class LogTimeFilter {
    companion object {
        const val ONE_MINUTE = 60000L
        const val FIFTEEN_MINUTES = 15 * ONE_MINUTE
        const val ONE_HOUR = 60 * ONE_MINUTE
        const val TWELVE_HOURS = 12 * ONE_HOUR
        const val ALL = 9999999999L
    }
}