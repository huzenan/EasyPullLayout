# EasyPullLayout

A light Pull Layout for Android that support both VERTICAL and HORIZONTAL. You can wrap any layout you want to support the pulling action.

## Java Demo
See [EasyPullLayoutJavaDemo](https://github.com/huzenan/EasyPullLayoutJavaDemo).

## ScreenShots
### RecyclerView
![recyclerview](https://github.com/huzenan/EasyPullLayout/blob/master/screenshots/recyclerview.gif)
### ListView
![listview](https://github.com/huzenan/EasyPullLayout/blob/master/screenshots/listview.gif)
### ViewPager
![viewpager](https://github.com/huzenan/EasyPullLayout/blob/master/screenshots/viewpager.gif)
### Fixed
![fixed](https://github.com/huzenan/EasyPullLayout/blob/master/screenshots/fixed.gif)
### Nested
![nested](https://github.com/huzenan/EasyPullLayout/blob/master/screenshots/nested.gif)

## Usage
Add to your root build.gradle:
```xml
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Add the dependency:
```xml
dependencies {
    compile 'com.github.huzenan:EasyPullLayout:v1.0.0'
}
```

>layout

Just wrap any layout with EasyPullLayout, then set the 'layout_type' attributes to 'content', 'edge_left', 'edge_top', 'edge_right' or 'edge_bottom':
```xml
<com.hzn.lib.EasyPullLayout
    android:id="@+id/epl"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:max_offset_bottom="150dp"
    app:max_offset_top="150dp"
    app:trigger_offset_bottom="75dp"
    app:trigger_offset_top="75dp">

    <com.hzn.easypulllayout.recyclerview.TransformerView
        android:id="@+id/topView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:layout_type="edge_top" />

    <com.hzn.easypulllayout.recyclerview.TransformerView
        android:id="@+id/bottomView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:layout_type="edge_bottom" />

    <android.support.v7.widget.RecyclerView
        android:id="@+id/rv"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_type="content" />
</com.hzn.lib.EasyPullLayout>
```
>Activity

Then set listeners:
```kotlin
    // set Pull Listener if you have to change the edge view while pulling
    epl.setOnPullListener { type, fraction, changed ->
        when (type) {
            EasyPullLayout.TYPE_EDGE_TOP -> {
                topView.setFraction(START_FRACTION, fraction)
                if (fraction == 1f)
                    topView.ready()
                else
                    topView.idle()
            }
            ...
        }
    }
    // set Trigger Listener, do your actions while the edge view is triggered
    epl.setOnTriggerListener {
        when (it) {
            EasyPullLayout.TYPE_EDGE_TOP -> {
                topView.triggered()
            }
            EasyPullLayout.TYPE_EDGE_BOTTOM -> {
                bottomView.triggered()
            }
            Loading()
        }
    }
```

Also if you want to check the edge yourself, set the Edge Listener(this will cover the default checking process):
```kotlin
    epl.setOnEdgeListener {
        ...
        if (onEdge)
            return EasyPullLayout.TYPE_EDGE_TOP
        ...
    }
```

## LayoutParams
| name                  | description   |
| --------------------- | ------------- |
| layout_type           | content, edge_left, edge_top, edge_right or edge_bottom |

## Attributes
| name                  | description   |
| --------------------- | ------------- |
| trigger_offset_left   | the triggered offset on left side |
| trigger_offset_top    | the triggered offset on top side |
| trigger_offset_right  | the triggered offset on right side |
| trigger_offset_bottom | the triggered offset on bottom side |
| max_offset_left       | the max offset on left side |
| max_offset_top        | the max offset on top side |
| max_offset_right      | the max offset on right side |
| max_offset_bottom     | the max offset on bottom side |
| fixed_content_left    | true if left edge is in content fixed mode |
| fixed_content_top     | true if top edge is in content fixed mode |
| fixed_content_right   | true if right edge is in content fixed mode |
| fixed_content_bottom  | true if bottom edge is in content fixed mode |
| roll_back_duration    | the duration while the edge view rolling back |
| sticky_factor         | 0f~1f, default 0.66f, the factor that decide how sticky while pulling |
