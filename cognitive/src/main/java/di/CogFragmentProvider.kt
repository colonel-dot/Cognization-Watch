package di

import androidx.fragment.app.Fragment
import com.example.cognitive.main.home.HomeFragment
import com.example.common.provider.FragmentProvider


class CogFragmentProvider: FragmentProvider {

    override fun createFragment(className: String): Fragment {
        return HomeFragment.newInstance("", "")
    }

}