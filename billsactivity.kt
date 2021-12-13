
package com.maj.gstbillingapp

import android.Manifest
//import com.maj.gstbillingapp.BillAdapter.swapCursor
import androidx.appcompat.app.AppCompatActivity
import com.maj.gstbillingapp.BillAdapter.BillItemClickListener
import androidx.recyclerview.widget.RecyclerView
import com.maj.gstbillingapp.BillAdapter
import android.os.Bundle
import com.maj.gstbillingapp.R
import com.maj.gstbillingapp.data.GSTBillingContract
import android.os.StrictMode.VmPolicy
import android.os.StrictMode
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.content.Intent
import com.maj.gstbillingapp.NewBillCustomerActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.maj.gstbillingapp.BillsActivity
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.maj.gstbillingapp.SetupPasswordActivity
import android.os.Build
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.maj.gstbillingapp.DetailActivity
import java.lang.RuntimeException

class BillsActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor>,
    BillItemClickListener {
    private var unpaidRecyclerView: RecyclerView? = null
    private var adapter: BillAdapter? = null
    private var billListStatus: String? = null
    private var billDividerColor = 0
    private var billSortOrder: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bills)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        billListStatus = if (savedInstanceState != null) {
            savedInstanceState.getString(GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_STATUS)
        } else {
            GSTBillingContract.BILL_STATUS_UNPAID
        }
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        isStoragePermissionGranted
        when (billListStatus) {
            GSTBillingContract.BILL_STATUS_PAID -> {
                supportActionBar!!.setTitle(R.string.paid_bills_title)
                billDividerColor = Color.GREEN
                billSortOrder = " DESC"
            }
            GSTBillingContract.BILL_STATUS_UNPAID -> {
                supportActionBar!!.setTitle(R.string.unpaid_bills_title)
                billDividerColor = Color.RED
                billSortOrder = " ASC"
            }
        }
        val fab = findViewById<View>(R.id.fab_unpaid) as FloatingActionButton
        fab.setOnClickListener {
            startActivity(
                Intent(
                    this@BillsActivity,
                    NewBillCustomerActivity::class.java
                )
            )
        }
        checkPasswordSetup()
        unpaidRecyclerView = findViewById<View>(R.id.unpaid_recycler_view) as RecyclerView
        unpaidRecyclerView!!.layoutManager = LinearLayoutManager(this)
        unpaidRecyclerView!!.setHasFixedSize(true)
        adapter = BillAdapter(this, this, billDividerColor)
        unpaidRecyclerView!!.adapter = adapter
        supportLoaderManager.initLoader(BILL_LOADER_ID, null, this)
    }

    private fun checkPasswordSetup() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        if (prefs.getString(SetupPasswordActivity.SETUP_PASSWORD_KEY, null) == null) {
            val intent = Intent(this, SetupPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    val isStoragePermissionGranted: Boolean
        get() = if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
                false
            }
        } else {
            true
        }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_bills_list, menu)
        if (billListStatus == GSTBillingContract.BILL_STATUS_PAID) {
            menu.findItem(R.id.action_swap_bills_list).setTitle(R.string.action_show_unpaid_bills)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        if (id == R.id.action_swap_bills_list) {
            when (billListStatus) {
                GSTBillingContract.BILL_STATUS_UNPAID -> {
                    billListStatus = GSTBillingContract.BILL_STATUS_PAID
                    item.title = getString(R.string.action_show_unpaid_bills)
                    supportActionBar!!.title = getString(R.string.paid_bills_title)
                    billDividerColor = Color.GREEN
                    billSortOrder = " DESC"
                }
                GSTBillingContract.BILL_STATUS_PAID -> {
                    billListStatus = GSTBillingContract.BILL_STATUS_UNPAID
                    item.title = getString(R.string.action_show_paid_bills)
                    supportActionBar!!.title = getString(R.string.unpaid_bills_title)
                    billDividerColor = Color.RED
                    billSortOrder = " ASC"
                }
            }
            supportLoaderManager.restartLoader(BILL_LOADER_ID, null, this)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return when (id) {
            BILL_LOADER_ID -> CursorLoader(
                this,
                GSTBillingContract.GSTBillingEntry.CONTENT_URI,
                null,
                GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_STATUS + "='" + billListStatus + "'",
                null,
                GSTBillingContract.GSTBillingEntry._ID + billSortOrder
            )
            else -> throw RuntimeException("Loader not implemented: $id")
        }
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        adapter!!.swapCursor(data, billDividerColor)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        adapter!!.swapCursor(null, Color.RED)
    }

    override fun onBillItemClick(
        clickedBillId: String?,
        customerName: String?,
        phoneNumber: String?
    ) {
        val detailIntent = Intent(this, DetailActivity::class.java)
        detailIntent.putExtra(GSTBillingContract.GSTBillingEntry._ID, clickedBillId)
        detailIntent.putExtra(
            GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_STATUS,
            billListStatus
        )
        detailIntent.putExtra(GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_NAME, customerName)
        detailIntent.putExtra(
            GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_PHONE_NUMBER,
            phoneNumber
        )
        startActivity(detailIntent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_STATUS, billListStatus)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val BILL_LOADER_ID = 100
    }
}
