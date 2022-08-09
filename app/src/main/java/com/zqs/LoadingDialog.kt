package com.zqs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.zqs.app.R

/*
 *   @author zhangqisheng
 *   @date 2022-08-06 21:22
 *   @description 
 */
class LoadingDialog:DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return LayoutInflater.from(context).inflate(R.layout.dialog_loading, container, false)
    }

    fun setText(text:String){
        view?.findViewById<TextView>(R.id.title)?.text=text
    }


}