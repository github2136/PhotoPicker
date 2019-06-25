图片选择器  
[![](https://jitpack.io/v/github2136/PhotoPicker.svg)](https://jitpack.io/#github2136/PhotoPicker)  
使用该库还需要引用以下库  
**com.github.chrisbanes:PhotoView:2.1.3**  

**样式控制**  
* **pickerToolBarBg** 标题颜色  
* **pickerToolBarDark** 状态栏颜色  
* **pickerToolBarText** 标题文字颜色  
* **pickerAccent** 图片复选框颜色  
* **pickerBottomTextColor** 预览底部文字颜色  
* **pickerImgColor** 加载、错误图片颜色  

**自定义图片加载器**  
* 实现`com.github2136.photopicker.other.ImageLoader`接口  
* 在AndroidManifest.xml中添加`meta-data`，value为`picker_image_loader`，name为实现`ImageLoader`接口类完整路径  
---
* **PhotoPickerActivity**选择图片  
    **请求参数**  

    * **ARG_PICKER_COUNT**可选择的图片总数  

    **返回参数**  

    * **ARG_RESULT**返回的结果  

* **PhotoViewActivity**图片浏览（如果传入已选图片的路径，则可以在图片浏览时修改选择的结果）  
    **请求参数**  

    * **ARG_PHOTOS**显示图片路径   
    * **ARG_CURRENT_INDEX**显示的图片下标  
    * **ARG_PICKER_PATHS**已选中图片路径  
    * **ARG_PICKER_COUNT**可选图片数量  

    **返回参数**  
    * **ARG_PICKER_PATHS**返回的结果  

    > 如果ARG_PICKER_PATHS不为空则会在下方显示单选框选择图片 返回路径的key为ARG_PICKER_PATHS  

* **CaptureActivity**拍摄图片（默认存储在项目私有目录的Pictures下，或在application中添加name为photo_picker_path的&lt;meta&#62;）  
    **请求参数**  

    * **ARG_FILE_PATH**保存目录可以为空  

    **返回参数**  

    * **ARG_RESULT**返回的结果  

* **CropActivity**裁剪图片（默认存储在项目私有目录的Pictures下，或在application中添加name为photo_picker_path的&lt;meta&#62;，在Android6.0以上必须使用分享方式传入uri路径，而不是使用直接的文件路径）   
    **请求参数** 
    * **ARG_CROP_IMG**被裁剪的图片路径  
    * **ARG_ASPECT_X**裁剪框比例X  
    * **ARG_ASPECT_Y**裁剪框比例Y  
    * **ARG_OUTPUT_X**输出图片X  
    * **ARG_OUTPUT_Y**输出图片Y  
    * **ARG_OUTPUT_IMG**保存目录可以为空

    返回参数  
    **ARG_RESULT**返回的结果  

