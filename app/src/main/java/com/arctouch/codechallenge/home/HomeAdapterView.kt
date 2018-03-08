package com.arctouch.codechallenge.home

/**
 * Created by mauker on 08/03/2018.
 */
interface HomeAdapterView {
    fun notifyInserted(position: Int)
    fun notifyRangeInserted(startPos: Int, count: Int)
    fun notifyRemoved(position: Int)
    fun notifyRemovedRage(startPos: Int, count: Int)
    // TODO - Item change notifications.
}