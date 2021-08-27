package com.anderson.restaapp.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.anderson.restaapp.adapter.FoodAdapter
import com.anderson.restaapp.model.ItemFood
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.json.JSONObject
import kotlin.math.log

class HomeRepository {

    val database = Firebase.database.reference
    private var foodLiveData = MutableLiveData<ItemFood>()
    private var statusFoodLiveData = MutableLiveData<String>()
    private var keysFood = ArrayList<String>()

    fun foodLiveDataObserver(): MutableLiveData<ItemFood>{
        return foodLiveData
    }
    fun statusFoodLiveDataObserver(): MutableLiveData<String>{
        return statusFoodLiveData
    }
    fun keysFoodSize(): Int {
        return keysFood.size
    }

    fun processListFood(){
        val query = database.child("Foods")
        query.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val food = snapshot.getValue(ItemFood::class.java)
                val key = snapshot.key
                keysFood.add(key!!)
                Log.d("key",keysFood.size.toString())
                foodLiveData.value = food!!
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val item = snapshot.getValue(ItemFood::class.java)
                val key = snapshot.key
                val index = keysFood.indexOf(key)
                if (index>-1 && item!=null) {
                    val message =
                        "change\t$index\t${item.name}\t${item.price}\t${item.url}\t${item.description}\t${item.discount}"
                    statusFoodLiveData.value = message
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val key = snapshot.key
                val index = keysFood.indexOf(key)
                if (index>-1) {
                    val message = "remove\t$index"
                    statusFoodLiveData.value = message
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}