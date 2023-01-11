package io.github.duzhaokun123.yamf.xposed

import android.os.IBinder.DeathRecipient
import android.os.Parcel
import android.os.RemoteException
import android.os.ServiceManager
import android.util.Log
import android.view.Surface
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

object YAMFManagerHelper : IYAMFManager, DeathRecipient {
    private const val TAG = "YAMFManagerHelper"

    private class ServiceProxy(private val obj: IYAMFManager) : InvocationHandler {
        override fun invoke(proxy: Any?, method: Method, args: Array<out Any?>?): Any? {
            val result = method.invoke(obj, *args.orEmpty())
            if (result == null) Log.i(TAG, "Call service method ${method.name}")
            else Log.i(TAG, "Call service method ${method.name} with result " + result.toString().take(20))
            return result
        }
    }

    @Volatile
    private var service: IYAMFManager? = null

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

    override fun createWindow(densityDpi: Int, flags: Int, taskId: Int): Int {
        return getService()?.createWindow(densityDpi, flags, taskId) ?: 0
    }

//    override fun createVirtualDisplay(
//        name: String,
//        width: Int,
//        height: Int,
//        densityDpi: Int,
//        surface: Surface?,
//        flags: Int
//    ): Int {
//        return getService()?.createVirtualDisplay(name, width, height, densityDpi, surface, flags) ?: 0
//    }
//
//    override fun resizeVirtualDisplay(id: Int, width: Int, height: Int, densityDpi: Int): Boolean {
//        return getService()?.resizeVirtualDisplay(id, width, height, densityDpi) ?: false
//    }
//
//    override fun setVirtualDisplaySurface(id: Int, surface: Surface?): Boolean {
//        return getService()?.setVirtualDisplaySurface(id, surface) ?: false
//    }
//
//    override fun releaseVirtualDisplay(id: Int): Boolean {
//        return getService()?.releaseVirtualDisplay(id) ?: false
//    }
//
//    override fun releaseAll(): Boolean {
//        return getService()?.releaseAll() ?: false
//    }
//
//    override fun getVirtualDisplayInfoS(id: Int): String? {
//        return getService()?.getVirtualDisplayInfoS(id)
//    }
//
//    override fun getVirtualDisplayIds(): IntArray? {
//        return getService()?.virtualDisplayIds
//    }
//
//    override fun showOverlay() {
//        getService()?.showOverlay()
//    }

    private fun getService(): IYAMFManager? {
        if (service != null) return service
        val pm = ServiceManager.getService("package")
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        val remote = try {
            data.writeInterfaceToken(BridgeService.DESCRIPTOR)
            data.writeInt(BridgeService.ACTION_GET_BINDER)
            pm.transact(BridgeService.TRANSACTION, data, reply, 0)
            reply.readException()
            val binder = reply.readStrongBinder()
            IYAMFManager.Stub.asInterface(binder)
        } catch (e: RemoteException) {
            Log.d(TAG, "Failed to get binder")
            null
        } finally {
            data.recycle()
            reply.recycle()
        }
        if (remote != null) {
            Log.i(TAG, "Binder acquired")
            remote.asBinder().linkToDeath(this, 0)
            service = Proxy.newProxyInstance(
                javaClass.classLoader,
                arrayOf(IYAMFManager::class.java),
                ServiceProxy(remote)
            ) as IYAMFManager
        }
        return service
    }
}