package com.gachon.mudang2.layout

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.gachon.mudang2.databinding.DialogInformationBinding

class InformationDialog(private val width: Int, private val height: Int) : DialogFragment() {
    private lateinit var binding: DialogInformationBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DialogInformationBinding.inflate(inflater, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        isCancelable = false

        binding.dialogButton.setOnClickListener {
            dismiss()
        }
        return binding.root
    }


    override fun onResume() {
        super.onResume()
        // 다이얼로그 크기 지정
        dialog?.window?.setLayout(width - 300, ((height - 1400) * 0.75).toInt())

    }
}
