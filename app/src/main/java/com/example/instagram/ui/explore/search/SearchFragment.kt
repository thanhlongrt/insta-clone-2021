package com.example.instagram.ui.explore.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.R
import com.example.instagram.Status
import com.example.instagram.databinding.FragmentSearchBinding
import com.example.instagram.getFragmentNavController
import com.example.instagram.firebase_model.User
import com.example.instagram.ui.explore.getQueryTextObservable
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

/**
 * Created by Thanh Long Nguyen on 4/17/2021
 */

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private var binding: FragmentSearchBinding? = null

    private lateinit var searchView: SearchView

    private val searchViewModel: SearchViewModel by viewModels()

    private lateinit var searchResultAdapter: SearchResultAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        searchResultAdapter = SearchResultAdapter(mutableListOf())
        searchResultAdapter.onClick = { user ->
//            searchViewModel.getUserData(user.uid)
            val bundle = bundleOf("uid" to user.uid)
            getFragmentNavController(R.id.nav_host_fragment)?.navigate(R.id.action_searchFragment_to_otherUserFragment, bundle)
        }


        binding?.searchResultRecyclerView?.apply {
            layoutManager = LinearLayoutManager(view.context)
            setHasFixedSize(true)
            adapter = searchResultAdapter
        }

        searchView = activity?.findViewById(R.id.searchView) as SearchView
        val searchEditText =
            searchView.findViewById(androidx.appcompat.R.id.search_src_text) as EditText

        val recentSearch = mutableListOf(
            User(username = "user1", display_name = "User 1"),
            User(username = "user2", display_name = "User 2"),
            User(username = "user3", display_name = "User 3"),
            User(username = "user4", display_name = "User 4"),
        )
        searchResultAdapter.addAll(recentSearch)

        searchView.getQueryTextObservable()
            .debounce(300, TimeUnit.MILLISECONDS)
            .filter { query ->
                query.length > 1
            }
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { query ->
                if (query.isNotBlank()) {
                    searchViewModel.searchUser(query)
                } else {
                    searchResultAdapter.addAll(recentSearch)
                }

            }

        searchViewModel.searchUserResult.observe(viewLifecycleOwner, {
            when (it.status) {
                Status.SUCCESS -> {
                    searchResultAdapter.addAll(it.data!!)
                }
                Status.ERROR -> {

                }
                Status.LOADING -> {

                }
                Status.IDLE -> {

                }
            }
        })


    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }


}