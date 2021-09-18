package com.github.barteksc.sample

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.github.barteksc.sample.databinding.ActivityFromUrlBinding
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfDocument.Bookmark
import kotlinx.coroutines.flow.collectLatest

class FromUrlActivity : AppCompatActivity(R.layout.activity_from_url), OnLoadCompleteListener {
    private val TAG = PDFViewActivity::class.java.simpleName

    private lateinit var binding: ActivityFromUrlBinding
    private val viewModel by viewModels<FromUrlViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFromUrlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.loadPdf()

        lifecycleScope.launchWhenCreated {
            viewModel.dataStateFlow.collectLatest { state ->
                when (state) {
                    is FromUrlViewModel.DataState.Loading -> {
                        binding.progressCircular.isVisible = true
                    }
                    is FromUrlViewModel.DataState.Error -> {
                        binding.progressCircular.isVisible = false
                        Log.i("dataStateFlow", "error : ${state.exception}")
                        Toast.makeText(
                            this@FromUrlActivity,
                            "Error: ${state.exception}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is FromUrlViewModel.DataState.Success -> {
                        binding.progressCircular.isVisible = false
                        binding.pdfView.fromStream(state.data)
                            .defaultPage(0)
                            // allows to draw something on the current page, usually visible in the middle of the screen
//                            .onDraw(onDrawListener)
                            // allows to draw something on all pages, separately for every page. Called only for visible pages
//                            .onDrawAll(onDrawListener)
//                            .onLoad(onLoadCompleteListener) // called after document is loaded and starts to be rendered
//                            .onPageChange(onPageChangeListener)
//                            .onPageScroll(onPageScrollListener)
//                            .onError(onErrorListener)
//                            .onPageError(onPageErrorListener)
//                            .onRender(onRenderListener) // called after document is rendered for the first time
                            // called on single tap, return true if handled, false to toggle scroll handle visibility
//                            .onTap(onTapListener)
//                            .onLongPress(onLongPressListener)
                            .onLoad(this@FromUrlActivity)
                            .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                            .autoSpacing(true) // add dynamic spacing to fit each page on its own on the screen
//                            .linkHandler(DefaultLinkHandler)
                            .pageFitPolicy(FitPolicy.BOTH) // mode to fit pages in the view
                            .fitEachPage(true)
                            .pageSnap(true) // snap pages to screen boundaries
                            .pageFling(true) // make a fling change only a single page like ViewPager
                            .nightMode(false) // toggle night mode
                            .load()
                    }
                    is FromUrlViewModel.DataState.Idle -> {
                    }
                }
            }
        }
    }

    override fun loadComplete(nbPages: Int) {
        val meta: PdfDocument.Meta = binding.pdfView.documentMeta
        Log.e(TAG, "title = " + meta.title)
        Log.e(TAG, "author = " + meta.author)
        Log.e(TAG, "subject = " + meta.subject)
        Log.e(TAG, "keywords = " + meta.keywords)
        Log.e(TAG, "creator = " + meta.creator)
        Log.e(TAG, "producer = " + meta.producer)
        Log.e(TAG, "creationDate = " + meta.creationDate)
        Log.e(TAG, "modDate = " + meta.modDate)

        printBookmarksTree(binding.pdfView.tableOfContents, "-")
    }

    private fun printBookmarksTree(tree: List<Bookmark>, sep: String) {
        for (b in tree) {
            Log.e(TAG, String.format("%s %s, p %d", sep, b.title, b.pageIdx))
            if (b.hasChildren()) {
                printBookmarksTree(b.children, "$sep-")
            }
        }
    }

}