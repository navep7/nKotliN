package com.parvatha.nkotli

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.widget.addTextChangedListener
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.parvatha.nkotli.MainActivity.Companion.questsAndAns
import com.parvatha.nkotli.databinding.ActivityIndexBinding
import java.util.HashMap


class IndexActivity : AppCompatActivity(), MyRecyclerViewAdapter.ItemClickListener {

    private val arrayListOfflineQs: ArrayList<QnA> = ArrayList()
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var textViewX: TextView
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
        sharedPrefs = this.getSharedPreferences(
            "com.parvatha.nkotli", MODE_PRIVATE
        )

        if (sharedPrefs.getBoolean("DM", false))
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        findViewByIds()

        db = Firebase.firestore
        ReadData()

        initRv()
        initSearch()
        OnClickListeners()

    }

    override fun onDestroy() {

        super.onDestroy()
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (menu is MenuBuilder) (menu as MenuBuilder).setOptionalIconsVisible(true)
        menuInflater.inflate(R.menu.simple_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_dark_mode-> {
                if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                    sharedPrefs.edit().putBoolean("DM", false).apply()
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else {
                    sharedPrefs.edit().putBoolean("DM", true).apply()
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
            }

            R.id.action_save_offline-> {
                if (arrayListOfflineQs.isEmpty()) {
                    makeToast("Saving...")
                    saveOffline(questsAndAns)
                } else makeToast("Already Dld - " + arrayListOfflineQs.size)
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveOffline(questsAndAns: java.util.ArrayList<HashMap<String, String>>) {

        makeToast("yet2Impl")

    }

    private fun OnClickListeners() {

        textViewX.setOnClickListener({
            editTextSeach.setText("")
            ReadData()
        })

        fabInfo.setOnClickListener({
            Snackbar.make(
                window.decorView,
                "Contact, for contributing.. - \n +918884846307 / xxx_xx@gmail.com",
                Snackbar.LENGTH_LONG
            ).show()
        })
    }

    private fun initSearch() {
        editTextSeach.addTextChangedListener { input ->
            if (input != null) {
                if (input.isEmpty())
                    ReadData()
                else
                    rvAdapter.filter.filter(input)
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
        textViewX = findViewById(R.id.tx_x)
        recyclerViewIndex = findViewById(R.id.rv_index)
        fabInfo = findViewById(R.id.fab_info)
        editTextSeach = findViewById(R.id.edtx_search)
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun ReadData() {

        /*var pdRead = ProgressDialog(this)
        pdRead.setMessage("Fetching Content...")
        pdRead.show()*/

        arrayListIndex.clear()
        questsAndAns.clear()

        if (arrayListOfflineQs.isEmpty())
        db.collection("questions").get().addOnSuccessListener { result ->

                for (document in result) {
                    for (item in 0 until document.data.size) {
                        var values =
                            (document.data.getValue((item + 1).toString()) as Map<String, *>)

                        if (!arrayListIndex.contains(values["Q"].toString())) {
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
                //    pdRead.dismiss()
                }

            }.addOnFailureListener { exception ->
                makeToast("Error getting documents." + exception)
            }
        else {
            makeToast("loading from Offline")
            for (i in 0 until arrayListOfflineQs.size) {
                questsAndAns.add(
                    hashMapOf(
                        "question" to arrayListOfflineQs.get(i).strQ,
                        "answer" to arrayListOfflineQs.get(i).strA,
                        "code" to arrayListOfflineQs.get(i).strC,
                    )
                )
            }
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
        startActivity(
            Intent(appContext, MainActivity::class.java).putExtra(
                "topic", position.toString()
            )
        )
    }
}