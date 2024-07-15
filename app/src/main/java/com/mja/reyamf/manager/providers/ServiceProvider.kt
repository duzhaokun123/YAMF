package com.mja.reyamf.manager.providers

import android.content.ContentProvider
import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import com.mja.reyamf.manager.services.YAMFManagerProxy

class ServiceProvider: ContentProvider() {
    override fun onCreate() = false

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ) = null

    override fun getType(uri: Uri) = null

    override fun insert(uri: Uri, values: ContentValues?) = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ) = 0

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        if (callingPackage != "android" || extras == null) return null
        val binder = extras.getBinder("binder") ?: return null
        YAMFManagerProxy.linkService(binder)
        return Bundle()
    }
}