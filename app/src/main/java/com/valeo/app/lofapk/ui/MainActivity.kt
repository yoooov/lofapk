package com.valeo.app.lofapk.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

import com.symbol.emdk.EMDKManager
import com.symbol.emdk.EMDKManager.EMDKListener
import com.symbol.emdk.EMDKResults
import com.symbol.emdk.barcode.*
import com.symbol.emdk.barcode.Scanner.DataListener
import com.symbol.emdk.barcode.Scanner.StatusListener
import com.valeo.app.lofapk.R
import com.valeo.app.lofapk.model.LoginResponse
import com.valeo.app.lofapk.model.OFinfo
import com.valeo.app.lofapk.model.ScanningLocation
import com.valeo.app.lofapk.utils.ApiConstant.Companion.CLIENT_ID
import com.valeo.app.lofapk.utils.ApiConstant.Companion.CLIENT_SECRET
import com.valeo.app.lofapk.utils.ApiConstant.Companion.HANDLER_FAB
import com.valeo.app.lofapk.utils.ApiConstant.Companion.MAIN_TITLE
import com.valeo.app.lofapk.utils.ApiConstant.Companion.SEPA
import com.valeo.app.lofapk.utils.ApiConstant.Companion.SPACER
import com.valeo.app.lofapk.utils.api.ApiClient
import com.valeo.app.lofapk.utils.api.AuthSessionManager
import com.valeo.app.lofapk.utils.api.OrdsApiClient
import com.valeo.app.lofapk.utils.hideFab
import com.valeo.app.lofapk.utils.hideTip
import com.valeo.app.lofapk.utils.scanner.ProductAdapter
import com.valeo.app.lofapk.utils.showFab
import com.valeo.app.lofapk.utils.showTip
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), EMDKListener, StatusListener, DataListener {

    /*companion object {
        fun newInstance() = MainActivity()
    }*/

    private var dataLength = 0

    private var dataOF = ""
    private var dataLOC = ""

    private var canPost = false

    private var isOpen: Boolean = false
    private var isRunning: Boolean = false

    private var locationInfo: OFinfo = OFinfo(uniqueId = "", location = "")
    private var locationDataSet: MutableList<String> = mutableListOf()

    private var ofIsScanned = 0

    private var unixTime: Long = 0

    // Declare a variable to store EMDKManager object
    var emdkManager: EMDKManager? = null

    // Declare a variable to store Barcode Manager object
    private var barcodeManager: BarcodeManager? = null

    // Declare a variable to hold scanner device to scan
    private var scanner: Scanner? = null

    // Text view to display status of EMDK and Barcode Scanning Operations
    lateinit var statusTextView: TextView

    lateinit var rvBarcodes: RecyclerView

    // Edit Text that is used to display scanned barcode data
    private lateinit var dataView: EditText


    private val startRead = false

    lateinit var productAdapter: ProductAdapter
    val productList = ArrayList<ScanningLocation>()


    private lateinit var sessionManager: AuthSessionManager
    private lateinit var apiClient: ApiClient

    private lateinit var mHandler: Handler
    private lateinit var mRunnable: Runnable

    // private var fabListener: IOnInteractionListener? = null

    @SuppressLint("LogNotTimber")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /* set title to toolbar */
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = SPACER + MAIN_TITLE

        supportActionBar?.setDisplayShowHomeEnabled(true);
        supportActionBar?.setLogo(R.drawable.ic_factory_white_24dp);
        supportActionBar?.setDisplayUseLogoEnabled(true);

        apiClient = ApiClient()
        sessionManager = AuthSessionManager(this)

        val base64String = get64BaseString("${CLIENT_ID}:${CLIENT_SECRET}")
        Log.i("APP_DEBUG ", "base64 client id+secret $base64String")

        //apiClient.getApiService().login(auth = "Basic ${ApiConstant.CLIENT_B64}", grantType = "client_credentials")
        apiClient.getApiService().login(auth = "Basic $base64String", grantType = "client_credentials")
            .enqueue(object : Callback<LoginResponse> {

                @SuppressLint("LogNotTimber")
                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Log.e("APP_DEBUG", "Error with OAuth2 request")
                }

                @SuppressLint("LogNotTimber")
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    val loginResponse = response.body()

                    if (loginResponse?.typeToken ==  "bearer" && loginResponse.expiresIn == 3600) {

                        Log.i("APP_DEBUG", "OAuth2 Login onResponse $loginResponse")
                        val unixTime = System.currentTimeMillis() + (loginResponse.expiresIn * 1000).toLong()
                        sessionManager.saveAuthToken(loginResponse.authToken)
                        sessionManager.saveExpiresIn(unixTime)

                    } else {
                        Log.e("APP_DEBUG", "Error while retrieving the token value")
                    }
                }
            })

        /*
        fab_main.setOnClickListener { view ->
            Snackbar.make(view, "Send to database", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        */


        findViewById<FloatingActionButton>(R.id.fab_main).setOnClickListener {


            runOnUiThread { // Update the dataView EditText on UI thread with barcode data
                // dataView.text.clear()
                /* dataView.text = ""
                dataViewLoc.text = ""*/
                //doFabDeployment()
                deployFabMenu()

            }

        }

        findViewById<FloatingActionButton>(R.id.fab_post_location).setOnClickListener {
            toBeImplemented("fab_add_host clicked")
        }

        findViewById<FloatingActionButton>(R.id.fab_get_location).setOnClickListener {
            toBeImplemented("fab_get_presets clicked")
        }


        statusTextView = findViewById<TextView>(R.id.textViewStatus)

        //dataView = findViewById<EditText>(R.id.editTextLocation)
        //dataView = findViewById<TextView>(tv_uniqueId)
        //dataViewLoc = findViewById<TextView>(tv_location)

        rvBarcodes = findViewById<RecyclerView>(R.id.rv_products)

        productAdapter = ProductAdapter(this@MainActivity, productList)

        rvBarcodes.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = productAdapter
        }


        val results = EMDKManager.getEMDKManager(applicationContext, this)

        if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            updateStatus("EMDKManager object request failed!");
            return;
        } else {
            updateStatus("EMDKManager object initialization is in progress.......");
        }

    }

    private fun get64BaseString(value: String): String{
        return Base64.encodeToString(value.toByteArray(), Base64.NO_WRAP)
    }

    private fun doFabDeployment() {
        //resetSearchView()
        //fabListener?.onDeployFabMenu()
        //onDeployFabMenu()
        deployFabMenu()
    }

    private fun toBeImplemented(message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
    }

    private fun initBarcodeManager() {

        barcodeManager = emdkManager!!.getInstance(EMDKManager.FEATURE_TYPE.BARCODE) as BarcodeManager

        if (barcodeManager == null) {
            Toast.makeText(this, getString(R.string.not_supported_message), Toast.LENGTH_LONG).show()
            finish()
        }
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
            R.id.action_settings -> {
                showSettings()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @Suppress("DEPRECATION")
    private fun showSettings() {
        Toast.makeText(this, "Settings to be implemented", Toast.LENGTH_LONG).show()
        /*
        supportFragmentManager.commit {
            replace<this>(R.id.preferences)
            setReorderingAllowed(true)
            addToBackStack(null) // Name can be null
        }
        */
    }


    private fun initScanner() {
        if (scanner == null) {

            // Get default scanner defined on the device
            scanner = barcodeManager!!.getDevice(BarcodeManager.DeviceIdentifier.DEFAULT)
            if (scanner != null) {


                scanner!!.addDataListener(this)


                scanner!!.addStatusListener(this)

                scanner!!.triggerType = Scanner.TriggerType.HARD
                try {

                    scanner!!.enable()
                } catch (e: ScannerException) {
                    updateStatus(e.message)
                    deInitScanner()
                }
            } else {
                updateStatus("Failed to initialize the scanner device.")
            }
        }
    }

    private fun deInitScanner() {
        if (scanner != null) {
            try {
                // Release the scanner
                scanner!!.release()
            } catch (e: java.lang.Exception) {
                updateStatus(e.message)
            }
            scanner = null
        }
    }


    override fun onOpened(emdkManager: EMDKManager?) {


        this.emdkManager = emdkManager;

        initBarcodeManager();

        initScanner();
    }

    override fun onClosed() {

        if (emdkManager != null) {
            emdkManager!!.release();
            emdkManager = null;
        }
        updateStatus("EMDK closed unexpectedly! Please close and restart the application.");
    }

    override fun onStatus(status: StatusData?) {

        val state: StatusData.ScannerStates = status!!.state
        var statusStr = ""

        when (state) {
            StatusData.ScannerStates.IDLE -> {
                // Scanner is idle and ready to change configuration and submit read.
                statusStr = status.friendlyName.toString() + " is ready..."

                setConfig()
                try {
                    scanner!!.read()
                } catch (e: ScannerException) {
                    updateStatus(e.message)
                }
            }

            StatusData.ScannerStates.WAITING ->     // Scanner is waiting for trigger press to scan...
                statusStr = if (ofIsScanned == 0) getString(R.string.status_message_of) else getString(
                                    R.string.status_message_location)

            StatusData.ScannerStates.SCANNING ->     // Scanning is in progress...
                statusStr = "Scanning..."

            StatusData.ScannerStates.DISABLED ->     // Scanner is disabled
                statusStr = status.friendlyName.toString() + " is disabled."

            StatusData.ScannerStates.ERROR ->     // Error has occurred during scanning
                statusStr = "An error has occurred."
            else -> {
            }
        }

        updateStatus(statusStr)
    }

    @SuppressLint("LogNotTimber")
    override fun onData(scanDataCollection: ScanDataCollection?) {

        // var dataStr = ""
        val regexOF = """^(R?\d{5}[A-Z]?)(-?\d{2})?$""".toRegex()

        if (scanDataCollection != null && scanDataCollection.result === ScannerResults.SUCCESS) {
            val scanData = scanDataCollection.scanData

            for (data in scanData) {

                val barcodeData = data.data

                if (ofIsScanned == 0) {
                    unixTime = System.currentTimeMillis() / 1000
                    // dataStr = "OF: $barcodeData"

                    // check numof format like 00000 or 00000-00
                    if (regexOF.matches(input = barcodeData)) {
                        locationInfo.uniqueId = barcodeData
                        dataOF = barcodeData
                        locationDataSet.add(dataOF)

                        ofIsScanned++
                    } else {
                        // reset the counter to zero
                        updateStatus(getString(R.string.uniqueid_not_valid))

                        // TODO fix toast issue
                        val toast: Toast = Toast.makeText(this@MainActivity, getString(R.string.uniqueid_not_valid), Toast.LENGTH_SHORT)
                        val view: View = toast.view!!
                        view.setBackgroundColor(Color.RED);
                        toast.setGravity(Gravity.TOP, 0, 140)
                        toast.show()

                        ofIsScanned = 0
                    }

                } else {
                    // dataStr = "LOC: $barcodeData"
                    locationInfo.location = barcodeData
                    dataLOC = barcodeData
                    locationDataSet.add(dataLOC)

                    canPost = true

                    Log.d("APP_DEBUG", "locationInfo to POST: " + locationInfo.toString())
                    if (System.currentTimeMillis() < sessionManager.fetchExpiresIn()!!) {
                        onPostSelected(data = locationInfo)
                        //updateData(locationInfo)
                        // ofIsScanned = 0
                    }

                    updateData(locationDataSet[0] + SEPA + locationDataSet[1])

                    ofIsScanned = 0

                    /*
                    if ((System.currentTimeMillis() / 1000) < (unixTime + 3600)) {
                        // get current token
                        Log.d("APP_DEBUG", "locationInfo to POST: " + locationInfo.toString())
                        onPostSelected( data = locationInfo, token = ACCESS_TOKEN)
                    }
                    else {
                        // renew the token
                        // ACCESS_TOKEN = ""
                        Log.d("APP_DEBUG", "locationInfo to POST: " + locationInfo.toString())
                        onPostSelected(data = locationInfo, token = ACCESS_TOKEN)
                    }
                    */

                }
            }

        }

    }


    private fun updateData(result: String) {
        //private fun updateData(result: OFinfo) {

        var counter: Int = 0

        runOnUiThread { // Update the dataView EditText on UI thread with barcode data and its label type
            if (dataLength++ >= 1) {
                // if (dataLength++ >= 50) {
                // Clear the cache after 50 scans
                // dataView.text.clear()

                //dataLength = 0
            }

            productList.add(ScanningLocation(input = result))

            productAdapter.refresh(productList) // with refresh all cardviews are refreshed
            //productAdapter.add(productList)

            locationDataSet.removeAt(1)
            locationDataSet.removeAt(0)

        }
    }


    private fun updateStatus(status: String?) {
        runOnUiThread { // Update the status text view on UI thread with current scanner state
            statusTextView.text = "" + status
        }
    }


    @SuppressLint("LogNotTimber")
    private fun onPostSelected(data: OFinfo) {
        // do post stuff
        val apiService = OrdsApiClient()

        //apiService.addLocation("Bearer pumeeT_aTv2VFjVwGvrZBA", data) {
        apiService.addLocation("Bearer ${sessionManager.fetchAuthToken()}", data) {
            if (it != null) {
                // popup a message
            } else {
                Log.d("APP_DEBUG", "Error upserting a new OF assignment")
            }
        }
    }

    private fun setConfig() {
        if (scanner != null) {
            try {
                // Get scanner config
                val config = scanner!!.config

                // Enable haptic feedback
                if (config.isParamSupported("config.scanParams.decodeHapticFeedback")) {
                    config.scanParams.decodeHapticFeedback = true
                }

                // Set scanner config
                scanner!!.config = config
            } catch (e: ScannerException) {
                updateStatus(e.message!!)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        // fabListener = null
        if (emdkManager != null) {

            // Clean up the objects created by EMDK manager
            emdkManager!!.release()
            emdkManager = null
        }
    }

    override fun onStop() {
        super.onStop()

        try {
            if (scanner != null) {
                // releases the scanner hardware resources for other application
                // to use. You must call this as soon as you're done with the
                // scanning.
                scanner!!.removeDataListener(this)
                scanner!!.removeStatusListener(this)
                scanner!!.disable()
                scanner = null
            }
        } catch (e: ScannerException) {
            e.printStackTrace()
        }
    }

    private fun deployFabMenu() {

        val mSetView = mutableListOf<View>(
            findViewById<FloatingActionButton>(R.id.fab_post_location),
            findViewById<FloatingActionButton>(R.id.fab_get_location)
        )

        val mSetTextView = mutableListOf<TextView>(
            findViewById<TextView>(R.id.tv_post_location),
            findViewById<TextView>(R.id.tv_get_location)
        )

        if (isOpen) {

            if(isRunning) mHandler.removeCallbacks(mRunnable)
            animHandler(false)
            for(v in mSetView) { v.hideFab() }
            for(t in mSetTextView) { t.hideTip() }
            isOpen = false;

        } else {

            mHandler = Handler()
            mRunnable = Runnable {
                animHandler(false)
                for(v in mSetView) { v.hideFab() }
                for(t in mSetTextView) { t.hideTip() }
                isOpen = false
            }

            animHandler(true)
            isRunning = true
            for(v in mSetView){ v.showFab() }
            for(t in mSetTextView) { t.showTip() }
            isOpen = true
            mHandler.postDelayed(mRunnable, HANDLER_FAB.toLong())
        }
    }


    private fun animHandler(mLaunch: Boolean) {

        val mMove : Animation
        val mPhase : Animation
        val animCollapsed = AnimationUtils.loadAnimation(this, R.anim.fab_collapse)
        val animExpanded = AnimationUtils.loadAnimation(this, R.anim.fab_expand)
        val animRotateRight = AnimationUtils.loadAnimation(this, R.anim.fab_rotate_right)
        val animRotateLeft = AnimationUtils.loadAnimation(this, R.anim.fab_rotate_left)

        if (!mLaunch) {
            mMove = animCollapsed
            mPhase = animRotateLeft
        } else {
            mMove = animExpanded
            mPhase = animRotateRight
        }

        findViewById<FloatingActionButton>(R.id.fab_post_location).startAnimation(mMove)
        findViewById<FloatingActionButton>(R.id.fab_get_location).startAnimation(mMove)

        findViewById<TextView>(R.id.tv_post_location).startAnimation(mMove)
        findViewById<TextView>(R.id.tv_get_location).startAnimation(mMove)

        findViewById<FloatingActionButton>(R.id.fab_main).startAnimation(mPhase)

    }


}