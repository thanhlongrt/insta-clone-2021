package com.example.instagram.ui.explore.search

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.R
import com.example.instagram.Status
import com.example.instagram.databinding.FragmentSearchBinding
import com.example.instagram.extensions.getFragmentNavController
import com.example.instagram.extensions.getQueryTextChangeStateFlow
import com.example.instagram.model.UserItem
import com.example.instagram.network.entity.User
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/**
 * Created by Thanh Long Nguyen on 4/17/2021
 */

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SearchFragment : Fragment() {

    companion object{
        private const val TAG = "SearchFragment"
    }

    private var binding: FragmentSearchBinding? = null

    private val searchViewModel: SearchViewModel by viewModels()

    private lateinit var searchResultAdapter: SearchResultAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupControllers(view)

        configObservers()

    }

    private fun configObservers() {
        searchViewModel.searchUserResult.observe(viewLifecycleOwner, {
            when (it.status) {
                Status.SUCCESS -> {
                    searchResultAdapter.addAll(it.data!!)
                }
                Status.ERROR -> {

                }
                Status.LOADING -> {

                }
            }
        })
    }

    private fun setupControllers(view: View) {

        binding?.backButton?.setOnClickListener {
            getFragmentNavController(R.id.nav_host_fragment)?.navigateUp()
        }
        val searchEditText =
            binding?.searchView?.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
        searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        searchEditText.requestFocus()
        showKeyboard(searchEditText)

        searchResultAdapter = SearchResultAdapter(mutableListOf())
        searchResultAdapter.onClick = { user ->
            val bundle = bundleOf("uid" to user.uid)
            getFragmentNavController(R.id.nav_host_fragment)?.navigate(
                R.id.action_searchFragment_to_otherUserFragment,
                bundle
            )
        }


        binding?.searchResultRecyclerView?.apply {
            layoutManager = LinearLayoutManager(view.context)
            setHasFixedSize(true)
            adapter = searchResultAdapter
        }

        val recentSearch = mutableListOf(
            UserItem(username = "user1", displayName = "User 1"),
            UserItem(username = "user2", displayName = "User 2"),
            UserItem(username = "user3", displayName = "User 3"),
            UserItem(username = "user4", displayName = "User 4"),
        )
        searchResultAdapter.addAll(recentSearch)

        lifecycleScope.launch {
            binding!!.searchView.getQueryTextChangeStateFlow()
                .debounce(300)
                .filter { query ->
                    query.isNotBlank()
                }
                .distinctUntilChanged()
                .collectLatest { query ->
                    searchViewModel.searchForUser(query)
                }
        }

    }



    private fun showKeyboard(searchEditText: EditText) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}