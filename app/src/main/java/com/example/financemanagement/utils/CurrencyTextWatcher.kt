package com.example.financemanagement.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

/**
 * TextWatcher that formats currency input with thousand separators
 * Example: 12000000 -> 12.000.000
 */
class CurrencyTextWatcher(private val editText: EditText) : TextWatcher {

    private var current = ""
    
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(editable: Editable?) {
        if (editable.toString() != current) {
            editText.removeTextChangedListener(this)

            // Remove all non-digit characters
            val cleanString = editable.toString().replace("[^\\d]".toRegex(), "")
            
            if (cleanString.isNotEmpty()) {
                try {
                    val parsed = cleanString.toLong()
                    val formatted = formatCurrency(parsed)
                    
                    current = formatted
                    editText.setText(formatted)
                    editText.setSelection(formatted.length)
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }
            } else {
                current = ""
            }

            editText.addTextChangedListener(this)
        }
    }

    private fun formatCurrency(value: Long): String {
        val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
            groupingSeparator = '.'
        }
        val formatter = DecimalFormat("#,###", symbols)
        return formatter.format(value)
    }

    companion object {
        /**
         * Parse formatted currency string to double
         * Example: "12.000.000" -> 12000000.0
         */
        fun parseCurrency(formattedString: String): Double? {
            val cleanString = formattedString.replace(".", "").replace(",", "")
            return cleanString.toDoubleOrNull()
        }
    }
}
