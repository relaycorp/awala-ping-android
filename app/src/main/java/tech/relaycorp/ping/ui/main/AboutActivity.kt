package tech.relaycorp.ping.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.mikepenz.aboutlibraries.LibsBuilder
import tech.relaycorp.ping.BuildConfig
import tech.relaycorp.ping.R
import tech.relaycorp.ping.databinding.ActivityAboutBinding
import tech.relaycorp.ping.ui.BaseActivity

class AboutActivity : BaseActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavigation()

        binding.version.text = getString(
            R.string.about_version,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE.toString()
        )
        binding.learnMore.setOnClickListener { openKnowMore() }
        binding.libraries.setOnClickListener { openLicenses() }
    }

    private fun openKnowMore() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.awala_website))))
    }

    private fun openLicenses() {
        LibsBuilder()
            .withActivityTitle(getString(R.string.about_licenses))
            .withAboutIconShown(false)
            .withVersionShown(false)
            .withOwnLibsActivityClass(LicensesActivity::class.java)
            .withEdgeToEdge(true)
            .withFields(R.string::class.java.fields)
            .start(this)
    }

    companion object {
        fun getIntent(context: Context) = Intent(context, AboutActivity::class.java)
    }
}
