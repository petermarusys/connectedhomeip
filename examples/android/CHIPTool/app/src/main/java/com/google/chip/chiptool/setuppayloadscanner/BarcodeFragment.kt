/*
 *   Copyright (c) 2020 Project CHIP Authors
 *   All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.google.chip.chiptool.setuppayloadscanner

import android.annotation.SuppressLint

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.fragment.app.Fragment
import com.google.chip.chiptool.SelectActionFragment
import com.google.chip.chiptool.databinding.BarcodeFragmentBinding
import com.google.chip.chiptool.util.FragmentUtil
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import matter.onboardingpayload.OnboardingPayloadParser
import matter.onboardingpayload.UnrecognizedQrCodeException

/** Launches the camera to scan for QR code. */
class BarcodeFragment : Fragment() {
  private var _binding: BarcodeFragmentBinding? = null
  private val binding
    get() = _binding!!

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = BarcodeFragmentBinding.inflate(inflater, container, false)
    startCamera()
    binding.inputAddressBtn.setOnClickListener {
      FragmentUtil.getHost(this@BarcodeFragment, SelectActionFragment.Callback::class.java)
        ?.onShowDeviceAddressInput()
    }

    return binding.root
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  @SuppressLint("UnsafeOptInUsageError")
  private fun startCamera() {
    // workaround: can not use gms to scan the code in China, added a EditText to debug
    binding.manualCodeBtn.setOnClickListener {
      val code = binding.manualCodeEditText.text.toString()
      Log.d(TAG, "Submit Code:$code")
      handleInputCode(code)
    }
  }

  private fun handleInputCode(code: String) {
    try {
      val payload =
        if (code.startsWith("MT:")) {
          OnboardingPayloadParser().parseQrCode(code)
        } else {
          OnboardingPayloadParser().parseManualPairingCode(code)
        }

      FragmentUtil.getHost(this@BarcodeFragment, Callback::class.java)
        ?.onCHIPDeviceInfoReceived(CHIPDeviceInfo.fromSetupPayload(payload))
    } catch (ex: UnrecognizedQrCodeException) {
      Log.e(TAG, "Unrecognized Code", ex)
      Toast.makeText(requireContext(), "Unrecognized QR Code", Toast.LENGTH_SHORT).show()
    } catch (ex: Exception) {
      Log.e(TAG, "Exception, $ex")
      Toast.makeText(requireContext(), "Occur Exception, $ex", Toast.LENGTH_SHORT).show()
    }
  }

  /** Interface for notifying the host. */
  interface Callback {
    /** Notifies host of the [CHIPDeviceInfo] from the scanned QR code. */
    fun onCHIPDeviceInfoReceived(deviceInfo: CHIPDeviceInfo)
  }

  companion object {
    private const val TAG = "BarcodeFragment"
    private const val REQUEST_CODE_CAMERA_PERMISSION = 100

    @JvmStatic fun newInstance() = BarcodeFragment()
  }
}
