package io.github.duzhaokun123.yamf.xposed

import android.os.IBinder
import android.os.IBinder.DeathRecipient
import android.os.Parcel
import android.os.RemoteException
import android.os.ServiceManager
import android.util.Log
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

object YAMFManagerHelper : IYAMFManager, DeathRecipient {
    private const val TAG = "YAMFManagerHelper"

    private class ServiceProxy(private val obj: IYAMFManager) : InvocationHandler {
        override fun invoke(proxy: Any?, method: Method, args: Array<out Any?>?): Any? {
            val result = method.invoke(obj, *args.orEmpty())
            if (result == null) Log.i(TAG, "Call service method ${method.name}")
            else Log.i(
                TAG,
                "Call service method ${method.name} with result " + result.toString().take(20)
            )
            return result
        }
    }

    @Volatile
    private var service: IYAMFManager? = null

    fun linkService(binder: IBinder) {
        service = Proxy.newProxyInstance(
            javaClass.classLoader,
            arrayOf(IYAMFManager::class.java),
            ServiceProxy(IYAMFManager.Stub.asInterface(binder))
        ) as IYAMFManager
        binder.linkToDeath(this, 0)
    }

    override fun binderDied() {
        service = null
        Log.e(TAG, "Binder died")
    }

    override fun asBinder() = service?.asBinder()

    override fun getVersionName(): String? {
        return getService()?.versionName
    }

    override fun getVersionCode() = getService()?.versionCode ?: 0

    override fun getUid() = getService()?.uid ?: -1

    override fun createWindow() {
        getService()?.createWindow()
    }

    override fun getBuildTime(): Long {
        return getService()?.buildTime ?: 0
    }

    override fun getConfigJson(): String {
        return getService()?.configJson ?: "{}"
    }

    override fun updateConfig(newConfig: String) {
        getService()?.updateConfig(newConfig)
    }

    override fun registerOpenCountListener(iOpenCountListener: IOpenCountListener) {
        getService()?.registerOpenCountListener(iOpenCountListener)
    }

    override fun unregisterOpenCountListener(iOpenCountListener: IOpenCountListener) {
        getService()?.unregisterOpenCountListener(iOpenCountListener)
    }

    override fun openAppList() {
        getService()?.openAppList()
    }

    override fun currentToWindow() {
        getService()?.currentToWindow()
    }

    override fun resetAllWindow() {
        getService()?.resetAllWindow()
    }

    private fun getService(): IYAMFManager? {
        return service
    }
}