package com.parvatha.nkotli

import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.common.AccountPicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.parvatha.nkotli.IndexActivity.Companion.db
import com.parvatha.nkotli.IndexActivity.Companion.makeToast
import com.parvatha.nkotli.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {


    private lateinit var recyclerViewComments: RecyclerView
    private lateinit var rvCommentsAdapter: RvCommentsAdapter
    private val listComments: ArrayList<Comment> = ArrayList()
    private lateinit var possibleEmail: String
    private val REQUEST_CODE_EMAIL: Int = 1
    private lateinit var buttonPost: ImageButton
    private var dataIndex: Int = 0
    private lateinit var txToolBar: TextView
    private lateinit var textToSpeech: TextToSpeech
    private var index: Int = 0
    private var qCount: Int = 0
    private lateinit var fabNextQ: FloatingActionButton
    private lateinit var fabPrevQ: FloatingActionButton
    private lateinit var fabRead: FloatingActionButton

    //   private lateinit var txQuestion: TextView
    private lateinit var txAnswer: TextView
    private lateinit var txCode: TextView
    private lateinit var txCodeSample: TextView
    private lateinit var txCommentSample: TextView
    private var qNas: ArrayList<QnA> = ArrayList()
    private lateinit var relativeLayoutMain: RelativeLayout
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var arrayListTopics: List<String> = ArrayList()
    var addIndex: Int = 0

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        txToolBar = findViewById(binding.txToolbar.id)
        buttonPost = findViewById(binding.actionImgbtnEdit.id)
        initStuff()
        findViewByIds()
        OnClickListeners()
        rvInit()

        if (questsAndAns.size >= dataIndex) {
            txToolBar.text =
                (Html.fromHtml(questsAndAns[dataIndex].get("question"))).toString()
//            txQuestion.text = Html.fromHtml(questsAndAns[dataIndex].get("question"))
            txAnswer.text = Html.fromHtml(questsAndAns[dataIndex].get("answer"))
            if (questsAndAns[dataIndex].get("code")?.length!! > 5) {
                txCode.visibility = View.VISIBLE
                txCodeSample.visibility = View.VISIBLE
                txCode.text = Html.fromHtml(questsAndAns[dataIndex].get("code"))
            } else {
                txCode.visibility = View.INVISIBLE
                txCodeSample.visibility = View.INVISIBLE
            }
            if (questsAndAns[dataIndex].get("comments") != null)
                if (questsAndAns[dataIndex].get("comments")?.length!! > 5) {
                    var commentsReceived = questsAndAns[dataIndex].get("comments")!!.split("~~~")
                    for (i in 0 until commentsReceived.size) {
                        var cSplit =
                            commentsReceived[i].split("|||")
                        listComments.add(Comment(cSplit.get(1), cSplit.get(0), cSplit.get(2)))
                    }

                    rvCommentsAdapter.notifyDataSetChanged()
                    recyclerViewComments.visibility = View.VISIBLE
                    txCommentSample.visibility = View.VISIBLE
                } else {
                    txCommentSample.visibility = View.INVISIBLE
                    recyclerViewComments.visibility = View.INVISIBLE
                }
        }

        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d("TextToSpeech", "Initialization Success")
                textToSpeech.language = Locale.US
            } else {
                Log.d("TextToSpeech", "Initialization Failed")
            }
        }

        IndexActivity.db = Firebase.firestore

        setSupportActionBar(binding.toolbar)
    }

    private fun rvInit() {

        recyclerViewComments = findViewById<RecyclerView>(R.id.rv_comments)
        recyclerViewComments.layoutManager = LinearLayoutManager(this)
        rvCommentsAdapter = RvCommentsAdapter(applicationContext, listComments)
        //  adapter.setClickListener(MainActivity.this)
        recyclerViewComments.adapter = rvCommentsAdapter
    }


    private fun initStuff() {
        val data = intent.extras!!.getString("topic", "defaultKey")
        dataIndex = data.toInt()
        qCount = dataIndex
    }

    private fun OnClickListeners() {


        fabRead.setOnClickListener(View.OnClickListener {

            if (textToSpeech.isSpeaking) {
                textToSpeech.stop()
                fabRead.setImageResource(android.R.drawable.stat_notify_call_mute)
            } else {
                fabRead.setImageResource(android.R.drawable.ic_btn_speak_now)
                val strToSpeak =
                    txToolBar.text.toString().replace("'", "") + "." + txAnswer.text.toString()
                        .replace("`", "")
                textToSpeech.speak(strToSpeak, TextToSpeech.QUEUE_FLUSH, null, "unique_id")
            }
        })

        fabNextQ.setOnClickListener(View.OnClickListener {
            listComments.clear()
            if (textToSpeech.isSpeaking) {
                fabRead.setImageResource(android.R.drawable.stat_notify_call_mute)
                textToSpeech.stop()
            }
            qCount++
            if (questsAndAns.size > qCount) {
                fabPrevQ.isEnabled = true
                txToolBar.text = Html.fromHtml(questsAndAns[qCount].get("question"))
                txAnswer.text = Html.fromHtml(questsAndAns[qCount].get("answer"))
                if (questsAndAns[qCount].get("code")?.length!! > 5) {
                    txCode.visibility = View.VISIBLE
                    txCodeSample.visibility = View.VISIBLE
                    txCode.text = Html.fromHtml(questsAndAns[qCount].get("code"))
                } else {
                    txCodeSample.visibility = View.INVISIBLE
                    txCode.visibility = View.INVISIBLE
                }
                if (questsAndAns[qCount].get("comments") != null)
                    if (questsAndAns[qCount].get("comments")?.length!! > 5) {
                        var commentsReceived = questsAndAns[qCount].get("comments")!!.split("~~~")
                        for (i in 0 until commentsReceived.size) {
                            var cSplit =
                                commentsReceived[i].split("|||")
                            listComments.add(Comment(cSplit.get(1), cSplit.get(0), cSplit.get(2)))
                        }

                        rvCommentsAdapter.notifyDataSetChanged()
                        recyclerViewComments.visibility = View.VISIBLE
                        txCommentSample.visibility = View.VISIBLE
                    } else {
                        txCommentSample.visibility = View.INVISIBLE
                        recyclerViewComments.visibility = View.INVISIBLE
                    }
            } else {
                makeToast("All Qs loaded!")
                fabNextQ.isEnabled = false
            }
        })

        fabPrevQ.setOnClickListener(View.OnClickListener {
            listComments.clear()
            if (textToSpeech.isSpeaking) {
                fabRead.setImageResource(android.R.drawable.stat_notify_call_mute)
                textToSpeech.stop()
            }

            if (qCount > 0) {
                fabNextQ.isEnabled = true
                qCount--
                txToolBar.text = Html.fromHtml(questsAndAns[qCount].get("question"))
                txAnswer.text = Html.fromHtml(questsAndAns[qCount].get("answer"))
                if (questsAndAns[qCount].get("code")?.length!! > 5) {
                    txCode.visibility = View.VISIBLE
                    txCodeSample.visibility = View.VISIBLE
                    txCode.text = Html.fromHtml(questsAndAns[qCount].get("code"))
                } else {
                    txCodeSample.visibility = View.INVISIBLE
                    txCode.visibility = View.INVISIBLE
                }
                if (questsAndAns[qCount].get("comments") != null)
                    if (questsAndAns[qCount].get("comments")?.length!! > 5) {
                        var commentsReceived = questsAndAns[qCount].get("comments")!!.split("~~~")
                        for (i in 0 until commentsReceived.size) {
                            var cSplit =
                                commentsReceived[i].split("|||")
                            listComments.add(Comment(cSplit.get(1), cSplit.get(0), cSplit.get(2)))
                        }

                        rvCommentsAdapter.notifyDataSetChanged()
                        recyclerViewComments.visibility = View.VISIBLE
                        txCommentSample.visibility = View.VISIBLE
                    } else {
                        txCommentSample.visibility = View.INVISIBLE
                        recyclerViewComments.visibility = View.INVISIBLE
                    }
            } else {
                fabPrevQ.isEnabled = false
                makeToast("Showing 1st Q!")
            }
        })

        buttonPost.setOnClickListener {

            try {
                val intent = AccountPicker.newChooseAccountIntent(
                    null, null,
                    arrayOf(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE), false, null, null, null, null
                )
                startActivityForResult(intent, REQUEST_CODE_EMAIL)
            } catch (e: ActivityNotFoundException) {
                // TODO
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_EMAIL && resultCode == RESULT_OK) {
            val accountName = data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            possibleEmail = accountName.toString()

            val alertDialog = AlertDialog.Builder(this)
                .setTitle(possibleEmail)
                .setMessage("Post your Comment")

            val editTextComment = EditText(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
            }
            editTextComment.requestFocus()
            editTextComment.postDelayed(Runnable {
                val inputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(editTextComment, InputMethodManager.SHOW_IMPLICIT)
            }, 100)

            alertDialog.setView(editTextComment)
            alertDialog.setIcon(R.drawable.nk_icon)

            alertDialog.setPositiveButton("Post") { dialog, which ->


                var comment: Comment = Comment(
                    editTextComment.text.toString(),
                    possibleEmail,
                    getCurrentTimeStamp().toString()
                )
                listComments.add(comment)

                var sendMsg = ""
                for (i in 0 until listComments.size) {
                    if (sendMsg == "")
                        sendMsg =
                            listComments.get(i).strCommentUser + " ||| " + listComments.get(i).strComment + " ||| " + listComments.get(
                                i
                            ).strCommentTime
                    else sendMsg =
                        sendMsg + "~~~" + listComments.get(i).strCommentUser + " ||| " + listComments.get(
                            i
                        ).strComment + " ||| " + listComments.get(i).strCommentTime
                }

                db.collection("questions").get().addOnSuccessListener { result ->
                    result.documents[0].reference.update(
                        "${qCount + 1}.CC",
                        sendMsg
                    )
                        .addOnSuccessListener {
                            makeToast("DocumentSnapshot successfully updated!")
                            IndexActivity.readAgain = true
                            IndexActivity.ReadData()
                            recyclerViewComments.visibility = View.VISIBLE
                            txCommentSample.visibility = View.VISIBLE
                            //    listComments.add(comment)
                            rvCommentsAdapter.notifyDataSetChanged()

                        }
                        .addOnFailureListener { e -> makeToast("Error updating document" + e.message) }
                }
            }

            alertDialog.setNegativeButton("Cancel") { dialog, which ->
                makeToast("Cancel")
                dialog.cancel()
            }

            alertDialog.show()

        }
    }

    fun getCurrentTimeStamp(): String? {
        try {
            val dateFormat: SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
            val currentDateTime: String = dateFormat.format(Date()) // Find todays date

            return currentDateTime
        } catch (e: Exception) {
            e.printStackTrace()

            return null
        }
    }

    override fun onPause() {
        textToSpeech.stop()
        super.onPause()
    }


    private fun findViewByIds() {
        txCodeSample = findViewById(R.id.tx_codesample)
        txCommentSample = findViewById(R.id.tx_commentsample)
        fabNextQ = findViewById(R.id.fab_next)
        fabPrevQ = findViewById(R.id.fab_prev)
        fabRead = findViewById(R.id.fab_read)
        relativeLayoutMain = findViewById(R.id.rl_main)
        txAnswer = findViewById(R.id.tx_a)
        txAnswer.setMovementMethod(ScrollingMovementMethod())
        txCode = findViewById(R.id.tx_c)
        txCode.setMovementMethod(ScrollingMovementMethod())
    }


    companion object {
        val questsAndAns = ArrayList<HashMap<String, String>>()
    }

}