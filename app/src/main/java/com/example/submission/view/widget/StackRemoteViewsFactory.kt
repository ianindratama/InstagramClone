package com.example.submission.view.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Binder
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.bumptech.glide.Glide
import com.example.submission.R
import com.example.submission.network.StoryResponse
import com.example.submission.database.StoryResponseDatabase


internal class StackRemoteViewsFactory(private val mContext: Context): RemoteViewsService.RemoteViewsFactory {

    private val mWidgetItems = ArrayList<StoryResponse>()

    override fun onCreate() {

    }

    override fun onDataSetChanged() {

        val identityToken = Binder.clearCallingIdentity()


        val database = StoryResponseDatabase.getDatabase(mContext)

        mWidgetItems.clear()
        mWidgetItems.addAll(database.storyResponseDao().getWidgetStoryResponses())

        Binder.restoreCallingIdentity(identityToken)

    }

    override fun onDestroy() {

    }

    override fun getCount(): Int = mWidgetItems.size

    override fun getViewAt(p0: Int): RemoteViews {

        val rv = RemoteViews(mContext.packageName, R.layout.widget_item)

        try {

            val bitmap: Bitmap = Glide.with(mContext)
                .asBitmap()
                .load(mWidgetItems[p0].photoUrl)
                .submit(512, 512)
                .get()

            rv.setImageViewBitmap(R.id.imageView, bitmap)

            val fillInIntent = Intent()
            fillInIntent.putExtra("Story", mWidgetItems[p0])

            rv.setOnClickFillInIntent(R.id.imageView, fillInIntent)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return rv
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 20

    override fun getItemId(p0: Int): Long = 0

    override fun hasStableIds(): Boolean = false

}