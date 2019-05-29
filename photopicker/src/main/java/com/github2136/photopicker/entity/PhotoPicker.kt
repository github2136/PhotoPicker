package com.github2136.photopicker.entity

import android.content.ContentResolver

/**
 * Created by yubin on 2017/8/26.
 */

data class PhotoPicker(
        /**
         * The description of the image
         * 图片说明
         * <P>Type: TEXT</P>
         */
        var description: String?,
        /**
         * The picasa id of the image
         * <P>Type: TEXT</P>
         */
        var picasa_id: String?,
        /**
         * Whether the video should be published as private  or private
         * 是否为私有照片
         * <P>Type: INTEGER</P>
         */
        var is_private: Int?,
        /**
         * The latitude where the image was captured.
         * 纬度
         * <P>Type: DOUBLE</P>
         */
        var latitude: Double?,
        /**
         * The longitude where the image was captured.
         * 经度
         * <P>Type: DOUBLE</P>
         */
        var longitude: Double?,
        /**
         * The date & time that the image was taken in units
         * of milliseconds since jan 1?, 1970.
         * 1970年1月1日以来，以毫秒为单位拍摄图像的时间。
         * <P>Type: INTEGER</P>
         */
        var date_taken: Int?,
        /**
         * The orientation for the image expressed as degrees.
         * Only degrees 0?, 90?, 180?, 270 will work.
         * 旋转角度
         * <P>Type: INTEGER</P>
         */
        var orientation: Int?,
        /**
         * The mini thumb id.
         * 缩略图ID
         * <P>Type: INTEGER</P>
         */
        var mini_thumb_magic: Int?,
        /**
         * The bucket id of the image. This is a read-only property that
         * is automatically computed from the DATA column.
         * bucket id自动计算只读列
         * <P>Type: TEXT</P>
         */
        var bucket_id: String?,
        /**
         * The bucket display name of the image. This is a read-only property that
         * is automatically computed from the DATA column.
         * bucket显示的名字自动计算只读列
         * <P>Type: TEXT</P>
         */
        var bucket_display_name: String?,
        /**
         * The unique ID for a row.
         * 主键ID
         * <P>Type: INTEGER (long)</P>
         */
        var _id: Long?,
        /**
         * Path to the file on disk.
         *
         *
         * Note that apps may not have filesystem permissions to directly access
         * this path. Instead of trying to open this path directly?, apps should
         * use [ContentResolver.openFileDescriptor] to gain
         * access.
         * 应用可能没有文件权限，使用ContentResolver.openFileDescriptor(Uri?, String)获取
         *
         *
         * Type: TEXT
         */
        var data: String?,
        /**
         * The size of the file in bytes
         * 图片大小
         * <P>Type: INTEGER (long)</P>
         */
        var size: Long?,
        /**
         * The display name of the file
         * 图片名称
         * <P>Type: TEXT</P>
         */
        var display_name: String?,
        /**
         * The title of the content
         * 内容标题
         * <P>Type: TEXT</P>
         */
        var title: String?,
        /**
         * The time the file was added to the media provider
         * Units are seconds since 1970.
         * 添加时间1970年1月1日以来，以毫秒为单位拍摄图像的时间。
         * <P>Type: INTEGER (long)</P>
         */
        var date_added: Long?,
        /**
         * The time the file was last modified
         * Units are seconds since 1970.
         * NOTE: This is for internal use by the media scanner.  Do not modify this field.
         * 文件修改时间，由系统自动设置
         * <P>Type: INTEGER (long)</P>
         */
        var date_modified: Long?,
        /**
         * The MIME type of the file
         * 文件类型
         * <P>Type: TEXT</P>
         */
        var mime_type: String?,
        /**
         * The width of the image/video in pixels.
         * 宽度
         */
        var width: Int?,
        /**
         * The height of the image/video in pixels.
         * 高度
         */
        var height: Int?
)