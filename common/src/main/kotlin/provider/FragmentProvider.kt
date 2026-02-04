package provider

import androidx.fragment.app.Fragment

interface FragmentProvider {
    fun createFragment(className: String): Fragment
}