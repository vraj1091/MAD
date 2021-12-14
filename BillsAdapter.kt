package com.maj.gstbillingapp

import android.content.Context
import android.database.Cursor
import android.view.*
import com.maj.gstbillingapp.DetailActivity.Companion.printTotalDetails
import com.maj.gstbillingapp.DetailActivity.Companion.editItem
import androidx.recyclerview.widget.RecyclerView
import com.maj.gstbillingapp.DetailAdapter.DetailHolder
import com.maj.gstbillingapp.R
import com.maj.gstbillingapp.data.GSTBillingContract
import com.maj.gstbillingapp.utils.PriceUtils
import com.maj.gstbillingapp.DetailActivity
import android.view.View.OnCreateContextMenuListener
import android.widget.TextView
import android.view.ContextMenu.ContextMenuInfo

class DetailAdapter(private val mContext: Context) : RecyclerView.Adapter<DetailHolder>() {
    private var mCursor: Cursor? = null
    var totalTaxableValue = 0f
    var totalSingleGst = 0f
    var totalAmount = 0f
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailHolder {
        val view =
            LayoutInflater.from(mContext).inflate(R.layout.single_item_detail_layout, parent, false)
        return DetailHolder(view)
    }

    override fun onBindViewHolder(holder: DetailHolder, position: Int) {
        if (mCursor!!.moveToPosition(position)) {
            val idValue =
                mCursor!!.getInt(mCursor!!.getColumnIndex(GSTBillingContract.GSTBillingCustomerEntry._ID))
            val itemDescriptionValue =
                mCursor!!.getString(mCursor!!.getColumnIndex(GSTBillingContract.GSTBillingCustomerEntry.SECONDARY_COLUMN_ITEM_DESCRIPTION))
            val finalPriceValue =
                mCursor!!.getFloat(mCursor!!.getColumnIndex(GSTBillingContract.GSTBillingCustomerEntry.SECONDARY_COLUMN_FINAL_PRICE))
            val quantityValue =
                mCursor!!.getInt(mCursor!!.getColumnIndex(GSTBillingContract.GSTBillingCustomerEntry.SECONDARY_COLUMN_QUANTITY))
            val taxSlabValue =
                mCursor!!.getInt(mCursor!!.getColumnIndex(GSTBillingContract.GSTBillingCustomerEntry.SECONDARY_COLUMN_TAX_SLAB))
            val priceUtils = PriceUtils(finalPriceValue, quantityValue, taxSlabValue)
            val rateValue = priceUtils.rate
            val taxableValueValue = priceUtils.taxableValue
            val singleGstValue = priceUtils.singleGst
            holder.itemDescription.text = itemDescriptionValue
            holder.sno.text = idValue.toString()
            holder.finalPrice.text = String.format("%.2f", finalPriceValue)
            holder.qty.text = quantityValue.toString()
            holder.rate.text = String.format("%.2f", rateValue)
            holder.taxableValue.text = String.format("%.2f", taxableValueValue)
            holder.taxSlab.text = "$taxSlabValue%"
            holder.cgst.text = String.format("%.2f", singleGstValue)
            holder.sgst.text = String.format("%.2f", singleGstValue)
            holder.itemView.setTag(R.id.bill_edit_id, idValue)
            holder.itemView.setTag(R.id.bill_edit_item_description, itemDescriptionValue)
            holder.itemView.setTag(R.id.bill_edit_final_price, finalPriceValue)
            holder.itemView.setTag(R.id.bill_edit_quantity, quantityValue)
            totalTaxableValue += taxableValueValue
            totalSingleGst += singleGstValue
            totalAmount += finalPriceValue * quantityValue
            if (position == itemCount - 1) {
                printTotalDetails(totalTaxableValue, totalSingleGst, totalAmount)
            }
        }
    }

    override fun getItemCount(): Int {
        return if (mCursor == null) {
            0
        } else {
            mCursor!!.count
        }
    }

    fun swapCursor(newCursor: Cursor?) {
        mCursor = newCursor
        totalTaxableValue = 0f
        totalSingleGst = 0f
        totalAmount = 0f
        notifyDataSetChanged()
    }

    inner class DetailHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        OnCreateContextMenuListener {
        var itemDescription: TextView
        var sno: TextView
        var finalPrice: TextView
        var qty: TextView
        var rate: TextView
        var taxableValue: TextView
        var taxSlab: TextView
        var cgst: TextView
        var sgst: TextView
        override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
            val editItem = menu.add(Menu.NONE, 1, 1, R.string.action_edit_bill_item_label)
            editItem.setOnMenuItemClickListener(onEditItemMenu)
        }

        private val onEditItemMenu = MenuItem.OnMenuItemClickListener {
            val id = itemView.getTag(R.id.bill_edit_id) as Int
            val itemDescription = itemView.getTag(R.id.bill_edit_item_description) as String
            val finalPrice = itemView.getTag(R.id.bill_edit_final_price) as Float
            val quantity = itemView.getTag(R.id.bill_edit_quantity) as Int
            editItem(mContext, id, itemDescription, finalPrice, quantity)
            true
        }

        init {
            itemDescription = itemView.findViewById<View>(R.id.detail_item) as TextView
            sno = itemView.findViewById<View>(R.id.detail_sno) as TextView
            finalPrice = itemView.findViewById<View>(R.id.detail_final_price) as TextView
            qty = itemView.findViewById<View>(R.id.detail_quantity) as TextView
            rate = itemView.findViewById<View>(R.id.detail_rate) as TextView
            taxableValue = itemView.findViewById<View>(R.id.detail_taxable_value) as TextView
            taxSlab = itemView.findViewById<View>(R.id.detail_tax_slab) as TextView
            cgst = itemView.findViewById<View>(R.id.detail_cgst) as TextView
            sgst = itemView.findViewById<View>(R.id.detail_sgst) as TextView
            itemView.setOnCreateContextMenuListener(this)
        }
    }
}
