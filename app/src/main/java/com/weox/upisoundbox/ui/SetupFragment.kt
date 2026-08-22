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
import com.weox.upisoundbox.databinding.FragmentSetupBinding
import com.weox.upisoundbox.ui.TestNotificationSender

class SetupFragment : Fragment() {

    private var _binding: FragmentSetupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        animateEntrance()
    }

    private fun setupUI() {
        binding.btnStartSetup.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    }

    private fun animateEntrance() {
        val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(context, R.anim.slide_up)
        binding.btnStartSetup.startAnimation(slideUp)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
