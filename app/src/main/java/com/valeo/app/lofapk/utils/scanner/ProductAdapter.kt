package com.valeo.app.lofapk.utils.scanner

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.valeo.app.lofapk.R
import com.valeo.app.lofapk.model.ScanningLocation
import com.valeo.app.lofapk.utils.ApiConstant.Companion.SEPA
import java.util.ArrayList

class ProductAdapter(ctx: Context?, list: ArrayList<ScanningLocation>?) :
    BaseAdapter<ScanningLocation, ProductAdapter.ProductViewHolder>(ctx, list) {


    var productList: ArrayList<ScanningLocation>? = list
    // var mContext: Context? = ctx

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductAdapter.ProductViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_scanned, parent, false)
        return ProductViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return productList!!.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ProductAdapter.ProductViewHolder, position: Int) {

        val product = productList!![position]
        // holder.uniqueidName.text = """UNIQUEID: ${(product.input).split(SEPA)[0]}"""
        // holder.locationName.text = """LOCATION: ${(product.input).split(SEPA)[1]}"""
        holder.uniqueidName.text = """NUMOF: ${(product.input).split(SEPA)[0]}"""
        holder.locationName.text = """EMPLACEMENT: ${(product.input).split(SEPA)[1]}"""

    }

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val uniqueidName: TextView = itemView.findViewById(R.id.txt_uniqueid)
        val locationName: TextView = itemView.findViewById(R.id.txt_location)

        // val mainCard: MaterialCardView = itemView.findViewById(R.id.cv_container)

    }
}