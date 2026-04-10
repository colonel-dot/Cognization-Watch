package com.example.common.provider

import androidx.fragment.app.Fragment

interface FragmentProvider {
    // TODO
    fun createFragment(className: String): Fragment
}