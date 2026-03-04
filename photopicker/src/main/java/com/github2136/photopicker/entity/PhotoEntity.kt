package com.github2136.photopicker.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by 44569 on 2026/3/3
 */
@Parcelize
data class PhotoEntity(
    val photoPath: String,
    val photoInfo: String,
): Parcelable