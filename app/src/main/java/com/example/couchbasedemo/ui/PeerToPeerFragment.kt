package com.example.couchbasedemo.ui

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.couchbasedemo.R
import com.example.couchbasedemo.databinding.FragmentFirstBinding
import com.example.couchbasedemo.ui.viewmodel.PeerToPeerViewModel
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class PeerToPeerFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val viewModel = PeerToPeerViewModel()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateLocalIp()

        lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.countFlow.collect { count: Int ->
                    binding.itemsView.text = count.toString()
                }
            }
        }

        binding.addButton.setOnClickListener {
            viewModel.addDocument()

        }

        binding.subtractButton.setOnClickListener {
            viewModel.removeDocument()
        }

        binding.radioGroup.setOnCheckedChangeListener(
            RadioGroup.OnCheckedChangeListener { group, checkedId ->
                if (checkedId == R.id.sender) {
                    viewModel.onServerSelected(requireContext())
                    Toast.makeText(context, "Server", Toast.LENGTH_SHORT).show()
                } else if (checkedId == R.id.receiver) {
                    viewModel.onClientSelected(requireContext())
                    Toast.makeText(context, "Client", Toast.LENGTH_SHORT).show()
                }

                for (i in 0 until group.childCount) {
                    group.getChildAt(i).setEnabled(false)
                }

            })

        viewModel.getAllDocuments()
        //requestNeededPermissions()

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNeededPermissions() {
        val missingPerms: List<String> = checkPerms(viewModel.getPermissions())
        requestPermissions(missingPerms.toTypedArray<String>(), 12345)
    }

    private fun checkPerms(requiredPerms: List<String>): List<String> {
        val perms: MutableList<String> = ArrayList()
        for (perm in requiredPerms) {
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    perm
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                perms.add(perm)
            }
        }
        return perms
    }

    private fun updateLocalIp() {
        val localIp = viewModel.getDeviceIpAddress(requireContext())
        Log.e("LocalIP", "Local IP: $localIp")
    }

    override fun onStop() {
        super.onStop()
        for (i in 0 until binding.radioGroup.childCount) {
            binding.radioGroup.getChildAt(i).setEnabled(true)
            binding.radioGroup.getChildAt(i).isSelected = false
        }
        viewModel.stop(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}