package com.example.xxfile.utils

import android.Manifest
import android.content.Context
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions

/**
 * @program: XieHui
 * @create: 2022-06-18
 **/
fun Context.requestPermission(vararg permission: String, callback: (Boolean) -> Unit) {
    var isCallbackInvoked = false //标识回调是否已执行（onGranted、onDenied只能执行其中之一）
    XXPermissions.with(this)
        .permission(permission)
        .request(object : OnPermissionCallback {
            override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                if (!isCallbackInvoked) {
                    isCallbackInvoked = true
                    callback.invoke(all)
                }
            }

            override fun onDenied(permissions: MutableList<String>?, never: Boolean) {
                if (!isCallbackInvoked) {
                    isCallbackInvoked = true
                    callback.invoke(false)
                }
            }
        })
}

fun Context.requestPermission2(vararg permission: String, callback: (Boolean, Boolean) -> Unit) {
    var isCallbackInvoked = false //标识回调是否已执行（onGranted、onDenied只能执行其中之一）
    XXPermissions.with(this)
        .permission(permission)
        .request(object : OnPermissionCallback {
            override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                //只在所有权限都已授权时执行
                if (all && !isCallbackInvoked) {
                    isCallbackInvoked = true
                    callback.invoke(true, false)
                }
            }

            override fun onDenied(permissions: MutableList<String>?, never: Boolean) {
                if (!isCallbackInvoked) {
                    isCallbackInvoked = true
                    callback.invoke(false, never)
                }
            }
        })

}

fun Context.hasPermission(vararg permission: String, callback: (Boolean) -> Unit) {
    callback(XXPermissions.isGranted(this, permission))
}

fun Context.hasPermission(vararg permission: String): Boolean {
    return XXPermissions.isGranted(this, permission)
}

fun Context.hasLocationPermission(): Boolean {
    return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
}