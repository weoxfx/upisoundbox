package com.weox.upisoundbox.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.weox.upisoundbox.databinding.ActivityMainBinding
import com.weox.upisoundbox.parser.ParserRegistry
import com.weox.upisoundbox.service.SelectedAppsStore

/**
 * Known UPI apps we support, with a human-readable label for the picker UI.
 * Package names must match the <queries> block in the manifest exactly.
 */
private val KNOWN_UPI_APPS = mapOf(
    "com.google.android.apps.nbu.paisa.user" to "Google Pay",
    "com.phonepe.app" to "PhonePe",
    "net.one97.paytm" to "Paytm",
    "in.org.npci.upiapp" to "BHIM"
)

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val installedApps = mutableListOf<Pair<String, String>>() // packageName to label

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        detectInstalledUpiApps()
        renderAppList()

        binding.grantNotificationAccessButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        binding.saveSelectionButton.setOnClickListener {
            saveSelection()
        }

        binding.sendTestNotificationButton.setOnClickListener {
            TestNotificationSender.sendTestPayment(this, amountRupees = 50.0)
            Toast.makeText(
                this,
                "Test notification sent. If notification access is granted, you should hear the alert.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun detectInstalledUpiApps() {
        installedApps.clear()
        val pm = packageManager
        for ((pkg, label) in KNOWN_UPI_APPS) {
            val isInstalled = try {
                pm.getPackageInfo(pkg, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
            if (isInstalled) installedApps.add(pkg to label)
        }
    }

    private fun renderAppList() {
        if (installedApps.isEmpty()) {
            Toast.makeText(
                this,
                "No supported UPI apps found on this device.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val labels = installedApps.map { it.second }.toTypedArray()
        val previouslySelected = SelectedAppsStore.getSelected(this)
        val checkedItems = installedApps.map { it.first in previouslySelected }.toBooleanArray()

        binding.appListView.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_multiple_choice,
            labels
        )
        binding.appListView.choiceMode = android.widget.AbsListView.CHOICE_MODE_MULTIPLE
        for (i in checkedItems.indices) {
            binding.appListView.setItemChecked(i, checkedItems[i])
        }
    }

    private fun saveSelection() {
        val checkedPositions = binding.appListView.checkedItemPositions
        val selectedPackages = mutableSetOf<String>()
        for (i in 0 until checkedPositions.size()) {
            val position = checkedPositions.keyAt(i)
            if (checkedPositions.valueAt(i)) {
                selectedPackages.add(installedApps[position].first)
            }
        }
        SelectedAppsStore.setSelected(this, selectedPackages)
        Toast.makeText(this, "Saved. Alerts enabled for ${selectedPackages.size} app(s).", Toast.LENGTH_SHORT).show()
    }
}
