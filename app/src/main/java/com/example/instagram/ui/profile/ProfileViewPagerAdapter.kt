package com.example.instagram.ui.profile

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Created by Thanh Long Nguyen on 4/12/2021
 */
class ProfileViewPagerAdapter(
    container: Fragment,
    private val fragments: List<Fragment>
) : FragmentStateAdapter(container) {
    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }


}