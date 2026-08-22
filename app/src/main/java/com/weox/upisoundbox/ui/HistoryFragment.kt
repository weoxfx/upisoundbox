package com.weox.upisoundbox.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.weox.upisoundbox.R
import com.weox.upisoundbox.databinding.FragmentHistoryBinding
import com.weox.upisoundbox.model.PaymentHistoryItem
import com.weox.upisoundbox.service.PaymentHistoryStore

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClearButton()
        loadHistory()
        animateEntrance()
    }

    private fun setupRecyclerView() {
        adapter = HistoryAdapter()
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HistoryFragment.adapter
            setHasFixedSize(true)
            itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator().apply {
                addDuration = 250
                removeDuration = 200
                moveDuration = 200
                changeDuration = 200
            }
        }
    }

    private fun setupClearButton() {
        binding.btnClearHistory.setOnClickListener {
            PaymentHistoryStore.clearHistory(requireContext())
            adapter.submitList(emptyList())
            Toast.makeText(requireContext(), R.string.history_cleared, Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadHistory() {
        val history = PaymentHistoryStore.getHistory(requireContext())
        adapter.submitList(history)
        if (history.isEmpty()) {
            binding.rvHistory.visibility = View.GONE
            binding.btnClearHistory.visibility = View.GONE
        } else {
            binding.rvHistory.visibility = View.VISIBLE
            binding.btnClearHistory.visibility = View.VISIBLE
        }
    }

    private fun animateEntrance() {
        val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(context, R.anim.slide_up)
        binding.btnClearHistory.startAnimation(fadeIn)
        binding.rvHistory.startAnimation(slideUp)
    }

    override fun onResume() {
        super.onResume()
        loadHistory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
