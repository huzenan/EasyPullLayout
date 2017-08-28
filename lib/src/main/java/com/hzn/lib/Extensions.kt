package com.hzn.lib

import android.view.View

/**
 * Extensions
 *
 * Created by huzenan on 2017/8/3.
 */
internal fun HashMap<View, EasyPullLayout.ChildViewAttr>.getByType(type: Int?): View? {
    for ((key) in this)
        if ((key.layoutParams as EasyPullLayout.LayoutParams).type == type)
            return key
    return null
}