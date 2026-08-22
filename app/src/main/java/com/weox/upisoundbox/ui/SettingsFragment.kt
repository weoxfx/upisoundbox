package com.weox.upisoundbox.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.weox.upisoundbox.R
import com.weox.upisoundbox.databinding.FragmentSettingsBinding
import com.weox.upisoundbox.ui.TestNotificationSender

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        animateEntrance()
    }

    private fun setupUI() {
        binding.btnGrantAccess.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        binding.btnTestSound.setOnClickListener {
            TestNotificationSender.sendTestPayment(requireContext(), 50.0)
        }

        binding.tvVersion.text = "Version 1.0.0"
    }

    private fun animateEntrance() {
        val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(context, R.anim.slide_up)
        binding.btnGrantAccess.startAnimation(fadeIn)
        binding.btnTestSound.startAnimation(slideUp)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
