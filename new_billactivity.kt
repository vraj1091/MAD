package com.maj.gstbillingapp

import com.maj.gstbillingapp.DetailActivity.Companion.changeBillStatus
import androidx.appcompat.app.AppCompatActivity
import android.content.ContentValues
import android.os.Bundle
import com.maj.gstbillingapp.R
import com.maj.gstbillingapp.DetailActivity
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.maj.gstbillingapp.data.GSTBillingContract
import com.maj.gstbillingapp.NewBillCustomerActivity
import com.maj.gstbillingapp.NewBillActivity
import java.text.SimpleDateFormat
import java.util.*

class NewBillActivity : AppCompatActivity() {
    private var taxSlabSpinner: Spinner? = null
    private var itemDescription: EditText? = null
    private var finalPriceEt: EditText? = null
    private var quantityEt: EditText? = null
    private var finishBtn: Button? = null
    private var taxSlab = 0
    var cvList: MutableList<ContentValues>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_bill)
        taxSlabSpinner = findViewById<View>(R.id.tax_slab_spinner) as Spinner
        setupTaxSpinner()
        itemDescription = findViewById<View>(R.id.new_item_value) as EditText
        finalPriceEt = findViewById<View>(R.id.new_final_price_value) as EditText
        quantityEt = findViewById<View>(R.id.new_quantity_value) as EditText
        finishBtn = findViewById<View>(R.id.finish_btn) as Button
        finishBtn!!.isEnabled = false
        if (intent.hasExtra(DetailActivity.EDITING_ITEM)) {
            supportActionBar!!.setTitle(R.string.action_edit_bill_item_label)
            findViewById<View>(R.id.add_to_bill_btn).visibility = View.GONE
            finishBtn!!.visibility = View.GONE
            val editIntent = intent
            val idValue = editIntent.getIntExtra(GSTBillingContract.GSTBillingCustomerEntry._ID, 0)
            val itemDescriptionValue =
                editIntent.getStringExtra(GSTBillingContract.GSTBillingCustomerEntry.SECONDARY_COLUMN_ITEM_DESCRIPTION)
            val finalPriceValue = editIntent.getFloatExtra(
                GSTBillingContract.GSTBillingCustomerEntry.SECONDARY_COLUMN_FINAL_PRICE,
                0f
            )
            val quantityValue = editIntent.getIntExtra(
                GSTBillingContract.GSTBillingCustomerEntry.SECONDARY_COLUMN_QUANTITY,
                0
            )
            itemDescription!!.setText(itemDescriptionValue)
            finalPriceEt.setText(finalPriceValue as Int.toString())
            quantityEt!!.setText(quantityValue.toString())
            val doneEditingBtn = findViewById<View>(R.id.done_edit_item_btn) as Button
            doneEditingBtn.visibility = View.VISIBLE
            doneEditingBtn.setOnClickListener(View.OnClickListener {
                if (itemDescription!!.text.toString().length == 0) {
                    itemDescription!!.setText("NA")
                }
                if (finalPriceEt!!.text.toString().length == 0) {
                    finalPriceEt!!.requestFocus()
                    Toast.makeText(
                        this@NewBillActivity,
                        getString(R.string.enter_final_price_error),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@OnClickListener
                }
                if (quantityEt!!.text.toString().length == 0) {
                    quantityEt!!.setText("1")
                }
                val cv = ContentValues()
                cv.put(
                    GSTBillingContract.GSTBillingCustomerEntry.SECONDARY_COLUMN_ITEM_DESCRIPTION,
                    itemDescription!!.text.toString()
                )
                cv.put(
                    GSTBillingContract.GSTBillingCustomerEntry.SECONDARY_COLUMN_FINAL_PRICE,
                    finalPriceEt!!.text.toString().toInt()
                )
                cv.put(
                    GSTBillingContract.GSTBillingCustomerEntry.SECONDARY_COLUMN_QUANTITY,
                    quantityEt!!.text.toString().toInt()
                )
                cv.put(
                    GSTBillingContract.GSTBillingCustomerEntry.SECONDARY_COLUMN_TAX_SLAB,
                    taxSlab
                )
                contentResolver.update(
                    GSTBillingContract.GSTBillingEntry.CONTENT_URI.buildUpon()
                        .appendPath(editIntent.getStringExtra(DetailActivity.EDITING_ITEM))
                        .appendPath(idValue.toString()).build(),
                    cv,
                    null,
                    null
                )
                val contentValues = ContentValues()
                contentValues.put(
                    GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_STATUS,
                    GSTBillingContract.BILL_STATUS_UNPAID
                )
                contentResolver.update(
                    GSTBillingContract.GSTBillingEntry.CONTENT_URI.buildUpon()
                        .appendPath(editIntent.getStringExtra(DetailActivity.EDITING_ITEM)).build(),
                    contentValues,
                    GSTBillingContract.GSTBillingEntry._ID + "=" + editIntent.getStringExtra(
                        DetailActivity.EDITING_ITEM
                    ),
                    null
                )
                changeBillStatus()
                finish()
            })
        } else {
            cvList = ArrayList()
        }
    }

    private fun setupTaxSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this, R.array.tax_slab_list_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        taxSlabSpinner!!.adapter = adapter
        taxSlabSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> taxSlab = 28
                    1 -> taxSlab = 18
                    2 -> taxSlab = 12
                    3 -> taxSlab = 5
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                taxSlab = 28
            }
        }
    }

    fun addToBill(view: View?) {
        if (itemDescription!!.text.toString().length == 0) {
            itemDescription!!.setText("NA")
        }
        if (finalPriceEt!!.text.toString().length == 0) {
            finalPriceEt!!.requestFocus()
            Toast.makeText(this, getString(R.string.enter_final_price_error), Toast.LENGTH_SHORT)
                .show()
            return
        }
        if (quantityEt!!.text.toString().length == 0) {
            quantityEt!!.setText("1")
        }
        val cv = ContentValues()
        cv.put(
            GSTBillingContract.GSTBillingCustomerEntry.SECONDARY_COLUMN_ITEM_DESCRIPTION,
            itemDescription!!.text.toString()
        )
        cv.put(
            GSTBillingContract.GSTBillingCustomerEntry.SECONDARY_COLUMN_FINAL_PRICE,
            finalPriceEt!!.text.toString().toInt()
        )
        cv.put(
            GSTBillingContract.GSTBillingCustomerEntry.SECONDARY_COLUMN_QUANTITY,
            quantityEt!!.text.toString().toInt()
        )
        cv.put(GSTBillingContract.GSTBillingCustomerEntry.SECONDARY_COLUMN_TAX_SLAB, taxSlab)
        cvList!!.add(cv)
        Toast.makeText(this, getString(R.string.item_added_success), Toast.LENGTH_SHORT).show()
        itemDescription!!.setText("")
        finalPriceEt!!.setText("")
        quantityEt!!.setText("")
        finishBtn!!.isEnabled = true
        itemDescription!!.requestFocus()
    }

    fun finishAddingItems(view: View?) {

        // Check if any item is added in Selling price EditText before finishing the bill
        if (finalPriceEt!!.text.toString().length != 0) {
            Toast.makeText(this, getString(R.string.add_item_to_bill_error), Toast.LENGTH_SHORT)
                .show()
            return
        }
        if (!intent.hasExtra(DetailActivity.ADDING_MORE_ITEMS)) {
            // Inserting customer details in primary table
            val intent = intent
            val customerName = intent.getStringExtra(NewBillCustomerActivity.ADD_CUSTOMER_NAME_KEY)
            val phoneNumber = intent.getStringExtra(NewBillCustomerActivity.ADD_CUSTOMER_PHONE_KEY)
            val billDate = SimpleDateFormat("dd-MM-yyyy").format(Date())
            val billStatus = GSTBillingContract.BILL_STATUS_UNPAID
            val contentValues = ContentValues()
            contentValues.put(GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_NAME, customerName)
            contentValues.put(
                GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_PHONE_NUMBER,
                phoneNumber
            )
            contentValues.put(GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_DATE, billDate)
            contentValues.put(GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_STATUS, billStatus)
            val idUri = contentResolver.insert(
                GSTBillingContract.GSTBillingEntry.CONTENT_URI,
                contentValues
            )

            // Inserting item details in secondary table
            val id = idUri!!.lastPathSegment
            contentResolver.bulkInsert(
                GSTBillingContract.GSTBillingEntry.CONTENT_URI.buildUpon().appendPath(id).build(),
                cvList!!.toTypedArray()
            )

            // Opening detail activity
            val detailIntent = Intent(this, DetailActivity::class.java)
            detailIntent.putExtra(GSTBillingContract.GSTBillingEntry._ID, id)
            detailIntent.putExtra(
                GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_NAME,
                customerName
            )
            detailIntent.putExtra(
                GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_PHONE_NUMBER,
                phoneNumber
            )
            detailIntent.putExtra(
                GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_STATUS,
                GSTBillingContract.BILL_STATUS_UNPAID
            )
            startActivity(detailIntent)
            finish()
        } else {
            addingMoreItems = true
            val id = intent.getStringExtra(GSTBillingContract.GSTBillingEntry._ID)
            contentResolver.bulkInsert(
                GSTBillingContract.GSTBillingEntry.CONTENT_URI.buildUpon().appendPath(id).build(),
                cvList!!.toTypedArray()
            )
            val contentValues = ContentValues()
            contentValues.put(
                GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_STATUS,
                GSTBillingContract.BILL_STATUS_UNPAID
            )
            contentResolver.update(
                GSTBillingContract.GSTBillingEntry.CONTENT_URI.buildUpon().appendPath(id.toString())
                    .build(),
                contentValues,
                GSTBillingContract.GSTBillingEntry._ID + "=" + id,
                null
            )
            changeBillStatus()
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bill, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_discard) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        @JvmField
        var addingMoreItems = false
    }
}
