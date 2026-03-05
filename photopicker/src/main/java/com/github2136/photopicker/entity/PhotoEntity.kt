package com.github2136.photopicker.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by 44569 on 2026/3/3
 */
@Parcelize
data class PhotoEntity(
    var photoPath: String,
    var photoInfo: String = "",
) : Parcelable