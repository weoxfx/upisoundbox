package com.weox.upisoundbox.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(
                this,
                "Notification permission denied — the app can't post the test alert without it.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        detectInstalledUpiApps()
        renderAppList()
        requestNotificationPermissionIfNeeded()

        binding.grantNotificationAccessButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        binding.saveSelectionButton.setOnClickListener {
            saveSelection()
        }

        binding.sendTestNotificationButton.setOnClickListener {
            if (!hasNotificationPermission()) {
                requestNotificationPermissionIfNeeded()
                Toast.makeText(
                    this,
                    "Grant notification permission first, then tap test again.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            TestNotificationSender.sendTestPayment(this, amountRupees = 50.0)
            Toast.makeText(
                this,
                "Test notification sent. If notification access is granted, you should hear the alert.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun hasNotificationPermission(): Boolean {
        // Only relevant on Android 13+ — earlier versions never needed this permission.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (!hasNotificationPermission()) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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
