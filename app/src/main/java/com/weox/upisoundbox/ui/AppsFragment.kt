package com.weox.upisoundbox.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.weox.upisoundbox.R
import com.weox.upisoundbox.databinding.FragmentAppsBinding
import com.weox.upisoundbox.service.SelectedAppsStore

class AppsFragment : Fragment() {

    private var _binding: FragmentAppsBinding? = null
    private val binding get() = _binding!!

    private val upiApps = mapOf(
        "com.google.android.apps.nbu.paisa.user" to "Google Pay",
        "com.phonepe.app" to "PhonePe",
        "net.one97.paytm" to "Paytm",
        "in.org.npci.upiapp" to "BHIM"
    )

    private val appIconColors = mapOf(
        "com.google.android.apps.nbu.paisa.user" to R.color.primary_40,
        "com.phonepe.app" to R.color.primary_50,
        "net.one97.paytm" to R.color.accent_cyan,
        "in.org.npci.upiapp" to R.color.accent_violet
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        animateEntrance()
    }

    private fun setupUI() {
        binding.btnSaveSelection.setOnClickListener {
            saveSelection()
        }
    }

    private fun animateEntrance() {
        val slideUp = AnimationUtils.loadAnimation(context, R.anim.slide_up)
        val scaleIn = AnimationUtils.loadAnimation(context, R.anim.scale_in)
        val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)

        binding.layoutAppCards.startAnimation(slideUp)
        binding.btnSaveSelection.startAnimation(scaleIn)
    }

    override fun onResume() {
        super.onResume()
        renderAppList()
    }

    private fun renderAppList() {
        val pm = requireContext().packageManager
        val installed = upiApps.filter { (pkg, _) ->
            try {
                pm.getPackageInfo(pkg, 0)
                true
            } catch (e: Exception) {
                false
            }
        }

        if (installed.isEmpty()) {
            binding.layoutAppCards.removeAllViews()
            val emptyView = android.widget.TextView(requireContext()).apply {
                text = getString(R.string.no_apps_found)
                textSize = 14f
                setTextColor(resources.getColor(R.color.on_surface_dark, null))
                alpha = 0.5f
                gravity = android.view.Gravity.CENTER
                setPadding(0, 48, 0, 48)
            }
            binding.layoutAppCards.addView(emptyView)
            binding.btnSaveSelection.isEnabled = false
            return
        }

        binding.btnSaveSelection.isEnabled = true
        binding.layoutAppCards.removeAllViews()

        val previouslySelected = SelectedAppsStore.getSelected(requireContext())

        installed.forEach { (pkg, label) ->
            val card = ItemAppCardBinding.inflate(
                LayoutInflater.from(requireContext()),
                binding.layoutAppCards,
                true
            )

            val cardView = card.root as com.google.android.material.card.MaterialCardView
            cardView.tag = pkg
            cardView.setOnClickListener {
                val cb = card.cbAppSelected
                cb.isChecked = !cb.isChecked
            }

            card.tvAppName.text = label
            card.tvAppStatus.text = if (pkg in previouslySelected) {
                getString(R.string.status_active)
            } else {
                getString(R.string.status_inactive)
            }
            card.tvAppStatus.setTextColor(
                resources.getColor(
                    if (pkg in previouslySelected) R.color.accent_emerald else R.color.error,
                    null
                )
            )
            card.cbAppSelected.isChecked = pkg in previouslySelected

            val colorRes = appIconColors[pkg] ?: R.color.primary_40
            card.ivAppIcon.setBackgroundColor(resources.getColor(colorRes, null))

            cardView.startAnimation(
                AnimationUtils.loadAnimation(context, R.anim.fade_in)
            )
        }
    }

    private fun saveSelection() {
        val selected = mutableSetOf<String>()
        val container = binding.layoutAppCards
        for (i in 0 until container.childCount) {
            val card = container.getChildAt(i) as? com.google.android.material.card.MaterialCardView
            val pkg = card?.tag as? String ?: continue
            val cb = card.findViewById<android.widget.CheckBox>(R.id.cb_app_selected)
            if (cb != null && cb.isChecked) {
                selected.add(pkg)
            }
        }
        SelectedAppsStore.setSelected(requireContext(), selected)
        Toast.makeText(requireContext(), R.string.apps_saved, Toast.LENGTH_SHORT).show()

        val anim = AnimationUtils.loadAnimation(context, R.anim.scale_in)
        binding.btnSaveSelection.startAnimation(anim)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
