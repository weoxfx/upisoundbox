package com.weox.upisoundbox.ui

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.weox.upisoundbox.R
import com.weox.upisoundbox.databinding.FragmentHomeBinding
import com.weox.upisoundbox.service.PaymentHistoryStore
import com.weox.upisoundbox.service.UpiNotificationListenerService
import com.weox.upisoundbox.ui.TestNotificationSender

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(
                requireContext(),
                R.string.permission_denied,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        loadStats()
        animateEntrance()
    }

    private fun setupUI() {
        binding.btnTestAlert.setOnClickListener {
            if (!hasNotificationPermission()) {
                requestNotificationPermissionIfNeeded()
                Toast.makeText(
                    requireContext(),
                    R.string.grant_permission_first,
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            TestNotificationSender.sendTestPayment(requireContext(), 50.0)
            Toast.makeText(requireContext(), R.string.test_sent, Toast.LENGTH_SHORT).show()
        }

        val isActive = isNotificationListenerEnabled()
        updateStatusUI(isActive)
    }

    private fun loadStats() {
        val context = requireContext()
        val total = PaymentHistoryStore.getTotalCount(context)
        val today = PaymentHistoryStore.getTodayCount(context)

        binding.tvTotalCount.text = total.toString()
        binding.tvTodayCount.text = today.toString()

        val recent = PaymentHistoryStore.getHistory(context).take(3)
        updateRecentActivity(recent)
    }

    private fun updateRecentActivity(recent: List<com.weox.upisoundbox.model.PaymentHistoryItem>) {
        val container = binding.layoutRecentContainer
        if (recent.isEmpty()) {
            binding.tvNoPayments.visibility = View.VISIBLE
            binding.tvNoPaymentsDesc.visibility = View.VISIBLE
            container.removeAllViews()
        } else {
            binding.tvNoPayments.visibility = View.GONE
            binding.tvNoPaymentsDesc.visibility = View.GONE
            container.removeAllViews()
            recent.forEach { item ->
                val textView = android.widget.TextView(requireContext()).apply {
                    text = "${item.displayAmount} • ${item.sourceApp}"
                    textSize = 14f
                    setTextColor(resources.getColor(R.color.on_surface_dark, null))
                    alpha = 0.8f
                    setPadding(0, 8, 0, 8)
                }
                container.addView(textView)
            }
        }
    }

    private fun updateStatusUI(isActive: Boolean) {
        val dot = binding.ivStatusDot
        val title = binding.tvStatusTitle
        val desc = binding.tvStatusDesc
        val btn = binding.btnTestAlert

        if (isActive) {
            dot.setImageResource(R.drawable.status_dot_active)
            title.text = R.string.listening_for_payments
            desc.text = getString(R.string.status_active)
            btn.visibility = View.VISIBLE
            btn.isEnabled = true
        } else {
            dot.setImageResource(R.drawable.status_dot_inactive)
            title.text = R.string.status_inactive
            desc.text = getString(R.string.grant_notification_access)
            btn.visibility = View.GONE
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val cn = android.content.ComponentName(
            requireContext(),
            UpiNotificationListenerService::class.java
        )
        val flat = Settings.Secure.getString(
            requireContext().contentResolver,
            "enabled_notification_listeners"
        )
        return flat?.contains(cn.flattenToString()) == true
    }

    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (!hasNotificationPermission()) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun animateEntrance() {
        val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(context, R.anim.slide_up)
        val scaleIn = AnimationUtils.loadAnimation(context, R.anim.scale_in)

        binding.cardTotal.startAnimation(slideUp)
        binding.cardTotal.postDelayed({
            binding.cardToday.startAnimation(slideUp)
        }, 80)
        binding.cardToday.postDelayed({
            binding.cardStatus.startAnimation(slideUp)
        }, 160)
        binding.cardStatus.postDelayed({
            binding.cardRecent.startAnimation(slideUp)
        }, 240)

        binding.tvGreeting.startAnimation(fadeIn)
        binding.tvSubtitle.startAnimation(fadeIn)
    }

    override fun onResume() {
        super.onResume()
        loadStats()
        updateStatusUI(isNotificationListenerEnabled())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
