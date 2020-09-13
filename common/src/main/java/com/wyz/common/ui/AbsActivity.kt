package com.wyz.common.ui

import android.content.res.Configuration
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity

abstract class AbsActivity : AppCompatActivity() {

    /* System default config */
    open var config = Configuration()
    set(value){field = value}

    override fun getResources(): Resources? {
        val res = super.getResources()
        res.updateConfiguration(config, res.displayMetrics)
        return res
    }
}