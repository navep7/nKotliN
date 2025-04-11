package com.parvatha.nkotli

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.parvatha.nkotli.databinding.ActivityMainBinding
import java.util.Locale


class MainActivity : AppCompatActivity() {


    private lateinit var textToSpeech: TextToSpeech
    private var index: Int = 0
    val questsAndAns = ArrayList<HashMap<String, String>>()
    private var qCount: Int = 0
    private lateinit var fabNextQ: FloatingActionButton
    private lateinit var fabPrevQ: FloatingActionButton
    private lateinit var fabRead: FloatingActionButton
    private lateinit var txQuestion: TextView
    private lateinit var txAnswer: TextView
    private var qNas: ArrayList<QnA> = ArrayList()
    private lateinit var db: FirebaseFirestore
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

        findViewByIds()

        textToSpeech = TextToSpeech(this) {status ->
            if (status == TextToSpeech.SUCCESS){
                Log.d("TextToSpeech", "Initialization Success")
                textToSpeech.language = Locale.US
            }else{
                Log.d("TextToSpeech", "Initialization Failed")
            }
        }

        db = Firebase.firestore

        ReadData()

        Handler().postDelayed(Runnable {
            if (questsAndAns.size > 0) {
                txQuestion.text = questsAndAns[0].get("question")
                txAnswer.text = questsAndAns[0].get("answer")
            }
        }, 3000)


        fabRead.setOnClickListener(View.OnClickListener {

            if (textToSpeech.isSpeaking) {
                textToSpeech.stop()
                fabRead.setImageResource(android.R.drawable.stat_notify_call_mute)
            } else {
                fabRead.setImageResource(android.R.drawable.ic_btn_speak_now)
                val strToSpeak =
                    txQuestion.text.toString().replace("'", "") + "." + txAnswer.text.toString()
                        .replace("'", "")
                textToSpeech.speak(strToSpeak, TextToSpeech.QUEUE_FLUSH, null, "unique_id")
            }
        })

        fabNextQ.setOnClickListener(View.OnClickListener {
            if (textToSpeech.isSpeaking) {
                fabRead.setImageResource(android.R.drawable.stat_notify_call_mute)
                textToSpeech.stop()
            }
            qCount++
            if (questsAndAns.size > qCount) {
                txQuestion.text = questsAndAns[qCount].get("question")
                txAnswer.text = questsAndAns[qCount].get("answer")
            } else makeToast("All Qs loaded!")
        })

        fabPrevQ.setOnClickListener(View.OnClickListener {
            if (textToSpeech.isSpeaking) {
                fabRead.setImageResource(android.R.drawable.stat_notify_call_mute)
                textToSpeech.stop()
            }
            qCount--
            if (qCount >= 0) {
                txQuestion.text = questsAndAns[qCount].get("question")
                txAnswer.text = questsAndAns[qCount].get("answer")
            } else makeToast("Showing 1st Q!")
        })

        setSupportActionBar(binding.toolbar)
    }

    private fun ReadData() {

        db.collection("questions")
            .get()
            .addOnSuccessListener { result ->

                for (document in result) {
                    for (item in document.data) {
                        var d: List<String> = item.toString().split("?")
                        questsAndAns.add(
                            hashMapOf(
                                "question" to d[0].substring(3, d[0].length),
                                "answer" to Html.fromHtml(d[1].substring(1, d[1].length - 1)).toString(),
                            )
                        )
                    }
                }

                val pi = 3.14 // Declaring a read-only variable
// pi = 3.1415 // Error: Val cannot be reassigned

                var count = 0 // Declaring a mutable variable
                count = 1 // Reassigning the value

                makeToast("TotalQnAs - " + questsAndAns.size)
            }
            .addOnFailureListener { exception ->
                makeToast("Error getting documents." + exception)
            }
    }

    fun AddData(item: HashMap<String, String>) {



            db.collection("questions")
                .add(item)
                .addOnSuccessListener { documentReference ->
                        makeToast("Added!")
                }
                .addOnFailureListener { e ->
                    makeToast("Error adding document")
                }


    }

    private fun Questions() {

        var questionn = hashMapOf(
            "question" to "1. What is Kotlin?",
            "answer" to "Kotlin is a modern programming language developed by JetBrains. It is statically typed and runs on the Java Virtual Machine (JVM). The Kotlin language is designed to be interoperable with Java, so you can use Kotlin code alongside Java code seamlessly. Aside from Android app development, it is also used for server-side and web development.",
        )
        questsAndAns.add(questionn)


        questionn = hashMapOf(
            "question" to "2. How is Kotlin different from Java?",
            "answer" to "Kotlin and Java both programming languages that run on the Java Virtual Machine (JVM). but Kotlin is designed to be more concise and expressive, reducing unnecessary code. One notable feature is its built-in null safety, helping to avoid common programming errors related to null values. Kotlin also introduces modern language features like extension functions and coroutines, offering developers more flexibility and improved productivity compared to Java.",
        )
        questsAndAns.add(questionn)


        questionn = hashMapOf(
            "question" to "3. Explain the advantages of using Kotlin.",
            "answer" to "Some advantages of using Kotlin include:\n" +
                    "\n" +
                    "Concise Syntax: Kotlin’s syntax is more concise, reducing boilerplate code and making it easier to read and write.\n" +
                    "Null Safety: Kotlin’s null safety features help prevent null pointer exceptions, enhancing code reliability.\n" +
                    "Interoperability: Kotlin is fully interoperable with Java, allowing you to leverage existing Java libraries and frameworks in Kotlin projects.\n" +
                    "Coroutines: Kotlin’s built-in support for coroutines simplifies asynchronous programming and improves performance.\n" +
                    "Functional Programming: Kotlin supports functional programming constructs like higher-order functions and lambda expressions, making code more expressive and concise.\n" +
                    "Tooling and Community Support: Kotlin has excellent tooling support, including IDE plugins for popular development environments. It also has a growing and active community, with abundant learning resources and libraries available.\n",
        )
        questsAndAns.add(questionn)


        questionn = hashMapOf(
            "question" to "4. What are the basic data types in Kotlin?",
            "answer" to "The basic data types in Kotlin are:\n" +
                    "\n" +
                    "Numbers: This includes types like Int (for integers), Double (for double-precision floating-point numbers), Float (for single-precision floating-point numbers), Long (for long integers), Short (for short integers), and Byte (for bytes).\n" +
                    "Booleans: The Boolean type represents logical values, either true or false.\n" +
                    "Characters: The Char type represents a single character.\n" +
                    "Strings: The String type represents a sequence of characters.\n",
        )
        questsAndAns.add(questionn)


        questionn = hashMapOf(
            "question" to "5. What is the difference between val and var in Kotlin?",
            "answer" to "In Kotlin, `val` and `var` are used to declare variables, but they have different characteristics:\n" +
                    "\n" +
                    "`val` is used to declare read-only (immutable) variables. Once assigned, the value of a `val` cannot be changed.\n" +
                    "`var` is used to declare mutable variables. The value of a `var` can be reassigned multiple times.\n",
        )
        questsAndAns.add(questionn)

        questionn = hashMapOf(
            "question" to "6. Explain type inference in Kotlin.",
            "answer" to "Type inference in Kotlin allows the compiler to automatically determine the type of a variable based on its initialization value. Each time you use a variable, you don’t have to specify its type explicitly.\n" +
                    "\n" +
                    "val name = \"John\" // Type inference infers that 'name' is of type String\n" +
                    "val age = 25 // Type inference infers that 'age' is of type Int\n" +
                    "val balance = 1000.0 // Type inference infers that 'balance' is of type Double\n" +
                    "val isActive = true // Type inference infers that 'isActive' is of type Boolean\n\n" +
                    "In the above examples, Kotlin infers the types of variables based on the values assigned to them. This reduces the verbosity of code and improves readability. However, it’s important to note that once the type is inferred, it becomes fixed and cannot be changed. If you need a different type, you’ll have to explicitly declare it.",
        )
        questsAndAns.add(questionn)

        questionn = hashMapOf(
            "question" to "7. What are nullable types in Kotlin?",
            "answer" to "In Kotlin, nullable types allow variables to hold null values in addition to their regular data type values. This is in contrast to non-nullable types, which cannot hold null values by default. By using nullable types, the compiler enforces null safety and reduces the occurrence of null pointer exceptions.\n" +
                    "\n" +
                    "To declare a nullable type, you append a question mark (?) to the data type.\n" +
                    "\n" +
                    "val name: String? = null // Nullable String type\n" +
                    "val age: Int? = 25 // Nullable Int type\n"
        )
        questsAndAns.add(questionn)

        questionn = hashMapOf(
            "question" to "8. How do you handle nullability in Kotlin?",
            "answer" to "In Kotlin, you can handle nullability using several techniques:\n" +
                    "\n" +
                    "Safe Calls: Use the safe call operator (?.) to safely access properties or call methods on a nullable object. If the object is null, the expression evaluates to null instead of throwing a null pointer exception.\n" +
                    "val length: Int? = text?.length\n" +
                    "Elvis Operator: The Elvis operator (?:) allows you to provide a default value when accessing a nullable object. If the object is null, the expression after the Elvis operator is returned instead.\n" +
                    "val length: Int = text?.length ?: 0\n" +
                    "Safe Casts: Use the safe cast operator (as?) to perform type casts on nullable objects. If the cast is unsuccessful, the result is null.\n" +
                    "val name: String? = value as? String\n" +
                    "Non-Null Assertion: When you are certain that a nullable variable is not null at a specific point, you can use the non-null assertion operator (!!) to bypass null safety checks. However, if the variable is actually null, a null pointer exception will occur.\n" +
                    "val length: Int = text!!.length\n"
        )
        questsAndAns.add(questionn)
     //   AddData(questionn)

        questionn = hashMapOf(
            "question" to "9. What is the Elvis operator in Kotlin?",
            "answer" to "The Elvis operator (?:) is a shorthand notation in Kotlin that provides a default value when accessing a nullable object. It is useful in scenarios where you want to assign a default value if a nullable object is null.\n" +
                    "\n" +
                    "Syntax:\n" +
                    "\n" +
                    "nullableObject ?: defaultValue\n" +
                    "If nullableObject is not null, the expression evaluates to nullableObject. If nullableObject is null, the expression evaluates to defaultValue.\n" +
                    "\n" +
                    "Example:\n" +
                    "\n" +
                    "val name: String? = null\n" +
                    "val length: Int = name?.length ?: 0 // If name is null, assign 0 as the length\n" +
                    "In the example, if name is null, the length variable is assigned the value of 0 as a default value. Otherwise, it assigns the length of name.\n" +
                    "\n"
        )
        questsAndAns.add(questionn)

        questionn = hashMapOf(
            "question" to "10. Explain the concept of smart casts in Kotlin.",
            "answer" to "Smart casts in Kotlin allow the compiler to automatically cast a variable to a non-nullable type after a null check. As a result, type casting is no longer necessary, and code readability and safety are enhanced.\n" +
                    "\n" +
                    "When a variable is checked for null using an if or when statement, the compiler can automatically cast the variable to a non-nullable type within the corresponding block.\n" +
                    "\n" +
                    "fun printLength(text: String?) {\n" +
                    "    if (text != null) {\n" +
                    "        println(\"Length: \${text.length}\") // Automatic smart cast to non-nullable type\n" +
                    "    } else {\n" +
                    "        println(\"Text is null\")\n" +
                    "    }\n" +
                    "}\n" +
                    "In the above example, the text variable is initially nullable. After the null check, the compiler understands that within the if block, the text variable is guaranteed to be non-null, so it can be used without any further null checks."
        )
        questsAndAns.add(questionn)


        /*questionn = hashMapOf(
            "question" to "",
            "answer" to ""
        )
        questsAndAns.add(questionn)*/







    }

    private fun findViewByIds() {
        fabNextQ = findViewById(R.id.fab_next)
        fabPrevQ = findViewById(R.id.fab_prev)
        fabRead = findViewById(R.id.fab_read)
        relativeLayoutMain = findViewById(R.id.rl_main)
        txQuestion = findViewById(R.id.tx_q)
        txQuestion.setMovementMethod(ScrollingMovementMethod())
        txAnswer = findViewById(R.id.tx_a)
        txAnswer.setMovementMethod(ScrollingMovementMethod())
    }


    private fun makeToast(s: String) {
        Toast.makeText(applicationContext, s, Toast.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

}