package com.anderson.restaapp.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anderson.restaapp.R
import com.anderson.restaapp.activity.HomeActivity
import com.anderson.restaapp.adapter.FoodAdapter
import com.anderson.restaapp.databinding.FragmentSelectDinkBinding
import com.anderson.restaapp.databinding.FragmentSelectFoodBinding
import com.anderson.restaapp.listener.ClickItemFood
import com.anderson.restaapp.model.ItemFood
import com.anderson.restaapp.viewmodel.HomeViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DatabaseReference

class SelectDinkFragment : Fragment(), ClickItemFood {

    private var _binding: FragmentSelectDinkBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var foodAdapter: FoodAdapter
    private lateinit var layoutManager: GridLayoutManager
    private var listDrink = ArrayList<ItemFood>()
    private var filterDrink = ArrayList<ItemFood>()
    private var tempList = ArrayList<ItemFood>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        homeViewModel = HomeActivity.homeViewModel
        _binding = FragmentSelectDinkBinding.inflate(inflater,container,false)
        val view = binding.root

        makeObserver()

        listDrink = homeViewModel.getListDrink()
        foodAdapter = FoodAdapter(listDrink)
        setupRecyclerview()
        foodAdapter.setOnCallbackListener(this)

        if (listDrink.size == 0) homeViewModel.fetchListDrink()
        else {
            binding.shimmerDrink.apply {
                stopShimmerAnimation()
                visibility = View.GONE
            }
            binding.rvDrink.visibility = View.VISIBLE
        }

        filterDrink.clear()
        filterDrink.addAll(listDrink)

        performSearch()
        setupHideBotNav()

        setTitleToolbar("Home")

        return view
    }

    private fun setupHideBotNav() {
        val botNav = (activity as HomeActivity).findViewById<BottomNavigationView>(R.id.bot_nav)
        binding.rvDrink.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy>0 && botNav.isShown) botNav.visibility = View.GONE
                else if (dy<0) botNav.visibility = View.VISIBLE
            }
        })
    }

    private fun performSearch() {
        binding.searchDrink.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                search(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                search(newText)
                return false
            }

        })
    }

    private fun search(text: String?) {
        text?.let {
            tempList.clear()
            listDrink.clear()
            listDrink.addAll(filterDrink)
            listDrink.forEach { food ->
                if (food.name.contains(text,true)) tempList.add(food)
            }
            updateRecyclerView()
        }
    }

    private fun updateRecyclerView() {
        listDrink.clear()
        listDrink.addAll(tempList)
        foodAdapter.notifyDataSetChanged()
        (binding.rvDrink.layoutManager as GridLayoutManager).scrollToPosition(0)
    }

    private fun setupRecyclerview() {
        layoutManager = GridLayoutManager(context,2, LinearLayoutManager.VERTICAL,false)
        binding.rvDrink.apply {
            setHasFixedSize(true)
            layoutManager = this@SelectDinkFragment.layoutManager
            adapter = foodAdapter
        }
    }

    private fun makeObserver() {
        homeViewModel.getDrinkLiveDataObserver().observe(viewLifecycleOwner,{
            if (it!=null && filterDrink.size<homeViewModel.getKeysDrinkSize()){
                listDrink.add(it)
                filterDrink.add(it)
                if (filterDrink.size == 1) {
                    binding.shimmerDrink.apply {
                        stopShimmerAnimation()
                        visibility = View.GONE
                    }
                    binding.rvDrink.visibility = View.VISIBLE
                    foodAdapter.notifyDataSetChanged()
                }
                else foodAdapter.notifyItemInserted(listDrink.size)
            }
        })

        homeViewModel.getStatusDrinkLiveDataObserver().observe(viewLifecycleOwner,{
            if (it!=null){
                val message = it.split('\t')
                val status = message[0]
                val pos = message[1].toInt()
                when (status) {
                    "change" -> {
                        val item = ItemFood(message[2],message[3].toDouble(),message[4],message[5],message[6].toDouble())
                        listDrink[pos] = item
                        filterDrink[pos] = item
                        foodAdapter.notifyItemChanged(pos)
                    }
                    "remove" -> {
                        listDrink.removeAt(pos)
                        filterDrink.removeAt(pos)
                        foodAdapter.notifyItemRemoved(pos)
                    }
                }
            }
        })
    }

    override fun onClickItemFood(data: ItemFood) {
        val action = SelectDinkFragmentDirections.actionSelectDinkFragmentToDetailFoodFragment(data)
        findNavController().navigate(action)
    }

    override fun onPause() {
        listDrink.clear()
        listDrink.addAll(filterDrink)
        super.onPause()
    }

    fun setTitleToolbar(title: String) {
        val titleToolBar = activity?.findViewById<TextView>(R.id.titleToolbar)
        (activity as HomeActivity).supportActionBar?.title = ""
        titleToolBar?.text = title
    }

}