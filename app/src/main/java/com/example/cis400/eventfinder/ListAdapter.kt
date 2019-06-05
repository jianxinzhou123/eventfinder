package com.example.cis400.eventfinder

import android.content.Context
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.*
import android.view.animation.AlphaAnimation
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.list_adapter.view.*




class ListAdapter(var items: MutableList<EventData>, val context: Context) : RecyclerView.Adapter<ListAdapter.ViewHolder>(){

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_adapter, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder?.item.tvName.isSelected = true


        var anim = AlphaAnimation(0.0f, 1.0f)
        anim.duration = 2500
        holder?.item.startAnimation(anim)

        holder?.item.tvName.ellipsize = TextUtils.TruncateAt.MARQUEE
        holder?.item.tvName.setSingleLine(true)
        holder?.item.tvName.text = items.get(position).name


        holder?.item.tvDescription.isSelected = true
        holder?.item.tvDescription.text = items.get(position).description

        Picasso.with(context)
            .load(items.get(position).image)
            .error(R.mipmap.ic_launcher)
            .into(holder?.item.ivPic)

        holder?.item.cardViewList.setOnClickListener {
            EventFragment.eventInfo = items.get(position)
            RecyclerViewFragment.events.clear()
            (context as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EventFragment())
                .commit()
        }
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        var item = view
    }
}