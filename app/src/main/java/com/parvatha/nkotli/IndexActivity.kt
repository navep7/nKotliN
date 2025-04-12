package com.parvatha.nkotli

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.parvatha.nkotli.MainActivity.Companion.questsAndAns
import com.parvatha.nkotli.databinding.ActivityIndexBinding


class IndexActivity : AppCompatActivity(), MyRecyclerViewAdapter.ItemClickListener {

    private lateinit var fabX: FloatingActionButton
    private lateinit var editTextSeach: EditText
    private lateinit var fabInfo: FloatingActionButton
    private lateinit var recyclerViewIndex: RecyclerView
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityIndexBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityIndexBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        appContext = applicationContext

        findViewByIds()

        db = Firebase.firestore
        ReadData()

        initRv()
        initSearch()
        OnClickListeners()

    }

    private fun OnClickListeners() {

        fabX.setOnClickListener(OnClickListener {
            editTextSeach.setText("")
            ReadData()
        })
    }

    private fun initSearch() {
        editTextSeach.addTextChangedListener { input ->
            if (input != null) {
                if (input.length == 0)
                    ReadData()
                else if (input.contains(" ")) {
                    makeToast("onTC - " + input)
                    rvAdapter.filter.filter(input)
                }
            }
        }
    }

    private fun initRv() {

        recyclerViewIndex.layoutManager = LinearLayoutManager(this)
        rvAdapter = MyRecyclerViewAdapter(this, arrayListIndex)
        rvAdapter.setClickListener(this)
        recyclerViewIndex.adapter = rvAdapter
    }

    private fun findViewByIds() {
        fabX = findViewById(R.id.fab_x)
        recyclerViewIndex = findViewById(R.id.rv_index)
        fabInfo = findViewById(R.id.fab_info)
        editTextSeach = findViewById(R.id.edtx_search)
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun ReadData() {

        arrayListIndex.clear()

        db.collection("questions")
            .get()
            .addOnSuccessListener { result ->

                for (document in result) {
                    for (item in 0 until document.data.size) {
                        var values =
                            (document.data.getValue((item + 1).toString()) as Map<String, *>)

                        arrayListIndex.add(values["Q"].toString())
                        rvAdapter.notifyDataSetChanged()
                        questsAndAns.add(
                            hashMapOf(
                                "question" to values["Q"].toString(),
                                "answer" to values["A"].toString(),
                                "code" to values["C"].toString(),
                            )
                        )
                    }
                }


                makeToast("TotalQnAs - " + questsAndAns.size)
            }
            .addOnFailureListener { exception ->
                makeToast("Error getting documents." + exception)
            }
    }

    companion object {
        fun makeToast(s: String) {
            Log.d("makeToastin6", s)
               Toast.makeText(appContext, s, Toast.LENGTH_SHORT).show()
        }

        lateinit var rvAdapter: MyRecyclerViewAdapter
        var arrayListIndex: java.util.ArrayList<String> = ArrayList()
        lateinit var appContext: Context

        @SuppressLint("StaticFieldLeak")
        lateinit var db: FirebaseFirestore
    }

    override fun onItemClick(view: View?, position: Int) {
        makeToast(arrayListIndex.get(position))
        startActivity(
            Intent(appContext, MainActivity::class.java).putExtra(
                "topic",
                position.toString()
            )
        )
    }
}