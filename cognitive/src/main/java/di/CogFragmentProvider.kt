package di

import androidx.fragment.app.Fragment
import com.example.cognitive.main.HomeFragment
import provider.FragmentProvider


class CogFragmentProvider: FragmentProvider {

    override fun createFragment(className: String): Fragment {
        return HomeFragment.newInstance()
    }

}