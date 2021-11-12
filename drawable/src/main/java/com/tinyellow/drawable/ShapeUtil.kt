package com.tinyellow.drawable

import android.R
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.*
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.FloatRange

fun View.selector(): Selector.ShapeEditor = Selector(this).editNormal()



fun TextView.fontSelector(): FontSelector = FontSelector(this)

fun View.shape(): Shape = Shape(this)

fun View.shapeOval(): ShapeOval = ShapeOval(this)

fun View.imageSelector(): ImageSelector = ImageSelector(this)

class Selector(private val view: View) {
    private var normalShape: ShapeEditor? = null
    private var pressShape: ShapeEditor? = null
    private var disableShape: ShapeEditor? = null
    private var selectedShape: ShapeEditor? = null
    private var unit: Convert
    private var defPressDark = 0.1f
    private var pressRipple = false

    fun setUnit(unit: Convert): Selector {
        this.unit = unit
        return this
    }

    fun defPressDark(@FloatRange(from = 0.0, to = 1.0) ratio: Float) {
        defPressDark = ratio
        if(null != normalShape && null != pressShape){
            normalShape!!.colors?.let {
                val length = it.size
                for (i in 0 until length) {
                    pressShape!!.colors[i] = translateDark(it[i],ratio)
                }
            }
        }
    }

    fun editNormal(): ShapeEditor {
        if (null == normalShape) {
            normalShape = ShapeEditor(this)
        }
        return normalShape!!
    }

    fun editPress(isRipple: Boolean = false): ShapeEditor {
        if (null == pressShape) {
            pressShape = ShapeEditor(this)
            //默认跟正常状态一样
            if (null != normalShape) {
                defPressDark(defPressDark)
                val length = normalShape!!.radius.size
                for (i in 0 until length) {
                    pressShape!!.radius[i] = normalShape!!.radius[i]
                }
            }
        }
        pressRipple = isRipple
        return pressShape!!
    }

    fun editDisable(): ShapeEditor {
        if (null == disableShape) {
            disableShape = ShapeEditor(this)

            normalShape?.radius?.let {
                val length = it.size
                for (i in 0 until length) {
                    disableShape!!.radius[i] = it[i]
                }
            }

            normalShape?.colors?.let {
                val length = it.size
                for (i in 0 until length) {
                    disableShape!!.colors[i] = alpha(it[i],0.5f)
                }
            }

        }
        return disableShape!!
    }

    fun editSelected(): ShapeEditor {
        if (null == selectedShape) {
            selectedShape = ShapeEditor(this)
            normalShape?.radius?.let {
                val length = it.size
                for (i in 0 until length) {
                    selectedShape!!.radius[i] = it[i]
                }
            }
        }
        return selectedShape!!
    }//            if(null!=pressShape){
//                RoundRectShape roundRectShape = new RoundRectShape(pressShape.radius, null, null);
//                maskDrawable = new ShapeDrawable();
//                maskDrawable.setShape(roundRectShape);
//                maskDrawable.getPaint().setColor(Color.parseColor("#000000"));
//                maskDrawable.getPaint().setStyle(Paint.Style.FILL);
//            }

    //注意一点添加state时，是有顺序的，stateListDrawable会先执行最新添加的state，如果不是该state，在执行下面的state，如果把大范围的state放到前面添加，会导致直接执行大范围的state，而不执行后面的state。此外，在添加state中，在state前添加“-”号，表示此state为false（例如：-android.R.attr.state_selected），否则为true。
    val drawable: Drawable
        get() {
            var rippleColor = -1
            //注意一点添加state时，是有顺序的，stateListDrawable会先执行最新添加的state，如果不是该state，在执行下面的state，如果把大范围的state放到前面添加，会导致直接执行大范围的state，而不执行后面的state。此外，在添加state中，在state前添加“-”号，表示此state为false（例如：-android.R.attr.state_selected），否则为true。
            val sld = StateListDrawable()
            if (null != disableShape) {
                val colors = disableShape!!.colors
                val pressed = Shape.getDrawableCompat(
                    disableShape!!.radius,
                    colors,
                    disableShape!!.orientation
                )
                setStroke(pressed, disableShape!!)
                sld.addState(intArrayOf(-R.attr.state_enabled), pressed)
            }
            if (null != selectedShape) {
                val colors = selectedShape!!.colors
                val selected = Shape.getDrawableCompat(
                    selectedShape!!.radius,
                    colors,
                    selectedShape!!.orientation
                )
                setStroke(selected, selectedShape!!)
                sld.addState(intArrayOf(R.attr.state_selected), selected)
                sld.addState(intArrayOf(R.attr.state_checked), selected)
            }
            if (null != pressShape) {
                val colors = pressShape!!.colors
                if (pressRipple && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    rippleColor = colors[0]
                } else {
                    val pressed = Shape.getDrawableCompat(
                        pressShape!!.radius,
                        colors,
                        pressShape!!.orientation
                    )
                    setStroke(pressed, pressShape!!)
                    sld.addState(intArrayOf(R.attr.state_pressed), pressed)
                }
            } else if (null != normalShape) {
                val length = normalShape?.colors?.size?:0
                val colors = IntArray(length)
                for (i in 0 until length) {
                    colors[i] = translateDark(normalShape!!.colors[i], defPressDark)
                }
                rippleColor = colors[0]
                if (pressRipple && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    rippleColor = colors[0]
                } else {
                    val pressed = Shape.getDrawableCompat(
                        normalShape!!.radius,
                        colors,
                        normalShape!!.orientation
                    )
                    setStroke(pressed, normalShape!!)
                    sld.addState(intArrayOf(R.attr.state_pressed), pressed)
                }
            }
            if (null != normalShape) {
                val colors = normalShape!!.colors
                val radius =  if (pressRipple && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP&&null != pressShape){
                    pressShape?.radius?:normalShape!!.radius
                }else{
                    normalShape!!.radius
                }
                val normal = Shape.getDrawableCompat(radius, colors, normalShape!!.orientation)
                setStroke(normal, normalShape!!)
                sld.addState(intArrayOf(), normal)
            }
            if (pressRipple && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && -1 != rippleColor) {
                val stateList = arrayOf(intArrayOf(R.attr.state_pressed), intArrayOf(R.attr.state_focused), intArrayOf(R.attr.state_activated),intArrayOf(R.attr.state_window_focused))
                var maskDrawable: ShapeDrawable? = null
//                if(null!=pressShape){
//                val roundRectShape = RoundRectShape(pressShape!!.radius, null, null);
//                maskDrawable = ShapeDrawable()
//                maskDrawable.setShape(roundRectShape);
//                maskDrawable.getPaint().setColor(Color.parseColor("#000000"));
//                maskDrawable.getPaint().setStyle(Paint.Style.FILL)
//                }
                val stateColorList = intArrayOf(rippleColor, rippleColor, rippleColor,rippleColor)
                val colorStateList = ColorStateList(stateList, stateColorList)
                return RippleDrawable(colorStateList, sld, maskDrawable)
            }
            return sld
        }

    private fun setStroke(drawable: GradientDrawable, shapeEditor: ShapeEditor) {
        if (0 == shapeEditor.dashWidth && 0 == shapeEditor.dashGap) {
            drawable.setStroke(shapeEditor.strokeWidth, shapeEditor.strokeColor)
        } else {
            drawable.setStroke(shapeEditor.strokeWidth, shapeEditor.strokeColor, shapeEditor.dashWidth.toFloat(), shapeEditor.dashGap.toFloat())
            view.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
    }

    fun setBackground() {
        setBackgroundCompat(drawable, view)
    }

    class ShapeEditor(private val selector: Selector) {
        var radius = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        var colors = intArrayOf(Color.WHITE, Color.WHITE)
        //    int bgColor;
        var strokeWidth = 0
        var strokeColor = Color.TRANSPARENT
        /**
         * 在android中设置虚线需要
         * 1. 用shape画虚线使用时，控件的layout_height一定要大于shape中stroke标签的width属性
         * 2. 将该控件设置关闭硬件加速（这里会处理）
         */
        var dashWidth = 0
        var dashGap = 0
        var orientation = GradientDrawable.Orientation.LEFT_RIGHT
        fun radius(r: Float): ShapeEditor {
            val r_ = selector.unit.convert(r)
            radius = floatArrayOf(r_, r_, r_, r_, r_, r_, r_, r_)
            return this
        }

        fun radius(topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float): ShapeEditor {
            val topLeft_ = selector.unit.convert(topLeft)
            val topRight_ = selector.unit.convert(topRight)
            val bottomRight_ = selector.unit.convert(bottomRight)
            val bottomLeft_ = selector.unit.convert(bottomLeft)
            radius = floatArrayOf(topLeft_, topLeft_, topRight_, topRight_, bottomRight_, bottomRight_, bottomLeft_, bottomLeft_)
            return this
        }

        fun radius(topLeftX: Float, topLeftY: Float, topRightX: Float, topRightY: Float, bottomRightX: Float, bottomRightY: Float, bottomLeftX: Float, bottomLeftY: Float): ShapeEditor {
            val topLeftX_ = selector.unit.convert(topLeftX)
            val topLeftY_ = selector.unit.convert(topLeftY)
            val topRightX_ = selector.unit.convert(topRightX)
            val topRightY_ = selector.unit.convert(topRightY)
            val bottomRightX_ = selector.unit.convert(bottomRightX)
            val bottomRightY_ = selector.unit.convert(bottomRightY)
            val bottomLeftX_ = selector.unit.convert(bottomLeftX)
            val bottomLeftY_ = selector.unit.convert(bottomLeftY)
            radius = floatArrayOf(topLeftX_, topLeftY_, topRightX_, topRightY_, bottomRightX_, bottomRightY_, bottomLeftX_, bottomLeftY_)
            return this
        }

        fun colors(@ColorInt vararg colors: Int): ShapeEditor {
            if (colors.size < 1) {
                this.colors = intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT)
            } else if (colors.size < 2) {
                this.colors = intArrayOf(colors[0], colors[0])
            } else {
                this.colors = colors
            }
            return this
        }

        fun colorRes(@ColorRes vararg res: Int): ShapeEditor {
            if (res.size < 1) {
                colors = intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT)
            } else if (res.size < 2) {
                val color = ContextCompat.getColor(selector.view.context, res[0])
                colors = intArrayOf(color, color)
            } else {
                colors = IntArray(res.size)
                val context = selector.view.context
                for (i in 0 until res.size) {
                    colors[i] = ContextCompat.getColor(context, res[i])
                }
            }
            return this
        }

        fun stroke(width: Float, color: Int): ShapeEditor {
            strokeWidth = selector.unit.convert(width).toInt()
            strokeColor = color
            return this
        }

        fun strokeRes(width: Float, @ColorRes colorRes: Int): ShapeEditor {
            strokeWidth = selector.unit.convert(width).toInt()
            strokeColor = ContextCompat.getColor(selector.view.context, colorRes)
            return this
        }

        fun dash(width: Int, gap: Int): ShapeEditor {
            dashWidth = selector.unit.convert(width.toFloat()).toInt()
            dashGap = selector.unit.convert(gap.toFloat()).toInt()
            return this
        }

        fun orientation(orientation: GradientDrawable.Orientation): ShapeEditor {
            this.orientation = orientation
            return this
        }

        fun defPressDark(@FloatRange(from = 0.0, to = 1.0) ratio: Float): ShapeEditor {
            selector.defPressDark(ratio)
            return this
        }

        fun editNormal(): ShapeEditor {
            return selector.editNormal()
        }

        fun editDisable(): ShapeEditor {
            return selector.editDisable()
        }

        fun editSelected(): ShapeEditor {
            return selector.editSelected()
        }

        fun editPress(): ShapeEditor {
            return selector.editPress(false)
        }

        fun editRipple(): ShapeEditor {
            return selector.editPress(true)
        }

        fun drawable(): Drawable {
            return selector.drawable
        }

        fun setBackground() {
            selector.setBackground()
        }

    }

    companion object {

        @JvmStatic
        fun translateDark(color: Int, ratio: Float): Int {
            var ratio = ratio
            ratio = 1f - ratio
            val a = color shr 24 and 0xFF
            val r = ((color shr 16 and 0xFF) * ratio).toInt()
            val g = ((color shr 8 and 0xFF) * ratio).toInt()
            val b = ((color and 0xFF) * ratio).toInt()
            return a shl 24 or (r shl 16) or (g shl 8) or b
        }
    }

    init {
        unit = DpUnit(view.context)
    }
}

class Shape(private val view: View) {
    private var unit: Convert
    private var color = Color.TRANSPARENT
    private var radius = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
    private var strokeWidth = 0
    private var strokeColor = Color.TRANSPARENT
    private var width = 0
    private var height = 0
    /**
     * 在android中设置虚线需要
     * 1. 用shape画虚线使用时，控件的layout_height一定要大于shape中stroke标签的width属性
     * 2. 将该控件设置关闭硬件加速（这里会处理）
     */
    private var dashWidth = 0
    private var dashGap = 0
    fun setUnit(unit: Convert): Shape {
        this.unit = unit
        return this
    }

    fun radius(r: Float): Shape {
        val r_ = unit.convert(r)
        radius = floatArrayOf(r_, r_, r_, r_, r_, r_, r_, r_)
        return this
    }

    fun radius(topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float): Shape {
        val topLeft_ = unit.convert(topLeft)
        val topRight_ = unit.convert(topRight)
        val bottomRight_ = unit.convert(bottomRight)
        val bottomLeft_ = unit.convert(bottomLeft)
        radius = floatArrayOf(topLeft_, topLeft_, topRight_, topRight_, bottomRight_, bottomRight_, bottomLeft_, bottomLeft_)
        return this
    }

    fun radius(topLeftX: Float, topLeftY: Float, topRightX: Float, topRightY: Float, bottomRightX: Float, bottomRightY: Float, bottomLeftX: Float, bottomLeftY: Float): Shape {
        val topLeftX_ = unit.convert(topLeftX)
        val topLeftY_ = unit.convert(topLeftY)
        val topRightX_ = unit.convert(topRightX)
        val topRightY_ = unit.convert(topRightY)
        val bottomRightX_ = unit.convert(bottomRightX)
        val bottomRightY_ = unit.convert(bottomRightY)
        val bottomLeftX_ = unit.convert(bottomLeftX)
        val bottomLeftY_ = unit.convert(bottomLeftY)
        radius = floatArrayOf(topLeftX_, topLeftY_, topRightX_, topRightY_, bottomRightX_, bottomRightY_, bottomLeftX_, bottomLeftY_)
        return this
    }

    fun stroke(width: Float, color: Int): Shape {
        strokeWidth = unit.convert(width).toInt()
        strokeColor = color
        return this
    }

    fun strokeRes(width: Float, @ColorRes colorRes: Int): Shape {
        strokeWidth = unit.convert(width).toInt()
        strokeColor = ContextCompat.getColor(view.context, colorRes)
        return this
    }

    fun color(@ColorInt color: Int): Shape {
        this.color = color
        return this
    }

    fun color(@ColorInt color: Int,@FloatRange(from = 0.0, to = 1.0) colorAlpha:Float = 1f): Shape {
        this.color = alpha(color,colorAlpha)
        return this
    }

    fun colorRes(@ColorRes colorRes: Int): Shape {
        color = ContextCompat.getColor(view.context, colorRes)
        return this
    }

    fun colorRes(@ColorRes colorRes: Int,@FloatRange(from = 0.0, to = 1.0) colorAlpha:Float = 1f): Shape {
        color = alpha(ContextCompat.getColor(view.context, colorRes),colorAlpha)
        return this
    }

    fun dash(width: Int, gap: Int): Shape {
        dashWidth = unit.convert(width.toFloat()).toInt()
        dashGap = unit.convert(gap.toFloat()).toInt()
        return this
    }

    fun size(width:Int,height:Int): Shape {
        this.width = unit.convert(width.toFloat()).toInt()
        this.height = unit.convert(height.toFloat()).toInt()
        return this
    }

    fun setBackground(){
        setBackgroundCompat(drawable, view)
    }

    //        Drawable drawable = view.getBackground();
//        if(null != drawable && drawable instanceof ShapeDrawable){
//            shapeDrawable = (ShapeDrawable) drawable;
//        }
//        if(null == shapeDrawable){
//            shapeDrawable = new ShapeDrawable();
//        }
    val drawable: Drawable
        get() { //        Drawable drawable = view.getBackground();
//        if(null != drawable && drawable instanceof ShapeDrawable){
//            shapeDrawable = (ShapeDrawable) drawable;
//        }
//        if(null == shapeDrawable){
//            shapeDrawable = new ShapeDrawable();
//        }
            val drawable = getDrawableCompat(radius, intArrayOf(color, color), GradientDrawable.Orientation.LEFT_RIGHT)
            if (0 == dashWidth && 0 == dashGap) {
                drawable.setStroke(strokeWidth, strokeColor)
            } else {
                drawable.setStroke(strokeWidth, strokeColor, dashWidth.toFloat(), dashGap.toFloat())
                view.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            }
            drawable.setSize(width, height)
            return drawable
        }



    companion object {
        /**
         *
         * @param radius 四个角的半径
         * @param colors 渐变的颜色
         * @return
         */
        fun getDrawableCompat(radius: FloatArray?, colors: IntArray?, orientation: GradientDrawable.Orientation?): GradientDrawable { //TODO:判断版本是否大于16  项目中默认的都是Linear散射 都是从左到右 都是只有开始颜色和结束颜色
            val drawable: GradientDrawable
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                drawable = GradientDrawable()
                drawable.orientation = orientation
                drawable.colors = colors
            } else {
                drawable = GradientDrawable(orientation, colors)
            }
            drawable.cornerRadii = radius
            drawable.gradientType = GradientDrawable.LINEAR_GRADIENT
            return drawable
        }

    }

    init {
        unit = DpUnit(view.context)
    }
}

class ShapeOval(private val view: View) {
    private var unit: Convert
    private var color = Color.TRANSPARENT
    private var radius = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
    private var strokeWidth = 0
    private var strokeColor = Color.TRANSPARENT
    private var width = 0
    private var height = 0
    /**
     * 在android中设置虚线需要
     * 1. 用shape画虚线使用时，控件的layout_height一定要大于shape中stroke标签的width属性
     * 2. 将该控件设置关闭硬件加速（这里会处理）
     */
    private var dashWidth = 0
    private var dashGap = 0

    fun setUnit(unit: Convert): ShapeOval {
        this.unit = unit
        return this
    }

    fun radius(r: Float): ShapeOval {
        val r_ = unit.convert(r)
        radius = floatArrayOf(r_, r_, r_, r_, r_, r_, r_, r_)
        return this
    }

    fun radius(topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float): ShapeOval {
        val topLeft_ = unit.convert(topLeft)
        val topRight_ = unit.convert(topRight)
        val bottomRight_ = unit.convert(bottomRight)
        val bottomLeft_ = unit.convert(bottomLeft)
        radius = floatArrayOf(topLeft_, topLeft_, topRight_, topRight_, bottomRight_, bottomRight_, bottomLeft_, bottomLeft_)
        return this
    }

    fun radius(topLeftX: Float, topLeftY: Float, topRightX: Float, topRightY: Float, bottomRightX: Float, bottomRightY: Float, bottomLeftX: Float, bottomLeftY: Float): ShapeOval {
        val topLeftX_ = unit.convert(topLeftX)
        val topLeftY_ = unit.convert(topLeftY)
        val topRightX_ = unit.convert(topRightX)
        val topRightY_ = unit.convert(topRightY)
        val bottomRightX_ = unit.convert(bottomRightX)
        val bottomRightY_ = unit.convert(bottomRightY)
        val bottomLeftX_ = unit.convert(bottomLeftX)
        val bottomLeftY_ = unit.convert(bottomLeftY)
        radius = floatArrayOf(topLeftX_, topLeftY_, topRightX_, topRightY_, bottomRightX_, bottomRightY_, bottomLeftX_, bottomLeftY_)
        return this
    }

    fun stroke(width: Float, color: Int): ShapeOval {
        strokeWidth = unit.convert(width).toInt()
        strokeColor = color
        return this
    }

    fun strokeRes(width: Float, @ColorRes colorRes: Int): ShapeOval {
        strokeWidth = unit.convert(width).toInt()
        strokeColor = ContextCompat.getColor(view.context, colorRes)
        return this
    }

    fun color(@ColorInt color: Int): ShapeOval {
        this.color = color
        return this
    }

    fun color(@ColorInt color: Int,@FloatRange(from = 0.0, to = 1.0) colorAlpha:Float = 1f): ShapeOval {
        this.color = alpha(color,colorAlpha)
        return this
    }

    fun colorRes(@ColorRes colorRes: Int): ShapeOval {
        color = ContextCompat.getColor(view.context, colorRes)
        return this
    }

    fun colorRes(@ColorRes colorRes: Int,@FloatRange(from = 0.0, to = 1.0) colorAlpha:Float = 1f): ShapeOval {
        color = alpha(ContextCompat.getColor(view.context, colorRes),colorAlpha)
        return this
    }

    fun dash(width: Int, gap: Int): ShapeOval {
        dashWidth = unit.convert(width.toFloat()).toInt()
        dashGap = unit.convert(gap.toFloat()).toInt()
        return this
    }

    fun size(width:Int,height:Int): ShapeOval {
        this.width = unit.convert(width.toFloat()).toInt()
        this.height = unit.convert(height.toFloat()).toInt()
        return this
    }

    fun setBackground(){
        setBackgroundCompat(drawable, view)
    }

    val drawable: Drawable
        get() {
            val shapeDrawable = ShapeDrawable(OvalShape())
            shapeDrawable.paint.style = Paint.Style.FILL
            shapeDrawable.paint.color = color
            return shapeDrawable
        }

    init {
        unit = DpUnit(view.context)
    }
}

class FontSelector(private val textView: TextView){

    private var color = Color.WHITE

    private var pressColor = 0

    private var disableColor = 0

    private var selectedColor = 0


    fun color(@ColorInt color: Int): FontSelector {
        this.color = color
        return this
    }

    fun colorRes(@ColorRes color: Int): FontSelector {
        this.color = ContextCompat.getColor(textView.context, color)
        return this
    }

    fun pressColor(color: Int): FontSelector {
        pressColor = color
        return this
    }

    fun pressColorRes(@ColorRes color: Int): FontSelector {
        this.pressColor = ContextCompat.getColor(textView.context, color)
        return this
    }
    fun pressColorRes(@ColorRes color: Int,@FloatRange(from = 0.0, to = 1.0) colorAlpha:Float = 1f): FontSelector {
        this.pressColor = alpha(ContextCompat.getColor(textView.context, color),colorAlpha)
        return this
    }


    fun disableColor(color: Int): FontSelector {
        disableColor = color
        return this
    }

    fun disableColorRes(@ColorRes color: Int): FontSelector {
        this.disableColor = ContextCompat.getColor(textView.context, color)
        return this
    }

    fun selectedColor(color: Int): FontSelector {
        selectedColor = color
        return this
    }

    fun selectedColorRes(@ColorRes color: Int): FontSelector {
        this.selectedColor = ContextCompat.getColor(textView.context, color)
        return this
    }

    fun getColors():ColorStateList{
        val stateList = mutableListOf<IntArray>()
        val stateColorList = mutableListOf<Int>()
        if(0 != pressColor){
            stateList.add(intArrayOf(R.attr.state_pressed))
            stateColorList.add(pressColor)
        }

        if(0 != disableColor){
            stateList.add(intArrayOf(-R.attr.state_enabled))
            stateColorList.add(disableColor)
        }

        if(0 != selectedColor){
            stateList.add(intArrayOf(R.attr.state_selected))
            stateColorList.add(selectedColor)

            stateList.add(intArrayOf(R.attr.state_checked))
            stateColorList.add(selectedColor)
        }
        stateList.add(intArrayOf())
        stateColorList.add(color)
//        StateListDrawable drawable =newStateListDrawable();
//
//                         //选中
//
//                         drawable.addState(new int[]{android.R.attr.state_checked},drawableSelect);
//
//                         //未选中
//
//                         drawable.addState(new int[]{-android.R.attr.state_checked},drawableNormal);
//
//                         cbButton.setBackgroundDrawable(drawable);

        return ColorStateList(stateList.toTypedArray(), stateColorList.toIntArray())
    }

    fun setColor():ColorStateList{
        val colors = getColors()
        textView.setTextColor(colors)
        return colors
    }
}

class ImageSelector(private val view: View){

    private var normalDrawable :Drawable ?= null

    private var pressDrawable :Drawable ?= null

    private var disableDrawable :Drawable ?= null

    private var selectedDrawable :Drawable ?= null

    private var rippleDrawable :ShapeDrawable ?= null

    private var rippleColor = Color.parseColor("#eeeeee")

    private var rippleCompat = true

    private var rippleAlpha = 0.8f


    fun normal(drawable: Drawable?): ImageSelector {
        this.normalDrawable = drawable
        return this
    }

    fun normalRes(@DrawableRes res: Int): ImageSelector {
        this.normalDrawable = ContextCompat.getDrawable(view.context, res)
        return this
    }

    fun press(drawable: Drawable?): ImageSelector {
        this.pressDrawable = drawable
        return this
    }

    fun pressRes(@DrawableRes res: Int): ImageSelector {
        this.pressDrawable = ContextCompat.getDrawable(view.context, res)
        return this
    }

    fun disable(drawable: Drawable?): ImageSelector {
        disableDrawable = drawable
        return this
    }

    fun disableRes(@DrawableRes res: Int): ImageSelector {
        this.disableDrawable = ContextCompat.getDrawable(view.context, res)
        return this
    }

    fun selected(drawable: Drawable?): ImageSelector {
        selectedDrawable = drawable
        return this
    }

    fun selectedRes(@DrawableRes res: Int): ImageSelector {
        this.selectedDrawable = ContextCompat.getDrawable(view.context, res)
        return this
    }

    fun rippleOval(): ImageSelector {
        rippleDrawable = ShapeDrawable(OvalShape())
        return this
    }

    fun rippleRect(radiusDp:Float): ImageSelector {
        val radiusValue = DpUnit(view.context).convert(radiusDp)
        var radius = floatArrayOf(radiusValue, radiusValue, radiusValue, radiusValue, radiusValue, radiusValue, radiusValue, radiusValue)
        val roundRectShape = RoundRectShape(radius, null, null)
        rippleDrawable = ShapeDrawable(roundRectShape)
        return this
    }

    fun rippleColor(color: Int): ImageSelector {
        this.rippleColor = color
        return this
    }

    fun rippleColorRes(@ColorRes colorRes: Int): ImageSelector {
        this.rippleColor = ContextCompat.getColor(view.context, colorRes)
        return this
    }

    fun rippleColor(color: Int,@FloatRange(from = 0.0, to = 1.0) colorAlpha:Float = 1f): ImageSelector {
        this.rippleColor = color
        this.rippleAlpha = colorAlpha
        return this
    }

    fun rippleColorRes(@ColorRes colorRes: Int,@FloatRange(from = 0.0, to = 1.0) colorAlpha:Float = 1f): ImageSelector {
        this.rippleColor = ContextCompat.getColor(view.context, colorRes)
        this.rippleAlpha = colorAlpha
        return this
    }

    fun rippleCompat(compat: Boolean): ImageSelector {
        this.rippleCompat = compat
        return this
    }



    fun drawable():StateListDrawable{
        val drawable = StateListDrawable()

        pressDrawable?.let {
            drawable.addState(intArrayOf(R.attr.state_pressed), it)
        }

        disableDrawable?.let {
            drawable.addState(intArrayOf(-R.attr.state_enabled), it)
        }

        selectedDrawable?.let {
            drawable.addState(intArrayOf(R.attr.state_selected), it)
            drawable.addState(intArrayOf(R.attr.state_checked), it)
        }

        normalDrawable?.let {
            drawable.addState(intArrayOf(), it)
        }?:let{
            if(view is ImageView){
                view.drawable?.let {
                    drawable.addState(intArrayOf(), it)
                }
            }
        }
        return drawable
    }

    fun ripple():Drawable?{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val stateList = arrayOf(intArrayOf(R.attr.state_pressed), intArrayOf(R.attr.state_focused), intArrayOf(R.attr.state_activated),intArrayOf(R.attr.state_window_focused))
            val color = alpha(rippleColor,if(0 >= rippleAlpha)0.1f else rippleAlpha)
            val stateColorList = intArrayOf(color, color, color,color)
            val colorStateList = ColorStateList(stateList, stateColorList)
            return RippleDrawable(colorStateList, null, rippleDrawable)
        }else if(rippleCompat){
            val sld = StateListDrawable()
            rippleDrawable?.let {
                it.paint.style = Paint.Style.FILL
                val compatAlpha = rippleAlpha-0.2f
                it.paint.color = alpha(rippleColor,if(0 >= compatAlpha)0.1f else compatAlpha)
                sld.addState(intArrayOf(R.attr.state_pressed), it)
            }?:let {
                val shapeDrawable = ShapeDrawable(OvalShape())
                shapeDrawable.paint.style = Paint.Style.FILL
                val compatAlpha = rippleAlpha-0.2f
                shapeDrawable.paint.color = alpha(rippleColor,if(0 >= compatAlpha)0.1f else compatAlpha)
                sld.addState(intArrayOf(R.attr.state_pressed),shapeDrawable)
            }
            return sld
        }else{
            return null
        }
    }

    fun setSrc(){
        val src = drawable()
        (view as ImageView).setImageDrawable(src)
    }

    fun setRipple(){
        val ripple = ripple()
        setBackgroundCompat(ripple,view)
    }

    fun setSrcWithRipple(){
        setSrc()
        setRipple()
    }
}

/**
 *
 * @param drawable 生成的背景
 * @param view 需要添加背景的View
 */
fun setBackgroundCompat(drawable: Drawable?, view: View){ //判断当前版本号，版本号大于等于16，使用setBackground；版本号小于16，使用setBackgroundDrawable。
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        view.background = drawable
    } else {
        view.setBackgroundDrawable(drawable)
    }
}



fun alpha(color:Int,alpha:Float):Int{
    var a = if(0f > alpha) 0.0f else alpha
    if(1f <= a) return color
    val alphaValue = (255 * a).toInt() shl 24
    val colorValue = 0x00FFFFFF and color
    return alphaValue or colorValue
}

interface Convert {
    fun convert(dpValue: Float): Float
}

class DpUnit(context: Context) : Convert {
    val scale:Float = context.resources.displayMetrics.density

    override fun convert(dpValue: Float) = dpValue * scale + 0.5f
}

class PxUnit : Convert {
    override fun convert(dpValue: Float) = dpValue
}



