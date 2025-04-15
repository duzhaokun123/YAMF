package android.app

import android.util.Log
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.init.InitFields
import com.mja.reyamf.xposed.ui.window.AppWindow
import com.mja.reyamf.xposed.ui.window.AppWindow.Companion.TAG
import com.mja.reyamf.xposed.utils.byteBuddyStrategy
import net.bytebuddy.ByteBuddy
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.implementation.bind.annotation.AllArguments
import net.bytebuddy.implementation.bind.annotation.Origin
import net.bytebuddy.implementation.bind.annotation.RuntimeType
import net.bytebuddy.matcher.ElementMatchers
import java.lang.reflect.Method

object ITaskStackListenerProxy {
    fun newInstance(
        classLoader: ClassLoader,
        intercept: (Array<Any?>, Method) -> Any?
    ): ITaskStackListener {
        return ByteBuddy()
            .subclass(ITaskStackListener.Stub::class.java)
            .method(ElementMatchers.any())
            .intercept(MethodDelegation.to(object {
                @RuntimeType
                fun intercept(
                    @AllArguments allArguments: Array<Any?>,
                    @Origin method: Method
                ) {
                    intercept(allArguments, method)
                }
            }))
            .make()
            .load(classLoader, byteBuddyStrategy)
            .loaded
            .getDeclaredConstructor()
            .newInstance()
    }
}