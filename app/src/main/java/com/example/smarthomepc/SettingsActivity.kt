package com.example.smarthomepc

import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

        }

        override fun onBindPreferences() {
            preferenceScreen.findPreference<EditTextPreference>("edit_text_mac_address")?.
            setOnBindEditTextListener {
                it.setSingleLine()
            }

            preferenceScreen.findPreference<EditTextPreference>("edit_text_inet")?.
            setOnBindEditTextListener { it.setSingleLine() }

            preferenceScreen.findPreference<EditTextPreference>("edit_text_")?.
            setOnBindEditTextListener { it.setSingleLine() }

            preferenceScreen.findPreference<EditTextPreference>("edit_text_broadcast")?.
            setOnBindEditTextListener { it.setSingleLine() }

            preferenceScreen.findPreference<EditTextPreference>("edit_text_port")?.
            setOnBindEditTextListener { it.setSingleLine() }

            preferenceScreen.findPreference<EditTextPreference>("edit_text_username")?.
            setOnBindEditTextListener { it.setSingleLine() }

            preferenceScreen.findPreference<EditTextPreference>("edit_password")?.
            setOnBindEditTextListener {
                it.setSingleLine()
                it.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            } }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle going back to main activity by clicking back arrow.
        return  when (item.itemId) {
            android.R.id.home -> {
                super.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}