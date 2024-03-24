package com.example.liste

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar
import com.example.liste.ItemAdapter
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class MainActivity : AppCompatActivity(), ViewHolder.OnItemClickListener, ViewHolder.ViewHolderCallback {

    private lateinit var adapter: ItemAdapter
    private var itemList = ArrayList<DataItem>()
    private var imageList = arrayListOf(
        R.drawable.baseline_alarm_24, R.drawable.baseline_alarm_24, R.drawable.baseline_alarm_24) //ici on ajoute des alarmes

    private val CHANNEL_ID = "channel_id"
    private val REQUEST_NOTIFICATION_PERMISSION = 123
    private var notificationId = 1 // Define a unique ID for each notification

/**************************************************
 * Construction de l'application - première étape du cycle de vie de l'application
 * Attribution des items à la liste pour la remplir
 * *************************************************/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createGridList()
        touch()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATION_PERMISSION
            )
        }
        createNotificationChannel()
    }

    private fun touch() {
        val add = findViewById<ImageButton>(R.id.add)
        val appStop = findViewById<ImageButton>(R.id.appStop)

        appStop.setOnClickListener {
            finish()
        }
        add.setOnClickListener {
            addItem()
        }
    }

    private fun createGridList(){
        // Instancier la liste et adapter les items
        val dynamicListe = findViewById<RecyclerView>(R.id.gridList)
        var myList = createList()
        this.adapter = ItemAdapter(myList, this, this)
        dynamicListe.adapter = adapter
        dynamicListe.setHasFixedSize(true)

        // Choisir lesens de défilement de la liste
        /**val DirectionLayout = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        dynamicListe.setLayoutManager(DirectionLayout)*/


        // Choisir le sens de défilement et la forme de la liste
        val DirectionLayout = GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false)
        dynamicListe.setLayoutManager(DirectionLayout)

        // Permettre à l'utilisateur d'agencer les items dans la liste
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT or ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val initialPosition = viewHolder.adapterPosition
                val finalPosition = target.adapterPosition
                val change = itemList[initialPosition]
                itemList[initialPosition] = itemList[finalPosition]
                itemList[finalPosition] = change
                adapter.notifyItemMoved(finalPosition, initialPosition)
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }
        })
        itemTouchHelper.attachToRecyclerView(dynamicListe)
    }

    fun createList(): ArrayList<DataItem> {
        for (i in 0 until this.imageList.size){
            val newItem = DataItem(imageList[i], "Item $i", R.drawable.garbage_2516958)
            this.itemList += newItem
        }

        return this.itemList
    }

    private fun addItem() {
        val newImage = R.drawable.baseline_alarm_24
        imageList.add(newImage)
        val newItem = DataItem(newImage, "Item ${itemList.size}", R.drawable.garbage_2516958)
        itemList.add(newItem)
        adapter.notifyItemInserted(itemList.size - 1)
    }

    override fun onItemClick(position: Int) {
        val itemImage: ImageView = findViewById(R.id.itemImage)
        itemImage.setImageResource(itemList[position].imageItem)
    }

    override fun onItemDelete(position: Int) {
        this.itemList.removeAt(position)
        this.adapter.notifyItemRemoved(position)
    }


    override fun showNotification() {
        val editText = findViewById<EditText>(R.id.nom_alarme)
        val valeurEditText = editText.text.toString()

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_1370574)
            .setContentTitle(valeurEditText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Vibration
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 1000, 1000) // Vibration pattern: wait 0ms, vibrate for 1000ms, wait 1000ms, vibrate for 1000ms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            vibrator.vibrate(pattern, -1)
        }

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission()
            }
            notify(notificationId, builder.build())
            notificationId =  notificationId+1
        }
    }
    private fun requestNotificationPermission() {
        // Demander la permission
        requestPermissions(
            arrayOf("android.permission.NOTIFICATION_POLICY_ACCESS_SETTINGS"), 123)
    }

    fun createNotificationChannel() {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, "Reminder", importance).apply {
            description = "Alerte"}

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

/**************************************************
 * Définition des données dans les items
 * *************************************************/

data class DataItem( var imageItem: Int, val nameItem: String, val deleteItem: Int)

/**************************************************
 * Instanciation des composants des items
 * *************************************************/

class ViewHolder(view: View, listener: OnItemClickListener, private val callback: ViewHolderCallback) : RecyclerView.ViewHolder(view), View.OnClickListener { // Instancier les variables pour chaque objets composants
    val btnitem: ImageButton = view.findViewById(R.id.btn_alarme)
    val nameItem: EditText = view.findViewById(R.id.nom_alarme)
    val timePickerItem: TimePicker = view.findViewById(R.id.timePicker)
    val deleteItem: ImageButton = view.findViewById(R.id.deleteItem)
    val Alarme: ImageButton = view.findViewById(R.id.btn_alarme)
    val listener = listener

    init {
        view.setOnClickListener(this)
        deleteItem.setOnClickListener {
            listener.onItemDelete(adapterPosition)
        }
        Alarme.setOnClickListener {

            val currentTime = Calendar.getInstance()
            val selectedTime = Calendar.getInstance()

            val hourOfDay = timePickerItem.hour
            val minute = timePickerItem.minute

            // Définir l'heure sélectionnée dans le calendrier
            selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
            selectedTime.set(Calendar.MINUTE, minute)
            selectedTime.set(Calendar.SECOND, 0)

            // Calculer la différence en millisecondes
            val differenceInMillis = selectedTime.timeInMillis - currentTime.timeInMillis

            Handler().postDelayed(
                {
                    // Actions à effectuer après le délai
                    callback.showNotification()
                    listener.onItemDelete(adapterPosition)
                }, differenceInMillis
            )
        }
    }

    interface ViewHolderCallback {
        fun showNotification()
    }

    override fun onClick(view: View?) {
        val position: Int = adapterPosition
        if(position != RecyclerView.NO_POSITION) {
            listener.onItemClick(position)
        }
    }

    interface OnItemClickListener{
        fun onItemClick(position: Int)
        fun onItemDelete(position: Int)
    }

}

/**************************************************
 * Attribution des valeurs(données) à chaque composants des items
 * *************************************************/

class ItemAdapter(itemList: ArrayList<DataItem>, listener: ViewHolder.OnItemClickListener, private val callback: ViewHolder.ViewHolderCallback) : RecyclerView.Adapter<ViewHolder>() { // Adapter les items dans la liste: Formes et Données
    private val listener = listener
    private val itemList = itemList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder { // Définir le visuel(View) de l'item et instancier les variables pour chaque objets composants
        val adapterLayout = LayoutInflater.from(parent.context).inflate(R.layout.item_1, parent, false)
        return ViewHolder(adapterLayout, listener, callback)
    }

    override fun getItemCount(): Int { // Définir le nombre d'items dans la liste
        return itemList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) { // Attribuer les données aux composants
        holder.btnitem.setImageResource(itemList[position].imageItem)
        holder.nameItem.text = Editable.Factory.getInstance().newEditable(itemList[position].nameItem)
        holder.deleteItem.setImageResource(itemList[position].deleteItem)
    }

}